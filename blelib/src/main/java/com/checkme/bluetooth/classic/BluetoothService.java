package com.checkme.bluetooth.classic;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.checkme.bluetooth.classic.cmd.EndReadAckPkg;
import com.checkme.bluetooth.classic.cmd.EndReadPkg;
import com.checkme.bluetooth.classic.cmd.EndWriteAckPkg;
import com.checkme.bluetooth.classic.cmd.EndWritePkg;
import com.checkme.bluetooth.classic.cmd.GetInfoAckPkg;
import com.checkme.bluetooth.classic.cmd.GetInfoPkg;
import com.checkme.bluetooth.classic.cmd.ParaSyncAckPkg;
import com.checkme.bluetooth.classic.cmd.ParaSyncPkg;
import com.checkme.bluetooth.classic.cmd.PingAckPkg;
import com.checkme.bluetooth.classic.cmd.PingPkg;
import com.checkme.bluetooth.classic.cmd.ReadContentAckPkg;
import com.checkme.bluetooth.classic.cmd.ReadContentPkg;
import com.checkme.bluetooth.classic.cmd.StartReadAckPkg;
import com.checkme.bluetooth.classic.cmd.StartReadPkg;
import com.checkme.bluetooth.classic.cmd.StartWriteAckPkg;
import com.checkme.bluetooth.classic.cmd.StartWritePkg;
import com.checkme.bluetooth.classic.cmd.WriteContentAckPkg;
import com.checkme.bluetooth.classic.cmd.WriteContentPkg;
import com.checkme.bluetooth.classic.listener.BTConnectListener;
import com.checkme.bluetooth.classic.listener.GetInfoThreadListener;
import com.checkme.bluetooth.classic.listener.ParaSyncThreadListener;
import com.checkme.bluetooth.classic.listener.PingThreadListener;
import com.checkme.bluetooth.classic.listener.ReadFileListener;
import com.checkme.bluetooth.classic.listener.WriteFileListener;
import com.checkme.bluetooth.utils.LogUtils;
import com.checkme.bluetooth.utils.MsgUtils;
import com.checkme.bluetooth.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class BluetoothService extends Service {

    public LocalBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    private InputStream is;
    private OutputStream os;
    private BluetoothSocket socket = null;

    private final static byte BT_EXP_READ_OPERATION_FAILED = -1;
    private final static byte BT_EXP_WRITE_OPERATION_FAILED = -2;

    private final static byte BT_STATUS_WAITING_PING_ACK = 1;
    private final static byte BT_STATUS_WAITING_START_WRITE_ACK = 2;
    private final static byte BT_STATUS_WAITING_WRITE_CONTENT_ACK = 3;
    private final static byte BT_STATUS_WAITING_END_WRITE_ACK = 4;
    private final static byte BT_STATUS_WAITING_START_READ_ACK = 5;
    private final static byte BT_STATUS_WAITING_READ_CONTENT_ACK = 6;
    private final static byte BT_STATUS_WAITING_END_READ_ACK = 7;
    private final static byte BT_STATUS_WAITING_GET_INFO_ACK = 9;
    private final static byte BT_STATUS_WAITING_PARA_SYNC_ACK = 10;

    private ReadThread readThread;
    private WriteFileThread writeFileThread;
    private ReadFileThread readFileThread;
    private GetInfoThread getInfoThread;
    private PingThread pingThread;
    private ParaSyncThread paraSyncThread;

    Disposable connectDisposable;
    RxBluetooth mRxBluetooth;
    BluetoothDevice mBluetoothDevice;

    private Handler btHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mRxBluetooth = new RxBluetooth(getApplicationContext());
        btHandler = new Handler(getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case BT_EXP_READ_OPERATION_FAILED:
                        interruptAllThread();
                        break;
                    case BT_EXP_WRITE_OPERATION_FAILED:
                        interruptAllThread();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void connectBluetooth(@NonNull BluetoothDevice mBluetoothDevice, String uuidStr, BTConnectListener listener) {
        this.mBluetoothDevice = mBluetoothDevice;
        connectDisposable = mRxBluetooth.observeConnectDevice(mBluetoothDevice, UUID.fromString(uuidStr))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        bluetoothSocket -> {
                            socket = bluetoothSocket;
                            is = bluetoothSocket.getInputStream();
                            os = bluetoothSocket.getOutputStream();
                            listener.onConnectSuccess();
                            if(!connectDisposable.isDisposed()) {
                                connectDisposable.dispose();
                            }
                        },
                        throwable -> {
                            listener.onConnectFailed(BTConnectListener.ERR_CODE_NORMAL, throwable);
                            if(!connectDisposable.isDisposed()) {
                                connectDisposable.dispose();
                            }
                        }
                );
    }

    public void interfacePing(int timeout, PingThreadListener listener){
        if(timeout<=0 || listener==null){
            LogUtils.d("Binder called with err parameters");
            return;
        }else if(isAnyThreadRunning()){
            LogUtils.d("Another thread is running");
            listener.onPingFailed(PingThreadListener.ERR_CODE_BUSY);
            return;
        }
        pingThread = new PingThread(timeout, listener);
        pingThread.start();
    }

    public void interfaceGetInfo(int timeOut, GetInfoThreadListener listener){
        if(timeOut<=0 || listener==null){
            LogUtils.d("Binder called with err parameters");
            return;
        }else if(isAnyThreadRunning()){
            LogUtils.d("Another thread is running");
            listener.onGetInfoFailed(GetInfoThreadListener.ERR_CODE_BUSY);
            return;
        }
        getInfoThread = new GetInfoThread(timeOut, listener);
        getInfoThread.start();
    }

    public void interfaceParaSync(int timeout, ParaSyncThreadListener listener) {
        if(listener==null||timeout<=0) {
            LogUtils.d("Binder called with err parameters");
            return;
        }else if(isAnyThreadRunning()) {
            LogUtils.d("Another thread is running");
            listener.onParaSyncFailed( ParaSyncThreadListener.ERR_CODE_BUSY);
            return;
        }
        JSONObject objectSetTime = new JSONObject();
        try {
            objectSetTime.put(BTConstant.SET_TIME, StringUtils.makeSetTimeString());
        } catch (JSONException e) {
            LogUtils.d("Binder called with err parameters");
            return;
        }
        paraSyncThread = new ParaSyncThread(timeout, objectSetTime,  listener);
        paraSyncThread.start();
    }

    /**
     * For writing files and upgrades Checkme
     * @param fileName
     * @param fileBuf
     * @param cmd
     * @param timeout
     * @param listener
     */
    public void interfaceWriteFile(String fileName, byte[] fileBuf, byte cmd
            , int timeout, WriteFileListener listener) {
        if(fileBuf==null||listener==null||timeout<=0) {
            LogUtils.d("Binder called with err parameters");
            return;
        }else if(isAnyThreadRunning()) {
            LogUtils.d("Another thread is running");
            listener.onWriteFailed(fileName, cmd, WriteFileListener.ERR_CODE_BUSY);
            return;
        }
        writeFileThread = new WriteFileThread(fileName, fileBuf, cmd, timeout, listener);
        writeFileThread.start();
    }

    /**
     * Read file from Checkme
     * @param fileName
     * @param fileType Enter the file type you define,
     * or enter '0' if you don't want to use it.
     * @param timeOut
     * @param listener
     */
    public void interfaceReadFile(String fileName, byte fileType
            , int timeOut, ReadFileListener listener){
        if(fileName == null || listener==null || timeOut<=0) {
            LogUtils.d("Binder called with err parameters");
            return;
        }else if(isAnyThreadRunning()) {
            LogUtils.d("Another thread is running");
            listener.onReadFailed(fileName, fileType, ReadFileListener.ERR_CODE_BUSY);
            return;
        }

        LogUtils.d("Read file " + fileName);
        readFileThread = new ReadFileThread(fileName, fileType, timeOut, listener);
        readFileThread.start();
    }

    /**
     * Check if any thread is running
     * @return
     */
    public boolean isAnyThreadRunning(){
        if (readFileThread!=null) {
            return true;
        }else if (writeFileThread!=null) {
            return true;
        }else if (getInfoThread!=null) {
            return true;
        }else if (pingThread!=null) {
            return true;
        }else {
            return false;
        }
    }

    /**
     *  Interrupt all thread
     */
    private void interruptAllThread() {
        LogUtils.d("Interrupt all thread");

        if(readFileThread!=null){
            readFileThread.interrupt();
            readFileThread = null;
        }
        if(writeFileThread!=null){
            writeFileThread.interrupt();
            writeFileThread = null;
        }
        if(getInfoThread!=null){
            getInfoThread.interrupt();
            getInfoThread = null;
        }
        if (pingThread!=null) {
            pingThread.interrupt();
            pingThread = null;
        }
        if (readThread!=null) {
            readThread.interrupt();
            readThread = null;
        }
        if (paraSyncThread!=null) {
            paraSyncThread.interrupt();
            paraSyncThread = null;
        }
    }

    /**
     * @author zouhao
     *	Read file thread
     */
    private class ReadFileThread extends Thread implements ReadThreadListener{
        private String fileName;
        private byte fileType;
        private int timeOut;
        private int filePkgNums;
        private int curPkgNum = 0;
        private int lastPkgBytes = 0;
        private ReadFileListener listener;
        private byte[] dataPool;

        public ReadFileThread(String fileName, byte fileType, int timeOut, ReadFileListener listener) {
            // TODO Auto-generated constructor stub
            this.fileName = fileName;
            this.fileType = fileType;
            this.timeOut = timeOut;
            this.listener = listener;
        }

        /**
         *  Send Start-read command
         */
        private void startRead() {
            readThread = new ReadThread(BT_STATUS_WAITING_START_READ_ACK
                    , BTConstant.COMMON_ACK_PKG_LENGTH, timeOut, this);
            readThread.start();
            StartReadPkg pkg = new StartReadPkg(fileName);
            writeCMD(pkg.getBuf());
        }

        /**
         * Send Read-content package
         * @param pkgNum
         * @param wantBytes
         */
        private void readContent(int pkgNum, int wantBytes) {
            readThread = new ReadThread(BT_STATUS_WAITING_READ_CONTENT_ACK
                    , wantBytes, timeOut, this);
            readThread.start();
            ReadContentPkg pkg = new ReadContentPkg(pkgNum);
            writeCMD(pkg.getBuf());
            LogUtils.d("Send Read-content package, wating pkg num:"
                    + curPkgNum + ", wating length:" + wantBytes);
        }

        /**
         *  Send End-Read package
         */
        private void endRead(){
            LogUtils.d("Send End-Read package");
            EndReadPkg pkg = new EndReadPkg();
            readThread = new ReadThread(BT_STATUS_WAITING_END_READ_ACK
                    , BTConstant.COMMON_ACK_PKG_LENGTH, timeOut, this);
            readThread.start();
            writeCMD(pkg.getBuf());
        }

        /**
         * Set parameters when received Start-read response package
         * @param fileSize
         */
        private void setFilePkgNums(int fileSize) {
            if(fileSize<=0){
                return;
            }
            dataPool = new byte[fileSize];
            int num = fileSize/BTConstant.READ_CONTENT_ACK_DATA_LENGTH;
            lastPkgBytes = fileSize % BTConstant.READ_CONTENT_ACK_DATA_LENGTH;
            num += (lastPkgBytes == 0) ? 0 : 1;
            lastPkgBytes += BTConstant.READ_CONTENT_ACK_PKG_FRONT_LENGTH;
            filePkgNums = num;
            LogUtils.d("Total want package number:"+num);
        }

        /**
         * Add the data new-received to data pool
         * @param buf
         */
        private void addDataToPool(byte[] buf) {
            if (buf == null || buf.length == 0 || dataPool == null) {
                return;
            }
            int curPos = curPkgNum * BTConstant.READ_CONTENT_ACK_DATA_LENGTH;
            if (curPos + buf.length > dataPool.length) {
                return;
            }
            for (int i = 0; i < buf.length; i++) {
                dataPool[curPos + i] = buf[i];
            }
        }

        /**
         *  Called when read file successfully
         */
        private void readFileSuccess() {
            LogUtils.d("Read file successfully");
            readFileThread = null;
            listener.onReadSuccess(fileName, fileType, dataPool);
        }

        /**
         *  Called when read file failed
         * @param errCode
         */
        private void readFileFailed(byte errCode) {
            LogUtils.d("Read file failed");
            listener.onReadFailed(fileName, fileType, ReadFileListener.ERR_CODE_NORMAL);
            MsgUtils.sendMsg(btHandler, BT_EXP_READ_OPERATION_FAILED);
        }

        @Override
        public void onReadThreadFinished(byte status, byte[] buf) {
            // TODO Auto-generated method stub
            byte cmd;
            switch (status) {
                case BT_STATUS_WAITING_START_READ_ACK:
                    StartReadAckPkg sraPkg = new StartReadAckPkg(buf);
                    cmd = sraPkg.getCmd();
                    if (cmd == BTConstant.ACK_CMD_OK) {
                        if (sraPkg.getFileSize()<=0) {
                            LogUtils.d("File size error, stop to read file");
                            readFileThread.readFileFailed(ReadFileListener.ERR_CODE_NORMAL);
                        }else {
                            readFileThread.setFilePkgNums(sraPkg.getFileSize());
                            synchronized (ACCESSIBILITY_SERVICE) {
                                ACCESSIBILITY_SERVICE.notify();
                            }
                        }
                    } else {
                        LogUtils.d("Response package error");
                        readFileThread.readFileFailed(ReadFileListener.ERR_CODE_NORMAL);
                    }
                    break;
                case BT_STATUS_WAITING_READ_CONTENT_ACK:
                    ReadContentAckPkg rcap = new ReadContentAckPkg(buf);
                    byte[] dataBuf = rcap.getDataBuf();
                    if(rcap.getCmd() != BTConstant.ACK_CMD_OK){
                        LogUtils.d("Response package error");
                        readFileThread.readFileFailed(ReadFileListener.ERR_CODE_NORMAL);
                        return;
                    }else if(dataBuf==null || dataBuf.length<=0){
                        LogUtils.d("Response package error");
                        readFileThread.readFileFailed(ReadFileListener.ERR_CODE_NORMAL);
                        return;
                    }else {
                        readFileThread.addDataToPool(dataBuf);
                        synchronized (ACCESSIBILITY_SERVICE) {
                            ACCESSIBILITY_SERVICE.notify();
                        }
                    }
                    break;
                case BT_STATUS_WAITING_END_READ_ACK:
                    EndReadAckPkg erap = new EndReadAckPkg(buf);
                    cmd = erap.getCmd();
                    if (cmd == BTConstant.ACK_CMD_OK) {
                        synchronized (ACCESSIBILITY_SERVICE) {
                            ACCESSIBILITY_SERVICE.notify();
                        }
                    } else {
                        LogUtils.d("Response package error");
                        readFileThread.readFileFailed(ReadFileListener.ERR_CODE_NORMAL);
                    }
                    break;
            }
        }

        @Override
        public void onReadThreadFailed(byte errCode) {
            // TODO Auto-generated method stub
            readFileFailed(errCode);
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
//			endRead();
            startRead();
            //Read file content
            try {
                synchronized (ACCESSIBILITY_SERVICE) {
                    ACCESSIBILITY_SERVICE.wait();
                    while (curPkgNum < filePkgNums) {
                        if (curPkgNum != filePkgNums-1 || lastPkgBytes == 0) {
                            readContent(curPkgNum,BTConstant.READ_CONTENT_ACK_DATA_LENGTH
                                    + BTConstant.READ_CONTENT_ACK_PKG_FRONT_LENGTH);
                        }else {
                            readContent(curPkgNum,lastPkgBytes);
                        }
                        ACCESSIBILITY_SERVICE.wait();
                        curPkgNum++;
                        //Refresh progress bar
                        listener.onReadPartFinished(fileName, fileType
                                , (float)curPkgNum/filePkgNums);
                    }
                    endRead();
                    ACCESSIBILITY_SERVICE.wait();
                    readFileSuccess();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                LogUtils.d("ReadFile thread is terminated");
            }
        }

    }

    private class ParaSyncThread extends Thread implements ReadThreadListener{
        private int timeOut;
        private JSONObject object;
        private ParaSyncThreadListener listener;
        public ParaSyncThread(int timeOut,JSONObject object,ParaSyncThreadListener listener) {
            this.timeOut=timeOut;
            this.object=object;
            this.listener=listener;
        }
        private void paraSync(){
            LogUtils.d("Start ParaSync");
            ParaSyncPkg paraSyncPkg=new ParaSyncPkg(object);
            readThread=new ReadThread(BT_STATUS_WAITING_PARA_SYNC_ACK,BTConstant.COMMON_ACK_PKG_LENGTH,timeOut,this);
            readThread.start();
            writeCMD(paraSyncPkg.getBuf());
        }
        private void paraSyncSuccess(){
            LogUtils.d("ParaSync success");
            paraSyncThread=null;
            listener.onParaSyncSuccess();
        }
        private void paraSyncFailed(byte errorCode){
            LogUtils.d("ParaSync failed");
            paraSyncThread=null;
            listener.onParaSyncFailed(errorCode);

        }
        @Override
        public void onReadThreadFinished(byte status, byte[] buf) {
            if (status == BT_STATUS_WAITING_PARA_SYNC_ACK) {
                ParaSyncAckPkg paraSyncAckPkg = new ParaSyncAckPkg(buf);
                if (paraSyncAckPkg.getCmd()==BTConstant.ACK_CMD_OK) {
                    paraSyncSuccess();
                }else {
                    paraSyncFailed(ParaSyncThreadListener.ERR_CODE_NORMAL);
                }
            }
        }

        @Override
        public void onReadThreadFailed(byte errCode) {
            paraSyncFailed(errCode);
        }

        @Override
        public void run() {
            super.run();
            paraSync();
        }
    }

    /**
     * @author zouhao
     *	Get the information of Checkme
     */
    private class GetInfoThread extends Thread implements ReadThreadListener{

        private int timeOut;
        private GetInfoThreadListener listener;

        public GetInfoThread(int timeOut, GetInfoThreadListener listener) {
            super();
            this.timeOut = timeOut;
            this.listener = listener;
        }

        private void getInfo() {
            LogUtils.d("Start to get information from Checkme");
            GetInfoPkg pkg = new GetInfoPkg();
            readThread = new ReadThread(BT_STATUS_WAITING_GET_INFO_ACK
                    , BTConstant.GET_INFO_ACK_PKG_LENGTH, timeOut, this);
            readThread.start();
            writeCMD(pkg.getBuf());
        }

        private void getInfoSuccess(String checkmeInfo) {
            LogUtils.d("Get Checkme information successfully");
            getInfoThread = null;
            listener.onGetInfoSuccess(checkmeInfo);
        }

        private void getInfoFailed(byte errCode)	{
            LogUtils.d("Get Checkme information failed");
            getInfoThread = null;
            listener.onGetInfoFailed(errCode);
        }

        @Override
        public void onReadThreadFinished(byte status, byte[] buf) {
            // TODO Auto-generated method stub
            if (status == BT_STATUS_WAITING_GET_INFO_ACK) {
                GetInfoAckPkg giap = new GetInfoAckPkg(buf);
                if (giap.getDataBufStr()!=null) {
                    getInfoSuccess(giap.getDataBufStr());
                }else {
                    getInfoFailed(GetInfoThreadListener.ERR_CODE_NORMAL);
                }
            }
        }

        @Override
        public void onReadThreadFailed(byte errCode) {
            // TODO Auto-generated method stub
            getInfoFailed(errCode);
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            getInfo();
        }

    }

    /**
     * @author zouhao
     * Send Ping command after connect to Checkme
     */
    private class PingThread extends Thread implements ReadThreadListener{

        private int timeOut;
        private PingThreadListener listener;

        public PingThread(int timeOut, PingThreadListener listener) {
            super();
            this.timeOut = timeOut;
            this.listener = listener;
        }

        private void startPing() {
            LogUtils.d("Start to ping");
            PingPkg pkg = new PingPkg();
            readThread = new ReadThread(BT_STATUS_WAITING_PING_ACK
                    , BTConstant.COMMON_ACK_PKG_LENGTH, timeOut, this);
            readThread.start();
            writeCMD(pkg.getBuf());
        }

        private void pingSuccess() {
            pingThread = null;
            listener.onPingSuccess();
        }

        private void pingFailed(byte errCode) {
            pingThread = null;
            listener.onPingFailed(errCode);
        }

        @Override
        public void onReadThreadFinished(byte status, byte[] buf) {
            if (status == BT_STATUS_WAITING_PING_ACK) {
                PingAckPkg pap = new PingAckPkg(buf);
                byte cmd = pap.getCmd();
                byte[] datBuf=pap.getBuf();
                if (cmd == BTConstant.ACK_CMD_OK&&datBuf[0]==0x55) {
                    pingSuccess();
                } else {
                    if (datBuf[0]==(byte)0xA5&&datBuf[1]==0x5A){
                        pingFailed(PingThreadListener.ERR_CODE_MONITOR);
                    }else {
                        LogUtils.d("Response package error");
                        pingFailed(PingThreadListener.ERR_CODE_NORMAL);

                    }
                }
            }
        }

        @Override
        public void onReadThreadFailed(byte errCode) {
            // TODO Auto-generated method stub
            pingFailed(errCode);
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            startPing();
        }
    }

    /**
     *	For writing files and upgrades Checkme
     *  with different fileType
     * @author zouhao
     */
    private class WriteFileThread extends Thread implements ReadThreadListener{
        private String fileName;
        private byte fileType;
        private int timeOut;
        private int writeOffset = 0; //The file position current writing
        private int curPkgNum = 0; //current package number
        public byte[] preBuf;//Last package buffer, used to rewrite
        private byte[] fileBuf;
        private WriteFileListener listener;

        public WriteFileThread(String fileName, byte[] fileBuf, byte cmd
                , int timeOut, WriteFileListener listener) {
            this.fileName = fileName;
            this.fileBuf = fileBuf;
            this.fileType = cmd;
            this.timeOut = timeOut;
            this.listener = listener;
        }

        /**
         * Send Start-Write command
         * @param fileName
         * @param fileSize
         */
        private void startWrite(String fileName, int fileSize) {
            LogUtils.d("Start to write, file name:" + fileName + "file size:" + fileSize);
            StartWritePkg pkg = new StartWritePkg(fileName, fileSize, fileType);
            byte[] buf = pkg.getBuf();
            readThread = new ReadThread(BT_STATUS_WAITING_START_WRITE_ACK
                    , BTConstant.COMMON_ACK_PKG_LENGTH, timeOut, this);
            readThread.start();
            writeCMD(buf);
            preBuf = pkg.getBuf();
        }

        /**
         * Send write-content package
         *
         */
        private void writeContent() {
            LogUtils.d("Write data package");
            //Open a read thread, and waiting to be awakened
            readThread = new ReadThread(BT_STATUS_WAITING_WRITE_CONTENT_ACK
                    , BTConstant.COMMON_ACK_PKG_LENGTH, timeOut, this);
            readThread.start();
            //Get data buffer and write to Bluetooth socket
            byte[] buffer;
            if (writeOffset + BTConstant.WRITE_CONTENT_PKG_DATA_LENGTH <= fileBuf.length) {
                buffer = new byte[BTConstant.WRITE_CONTENT_PKG_DATA_LENGTH];
                curPkgNum = writeOffset / BTConstant.WRITE_CONTENT_PKG_DATA_LENGTH;
            } else {
                //Last package
                buffer = new byte[fileBuf.length - writeOffset];
                curPkgNum ++;
            }
            System.arraycopy(fileBuf, writeOffset, buffer, 0, buffer.length);
            //fileType + 1,xxx_start_write + 1 = xxx_wirte_content
            WriteContentPkg pkg = new WriteContentPkg(buffer, curPkgNum,(byte)(fileType+1));

            writeCMD(pkg.getBuf());
            //Record last buffer, used to rewrite
            preBuf = pkg.getBuf();
            writeOffset += buffer.length;
            listener.onWritePartFinished(fileName, fileType
                    , (float)writeOffset/fileBuf.length);
        }

        /**
         *  Send End-write package
         */
        private void endWrite() {
            LogUtils.d("Send End-write package");
            readThread = new ReadThread(BT_STATUS_WAITING_END_WRITE_ACK
                    , BTConstant.COMMON_ACK_PKG_LENGTH, timeOut, this);
            readThread.start();
            //fileType + 2,xxx_start_write + 2 = xxx_wirte_end
            EndWritePkg pkg = new EndWritePkg((byte)(fileType+2));
            writeCMD(pkg.getBuf());
            //Record last buffer, used to rewrite
            preBuf = pkg.getBuf();
        }

        /**
         * Called when write file successfully
         */
        private void writeSuccess() {
            LogUtils.d("Write file successfully");
            writeFileThread = null;
//			MsgUtils.SendMSG(callerHandler, fileType);
            listener.onWriteSuccess(fileName, fileType);
        }

        /**
         *  Called when write file failed
         */
        private void writeFailed(byte errCode) {
            LogUtils.d("Write file failed");
            MsgUtils.sendMsg(btHandler, BT_EXP_WRITE_OPERATION_FAILED);
            listener.onWriteFailed(fileName, fileType, errCode);
        }

        @Override
        public void onReadThreadFinished(byte status, byte[] buf) {
            // TODO Auto-generated method stub
            byte cmd;
            switch (status) {
                case BT_STATUS_WAITING_START_WRITE_ACK:
                    StartWriteAckPkg swaPkg = new StartWriteAckPkg(buf);
                    cmd = swaPkg.getCmd();
                    if (cmd == BTConstant.ACK_CMD_OK) {
                        synchronized (ACCESSIBILITY_SERVICE) {
                            ACCESSIBILITY_SERVICE.notify();
                        }
                    } else {
                        LogUtils.d("Response package error");
                        writeFailed(ReadThreadListener.ERR_CODE_NORMAL);
                    }
                    break;
                case BT_STATUS_WAITING_WRITE_CONTENT_ACK:
                    WriteContentAckPkg wcaPkg = new WriteContentAckPkg(buf);
                    cmd = wcaPkg.getCmd();
                    if (cmd == BTConstant.ACK_CMD_OK) {
                        synchronized (ACCESSIBILITY_SERVICE) {
                            ACCESSIBILITY_SERVICE.notify();
                        }
                    } else {
                        LogUtils.d("Response package error");
                        if(writeFileThread!=null && writeFileThread.preBuf!=null) {
                            LogUtils.d("Rewite command");
                            reWrite();
                        }else {
                            writeFileThread.writeFailed(ReadThreadListener.ERR_CODE_NORMAL);
                        }
                    }
                    break;
                case BT_STATUS_WAITING_END_WRITE_ACK:
                    EndWriteAckPkg ewap = new EndWriteAckPkg(buf);
                    cmd = ewap.getCmd();
                    if (cmd == BTConstant.ACK_CMD_OK) {
                        synchronized (ACCESSIBILITY_SERVICE) {
                            ACCESSIBILITY_SERVICE.notify();
                        }
                    } else {
                        LogUtils.d("Response package error");
                        if(writeFileThread!=null && writeFileThread.preBuf!=null
                                && writeFileThread.preBuf.length!=0) {
                            writeFailed(ReadThreadListener.ERR_CODE_NORMAL);
                        }
                    }
                    break;
            }
        }

        @Override
        public void onReadThreadFailed(byte errCode) {
            writeFailed(errCode);
        }

        /**
         *  Rewrite last package
         */
        private void reWrite() {
            if (preBuf!=null && preBuf.length!=0) {
                readThread = new ReadThread(BT_STATUS_WAITING_WRITE_CONTENT_ACK
                        , BTConstant.COMMON_ACK_PKG_LENGTH, timeOut, this);
                readThread.start();
                writeCMD(preBuf);
                preBuf = null;
            }
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            // Start to write file
            startWrite(fileName, fileBuf.length);
            // Start to write content
            try {
                synchronized (ACCESSIBILITY_SERVICE) {
                    while (writeOffset < fileBuf.length) {
                        ACCESSIBILITY_SERVICE.wait();
                        writeContent();
                    }
                    //Waiting for end write
                    ACCESSIBILITY_SERVICE.wait();
                    endWrite();
                    //end write -> normal file, write success -> app update pkg
                    if(fileType != BTConstant.CMD_WORD_APP_UPDATE_START){
                        ACCESSIBILITY_SERVICE.wait();
                    }
                    writeSuccess();
                }
            } catch (InterruptedException e) {
                LogUtils.d("Write file thread is interrupted");
            }
        }
    }

    /**
     *	Bluetooth socket Readthread
     *	Created when you need to read data, and terminated after reading
     * @author zouhao
     */
    private class ReadThread extends Thread {
        private byte status;
        private int wantBytes;
        private int timeOut;
        private byte[] buf;
        private ReadThreadListener listener;

        public ReadThread(byte status, int wantBytes, int timeOut, ReadThreadListener listener) {
            this.status = status;
            this.wantBytes = wantBytes;
            this.timeOut = timeOut;
            this.listener = listener;
            buf = new byte[wantBytes];
        }

        /**
         *  Read data from Bluetooth socket
         */
        private void readBytes() {
            int readCount = 0, preNum = 0;
            try {
                LogUtils.d("Readthread started");
                while (preNum < wantBytes) {
                    while (readCount == 0){
                        readCount = is.available();
                        sleep(0, 1);
                    }
                    if ((preNum + readCount) > buf.length) {
                        is.read(buf, preNum, buf.length - preNum);
                        LogUtils.d("Received: " + (buf.length - preNum)+ ", PreNum" + preNum);
                        preNum += buf.length - preNum;
                    } else {
                        is.read(buf, preNum, readCount);
                        preNum += readCount;
                        LogUtils.d("Received: " + (readCount) + ", PreNum"+ preNum);
                    }
                    readCount = 0;
                }
                LogUtils.d("Readthread ended");
                listener.onReadThreadFinished(status, buf);
            } catch (IOException e) {
                // TODO: handle exception
                LogUtils.d("Readthread IOException");
                listener.onReadThreadFailed(ReadThreadListener.ERR_CODE_EXP);
            } catch (InterruptedException e) {
                // TODO: handle exception
                e.printStackTrace();
                LogUtils.d("Readthread Interrupted");
            }
        }

        @Override
        public void run() {
            if (timeOut <= 0) {
                LogUtils.d("Timeout parameter error");
                return;
            }
            ExecutorService executor = Executors.newSingleThreadExecutor();
            FutureTask<String> future = new FutureTask<String>(new Callable<String>() {
                public String call() {
                    readBytes();
                    return "";
                }});
            executor.execute(future);

            try {
                future.get(timeOut, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                LogUtils.d("Readthread is terminated");
                future.cancel(true);
            } catch (ExecutionException e) {
                LogUtils.d("Readthread execution exception");
                future.cancel(true);
                listener.onReadThreadFailed(ReadThreadListener.ERR_CODE_TIMEOUT);
            } catch (TimeoutException e) {
                LogUtils.d("Readthread timeout");
                future.cancel(true);
                listener.onReadThreadFailed(ReadThreadListener.ERR_CODE_TIMEOUT);
            } finally {
                LogUtils.d("Shutdown excutor");
                executor.shutdown();
            }
        }
    }

    /**
     *	ReadThread Listener
     * @author zouhao
     */
    private interface ReadThreadListener {
        public static final byte ERR_CODE_NORMAL = -1;
        public static final byte ERR_CODE_TIMEOUT = -2;
        public static final byte ERR_CODE_BUSY = -3;
        public static final byte ERR_CODE_EXP = -4;

        public void onReadThreadFinished(byte status, byte[] buf);

        public void onReadThreadFailed(byte errCode);
    }

    /**
     * @param buf
     * Write data to Bluetooth socket
     */
    private void writeCMD(byte[] buf) {
        try {
            os.write(buf);
        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
