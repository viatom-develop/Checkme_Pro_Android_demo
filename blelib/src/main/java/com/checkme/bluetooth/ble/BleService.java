package com.checkme.bluetooth.ble;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import androidx.annotation.Nullable;

import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.checkme.bluetooth.Logger;
import com.checkme.bluetooth.ble.callback.OnBleConnectResult;
import com.checkme.bluetooth.ble.listener.GetInfoBleListener;
import com.checkme.bluetooth.ble.listener.ParaSyncBleListener;
import com.checkme.bluetooth.ble.listener.PingBleListener;
import com.checkme.bluetooth.ble.listener.ReadBleFileListener;
import com.checkme.bluetooth.classic.BTConstant;
import com.checkme.bluetooth.classic.cmd.EndReadAckPkg;
import com.checkme.bluetooth.classic.cmd.EndReadPkg;
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
import com.checkme.bluetooth.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class BleService extends Service implements OnBleConnectResult {

    private Disposable connectStateChangeDisposable;
    private Disposable connectDisposable;

    private String serviceUUID;
    private String readUUID;
    private String writeUUID;

    RxBleConnection mRxBleConnection;

    public LocalBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    public RxBleDevice mCurrentRxBleDevice;
    private RxBleHelper mRxBleHelper;
    @Override
    public void onCreate() {
        super.onCreate();
        mRxBleHelper = RxBleHelper.newInstance(getApplicationContext());
        btHandler = new Handler(getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    default:
                        break;
                }
            }
        };
    }

    public static final int BLE_REQUEST_ID_AVAILABLE = -1;
    public static final int BLE_REQUEST_ID_PING = 0;
    public static final int BLE_REQUEST_ID_GET_INFO = 1;
    public static final int BLE_REQUEST_ID_PARA_SYNC = 2;
    public static final int BLE_REQUEST_ID_START_READ_FILE = 3;
    public static final int BLE_REQUEST_ID_READ_FILE_CONTENT = 4;
    public static final int BLE_REQUEST_ID_END_READ_FILE = 5;

    private int  currentRequestId = BLE_REQUEST_ID_AVAILABLE;
    private byte[] tmpData;
    private int start = 0;

    // file read/write
    private int fileSize = 0;
    private int num = 0;
    private int lastPkgBytes = 0;
    private int curPkgNum = 0;
    private int curPkaSize = 0;
    private byte[] dataPool;

    // listener
    PingBleListener mPingBleListener;
    GetInfoBleListener mGetInfoBleListener;
    ParaSyncBleListener mParaSyncBleListener;

    // read/write file
    ReadBleFileListener mReadBleFileListener;
    String mFileName;
    byte mFileType;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private Handler btHandler;

    public void connectBLE(@NonNull BluetoothDevice mBluetoothDevice,
           String serviceUUID, String readUUID, String writeUUID, OnBleConnectResult onBleConnectResult) {
        this.mCurrentRxBleDevice = mRxBleHelper.getRxBleDevice(mBluetoothDevice.getAddress());
        this.serviceUUID = serviceUUID;
        this.readUUID = readUUID;
        this.writeUUID = writeUUID;

        connectStateChangeDisposable = mRxBleHelper.observeConnectionStateChanges(mCurrentRxBleDevice)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mRxBleConnectionState ->  {
                    Logger.d(BleService.class, "ConnectionState == " + mRxBleConnectionState.toString());
                });
        connectDisposable = mRxBleHelper.connect(mCurrentRxBleDevice)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        mRxBleConnection -> {
                            Logger.e(BleService.class, "connectBLE connected");
                            this.mRxBleConnection = mRxBleConnection;
                            onBleConnectResult.onBleConnected();
                        },
                        throwable -> {
                            Logger.e(BleService.class, "connectBLE onError");
                            Logger.e(BleService.class, throwable.getMessage(), throwable);
                            onBleConnectResult.onBleConnectedError(throwable);
                        },
                        () -> {
                            Logger.d(BleService.class, "connectBLE onComplete");
                        },
                        disposable -> {
                            Logger.d(BleService.class, "connectBLE onSubscribe");
                        }
                );
    }

    @Override
    public void onBleConnected() {
        Logger.d(BleService.class, " onBleConnected");
        Disposable disposableNo = mRxBleConnection.setupNotification(UUID.fromString(readUUID))
                .flatMap(notificationObservable -> notificationObservable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        bytes -> {
                            Logger.d(BleService.class, "onNext");
                            Logger.d(BleService.class, "bytes == " + bytes);
                            if (bytes != null && bytes.length > 0) {
                                Logger.d(BleService.class, "bytes.length == " + bytes.length);
                                final StringBuilder stringBuilder = new StringBuilder(bytes.length);
                                for(byte byteChar : bytes)
                                    stringBuilder.append(String.format("%02X ", byteChar));
                                Logger.d(BleService.class, "bytes == " + stringBuilder.toString());
                                onCommandReceive(bytes);
                            }
                        },
                        throwable -> {
                            Logger.e(BleService.class, "onError");
                            Logger.e(BleService.class, throwable.getMessage(), throwable);
                        },
                        () -> {
                            Logger.d(BleService.class, "onComplete");
                        },
                        disposable -> {
                            Logger.d(BleService.class, "onSubscribe");
                        }

                );
    }

    @Override
    public void onBleConnectedError(Throwable throwable) {
        if(connectDisposable != null && !connectDisposable.isDisposed()) {
            connectDisposable.isDisposed();
        }
    }

    private void onCommandReceive(byte[] bytes) {
        if(currentRequestId == BLE_REQUEST_ID_PING) {
            System.arraycopy(bytes,0, tmpData, start, bytes.length );
            start = start + bytes.length;
            if(start == BTConstant.COMMON_ACK_PKG_LENGTH) {
                PingAckPkg pingAckPkg = new PingAckPkg(tmpData);
                byte cmd = pingAckPkg.getCmd();
                byte[] datBuf = pingAckPkg.getBuf();
                if(cmd == BTConstant.ACK_CMD_OK&&datBuf[0]==0x55) {
                    // ping已成功返回
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BleService.class,"Ping Response package validate");
                    if(mPingBleListener != null) {
                        mPingBleListener.onPingBleSuccess();
                    }
//                    btHandler.sendEmptyMessage(1);
                } else {
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BleService.class,"Ping Response package error");
                    if(mPingBleListener != null) {
                        mPingBleListener.onPingFailed(PingBleListener.ERR_CODE_NORMAL);
                    }
                }
                start = 0;
            }
        } else if(currentRequestId == BLE_REQUEST_ID_GET_INFO) {
            System.arraycopy(bytes,0, tmpData, start, bytes.length );
            start = start + bytes.length;
            if(start == BTConstant.GET_INFO_ACK_PKG_LENGTH) {
                GetInfoAckPkg getInfoAckPkg = new GetInfoAckPkg(tmpData);
                if (getInfoAckPkg.getDataBufStr()!=null) {
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BleService.class,"GetInfo Response package validate");
                    Logger.d(BleService.class, "deviceInfo == " + getInfoAckPkg.getDataBufStr());
                    if(mGetInfoBleListener != null) {
                        mGetInfoBleListener.onGetInfoBleSuccess(getInfoAckPkg.getDataBufStr());
                    }
                }else {
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BleService.class,"GetInfo Response package error");
                    if(mGetInfoBleListener != null) {
                        mGetInfoBleListener.onGetInfoFailed(GetInfoBleListener.ERR_CODE_NORMAL);
                    }
                }
                start = 0;
            }
        } else if(currentRequestId == BLE_REQUEST_ID_PARA_SYNC) {
            System.arraycopy(bytes,0, tmpData, start, bytes.length );
            start = start + bytes.length;
            if(start == BTConstant.COMMON_ACK_PKG_LENGTH) {
                ParaSyncAckPkg paraSyncAckPkg = new ParaSyncAckPkg(tmpData);
                if (paraSyncAckPkg.getCmd() == BTConstant.ACK_CMD_OK) {
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BleService.class,"ParaSync Response package validate");
                    if(mParaSyncBleListener != null) {
                        mParaSyncBleListener.onParaSyncBleSuccess();
                    }
//                    interfaceReadFile("usr.dat");
                } else {
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BleService.class,"ParaSync Response package error");
                    if(mParaSyncBleListener != null) {
                        mParaSyncBleListener.onParaSyncFailed(ParaSyncBleListener.ERR_CODE_NORMAL);
                    }
                }
                start = 0;
            }
        } else if(currentRequestId == BLE_REQUEST_ID_START_READ_FILE) {
            System.arraycopy(bytes,0, tmpData, start, bytes.length );
            start = start + bytes.length;
            if (start == BTConstant.COMMON_ACK_PKG_LENGTH) {
                StartReadAckPkg sraPkg = new StartReadAckPkg(tmpData);
                byte cmd = sraPkg.getCmd();
                if (cmd == BTConstant.ACK_CMD_OK) {
                    if (sraPkg.getFileSize()<=0) {
                        Logger.d(BleService.class,"interfaceReadFile read file info Response package error");
                        if(mReadBleFileListener != null) {
                            mReadBleFileListener.onReadFailed(mFileName, mFileType, ReadBleFileListener.ERR_CODE_EXP);
                        }
                        resetFileInfo();
                        currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    } else {
                        Logger.d(BleService.class,"interfaceReadFile read file info Response package validate");
                        Logger.d(BleService.class,"start read file content");
                        Logger.d(BleService.class," read file content fileSize == " + sraPkg.getFileSize());
                        setCurrentReadFileInfo(sraPkg.getFileSize());
                        if(num == 1) {
                            start = 0;
                            readFileContent(curPkgNum, lastPkgBytes);
                        } else {
                            start = 0;
                            readFileContent(curPkgNum, BTConstant.READ_CONTENT_ACK_DATA_LENGTH_BLE
                                    + BTConstant.READ_CONTENT_ACK_PKG_FRONT_LENGTH);
                        }
                    }
                } else {
                    if(mReadBleFileListener != null) {
                        mReadBleFileListener.onReadFailed(mFileName, mFileType, ReadBleFileListener.ERR_CODE_EXP);
                    }
                    resetFileInfo();
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BleService.class,"interfaceReadFile read file Response package error");
                }
                start = 0;
            }
        }else if(currentRequestId == BLE_REQUEST_ID_READ_FILE_CONTENT) {
            System.arraycopy(bytes,0, tmpData, start, bytes.length );
            start = start + bytes.length;
            Logger.d(BleService.class, "onCommandReceive BLE_REQUEST_ID_READ_FILE_CONTENT");
            Logger.d(BleService.class, "onCommandReceive start == " + start);
            Logger.d(BleService.class, "onCommandReceive curPkaSize == " + curPkaSize);
            if (start == curPkaSize) {
                ReadContentAckPkg rcap = new ReadContentAckPkg(tmpData);
                byte[] dataBuf = rcap.getDataBuf();
                if(rcap.getCmd() != BTConstant.ACK_CMD_OK){
                    Logger.d(BleService.class, "readFileContent Response package error");
                    resetFileInfo();
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    if(mReadBleFileListener != null) {
                        mReadBleFileListener.onReadFailed(mFileName, mFileType, ReadBleFileListener.ERR_CODE_EXP);
                    }
                } else if(dataBuf==null || dataBuf.length<=0){
                    resetFileInfo();
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BleService.class, "readFileContent Response package size error");
                    if(mReadBleFileListener != null) {
                        mReadBleFileListener.onReadFailed(mFileName, mFileType, ReadBleFileListener.ERR_CODE_EXP);
                    }
                } else {
                    int curPos = curPkgNum * BTConstant.READ_CONTENT_ACK_DATA_LENGTH_BLE;
                    System.arraycopy(dataBuf,0, dataPool, curPos, dataBuf.length );
                    if(mReadBleFileListener != null) {
                        float percent = (curPos + dataBuf.length) * 1.0f / fileSize ;
                        mReadBleFileListener.onBleReadPartFinished(mFileName, mFileType, percent);
                    }
                    Logger.d(BleService.class,"interfaceReadFile read content Response package validate");
                    curPkgNum = curPkgNum  + 1;
                    if(curPkgNum < num) {
                        if (curPkgNum != num - 1 || lastPkgBytes == 0) {
                            Logger.d(BleService.class,"start to read next pkg");
                            readFileContent(curPkgNum,BTConstant.READ_CONTENT_ACK_DATA_LENGTH_BLE
                                    + BTConstant.READ_CONTENT_ACK_PKG_FRONT_LENGTH);
                        } else {
                            Logger.d(BleService.class,"start to read last pkg");
                            readFileContent(curPkgNum,lastPkgBytes);
                        }
                    } else {
                        Logger.d(BleService.class,"stop reading file content");
                        endFileRead();
                    }
                }
                start = 0;
            }
        } else if(currentRequestId == BLE_REQUEST_ID_END_READ_FILE) {
            System.arraycopy(bytes,0, tmpData, start, bytes.length );
            start = start + bytes.length;
            if (start == BTConstant.COMMON_ACK_PKG_LENGTH) {
                EndReadAckPkg erap = new EndReadAckPkg(tmpData);
                byte cmd  = erap.getCmd();
                if (cmd == BTConstant.ACK_CMD_OK) {
                    if(mReadBleFileListener != null) {
                        float percent = 1.0f;
                        mReadBleFileListener.onBleReadPartFinished(mFileName, mFileType, percent);
                        mReadBleFileListener.onBleReadSuccess(mFileName, mFileType, dataPool);
                    }
                    resetFileInfo();
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BleService.class, "endFileRead Response package validate");
                } else {
                    resetFileInfo();
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BleService.class,"endFileRead Response package error");
                    if(mReadBleFileListener != null) {
                        mReadBleFileListener.onReadFailed(mFileName, mFileType, ReadBleFileListener.ERR_CODE_EXP);
                    }
                }
            }
            start = 0;
        }
    }

    public void interfacePing(PingBleListener pingBleListener){
        this.mPingBleListener = pingBleListener;
        if(currentRequestId != BLE_REQUEST_ID_AVAILABLE) {
            Logger.e(BleService.class, "service busy...");
            mPingBleListener.onPingFailed(PingBleListener.ERR_CODE_BUSY);
            return;
        }
        Logger.d(BleService.class, " interfacePing");
        btHandler.post(() -> {
            PingPkg pkg = new PingPkg();
            Single<byte[]> single = mRxBleHelper.writeCharacteristic(mRxBleConnection, UUID.fromString(writeUUID), pkg.getBuf());
            if(single != null) {
                Disposable disposable = single.subscribe(
                        bytes -> {
                            Logger.d(BleService.class, "interfacePing onNext");
                            final StringBuilder stringBuilder = new StringBuilder( bytes.length);
                            for(byte byteChar :  bytes)
                                stringBuilder.append(String.format("%02X ", byteChar));
                            Logger.d(BleService.class, "interfacePing onNext pkg.getBuf() == " + stringBuilder.toString());
                            currentRequestId = BLE_REQUEST_ID_PING;
                            tmpData = new byte[BTConstant.COMMON_ACK_PKG_LENGTH];
                        },
                        throwable -> {
                            Logger.e(BleService.class, "interfacePing onError");
                            Logger.e(BleService.class, throwable.getMessage(), throwable);
                            mPingBleListener.onPingFailed(PingBleListener.ERR_CODE_EXP);
                        }
                );
            } else {
                Logger.d(BleService.class, "interfacePing single == null");
                mPingBleListener.onPingFailed(PingBleListener.ERR_CODE_TIMEOUT);
            }
        });

    }

    public void interfaceGetInfo(GetInfoBleListener getInfoBleListener){
        this.mGetInfoBleListener = getInfoBleListener;
        if(currentRequestId != BLE_REQUEST_ID_AVAILABLE) {
            mGetInfoBleListener.onGetInfoFailed(GetInfoBleListener.ERR_CODE_BUSY);
            return;
        }
        Logger.d(BleService.class, " interfaceGetInfo");
        btHandler.post(() -> {
            GetInfoPkg pkg = new GetInfoPkg();
            Single<byte[]> single = mRxBleHelper.writeCharacteristic(mRxBleConnection, UUID.fromString(writeUUID), pkg.getBuf());
            if(single != null) {
                Disposable disposable = single.subscribe(
                        bytes -> {
                            Logger.d(BleService.class, "interfaceGetInfo onNext");
                            final StringBuilder stringBuilder = new StringBuilder( bytes.length);
                            for(byte byteChar :  bytes)
                                stringBuilder.append(String.format("%02X ", byteChar));
                            Logger.d(BleService.class, "interfaceGetInfo onNext pkg.getBuf() == " + stringBuilder.toString());
                            currentRequestId = BLE_REQUEST_ID_GET_INFO;
                            tmpData = new byte[BTConstant.GET_INFO_ACK_PKG_LENGTH];
                        },
                        throwable -> {
                            Logger.e(BleService.class, "interfaceGetInfo onError");
                            Logger.e(BleService.class, throwable.getMessage(), throwable);
                            mGetInfoBleListener.onGetInfoFailed(GetInfoBleListener.ERR_CODE_EXP);
                        }
                );
            } else {
                Logger.d(BleService.class, "interfaceGetInfo single == null");
                mGetInfoBleListener.onGetInfoFailed(GetInfoBleListener.ERR_CODE_TIMEOUT);
            }
        });
    }

    public void interfaceParaSync(ParaSyncBleListener paraSyncBleListener){
        this.mParaSyncBleListener = paraSyncBleListener;
        if(currentRequestId != BLE_REQUEST_ID_AVAILABLE) {
            mParaSyncBleListener.onParaSyncFailed(ParaSyncBleListener.ERR_CODE_BUSY);
            return;
        }
        Logger.d(BleService.class, " interfaceParaSync");
        btHandler.post(() -> {
            JSONObject objectSetTime = new JSONObject();
            try {
                objectSetTime.put(BTConstant.SET_TIME, StringUtils.makeSetTimeString());
            } catch (JSONException e) {

            }
            Logger.d(BleService.class, "objectSetTime  == " + objectSetTime.toString());
            ParaSyncPkg paraSyncPkg = new ParaSyncPkg(objectSetTime);
            Observable<byte[]> observable = mRxBleHelper.longWriteCharacteristic(mRxBleConnection, UUID.fromString(writeUUID), paraSyncPkg.getBuf());
            if (observable != null) {
                Disposable disposable = observable.subscribe(
                        bytes -> {
                            Logger.d(BleService.class, "interfaceParaSync onNext");
                            final StringBuilder stringBuilder = new StringBuilder(bytes.length);
                            for (byte byteChar : bytes)
                                stringBuilder.append(String.format("%02X ", byteChar));
                            Logger.d(BleService.class, " interfaceParaSync onNext pkg.getBuf() == " + stringBuilder.toString());
                            currentRequestId = BLE_REQUEST_ID_PARA_SYNC;
                            tmpData = new byte[BTConstant.COMMON_ACK_PKG_LENGTH];
                        },
                        throwable -> {
                            Logger.e(BleService.class, "interfaceParaSync onError");
                            Logger.e(BleService.class, throwable.getMessage(), throwable);
                            mParaSyncBleListener.onParaSyncFailed(ParaSyncBleListener.ERR_CODE_EXP);
                        }
                );
            } else {
                Logger.d(BleService.class, "interfaceParaSync single == null");
                mParaSyncBleListener.onParaSyncFailed(ParaSyncBleListener.ERR_CODE_TIMEOUT);
            }
        });
    }

    public void interfaceReadFile(String fileName, byte fileType, ReadBleFileListener listener) {
        this.mFileName = fileName;
        this.mFileType = fileType;
        this.mReadBleFileListener = listener;
        if(currentRequestId != BLE_REQUEST_ID_AVAILABLE) {
            mReadBleFileListener.onReadFailed(mFileName, mFileType,  ReadBleFileListener.ERR_CODE_BUSY);
            return;
        }
        Logger.d(BleService.class, " interfaceReadFile");
        Logger.d(BleService.class, " fileName == " + fileName);

        btHandler.post(() -> {
            StartReadPkg startReadPkg = new StartReadPkg(fileName);
            Observable<byte[]> observable = mRxBleHelper.longWriteCharacteristic(mRxBleConnection, UUID.fromString(writeUUID), startReadPkg.getBuf());
            if(observable != null) {
                Disposable disposable = observable.subscribe(
                        bytes -> {
                            Logger.d(BleService.class, "interfaceReadFile onNext");
                            final StringBuilder stringBuilder = new StringBuilder( bytes.length);
                            for(byte byteChar :  bytes)
                                stringBuilder.append(String.format("%02X ", byteChar));
                            Logger.d(BleService.class, " interfaceReadFile onNext pkg.getBuf() == " + stringBuilder.toString());
                            currentRequestId = BLE_REQUEST_ID_START_READ_FILE;
                            tmpData = new byte[BTConstant.COMMON_ACK_PKG_LENGTH];
                        },
                        throwable -> {
                            Logger.e(BleService.class, "interfaceReadFile onError");
                            Logger.e(BleService.class, throwable.getMessage(), throwable);
                            mReadBleFileListener.onReadFailed( mFileName, mFileType, ReadBleFileListener.ERR_CODE_EXP);
                        }
                );
            } else {
                Logger.d(BleService.class, "interfaceReadFile single == null");
                mReadBleFileListener.onReadFailed( mFileName, mFileType, ReadBleFileListener.ERR_CODE_TIMEOUT);
            }
        });
    }

    private void setCurrentReadFileInfo(int fileSize) {
        this.fileSize = fileSize;
        dataPool = new byte[fileSize];
        num = fileSize/BTConstant.READ_CONTENT_ACK_DATA_LENGTH_BLE;
        lastPkgBytes = fileSize % BTConstant.READ_CONTENT_ACK_DATA_LENGTH_BLE;
        num += (lastPkgBytes == 0) ? 0 : 1;
        lastPkgBytes += BTConstant.READ_CONTENT_ACK_PKG_FRONT_LENGTH;
    }

    private void resetFileInfo() {
        start = 0;
        fileSize = 0;
        num = 0;
        lastPkgBytes = 0;
        curPkgNum = 0;
        curPkaSize = 0;
    }

    private void readFileContent(int pkgNum, int wantBytes) {
        this.curPkaSize = wantBytes;
        Logger.d(BleService.class, " readFileContent");
        Logger.d(BleService.class, " pkgNum == " + pkgNum);
        Logger.d(BleService.class, " curPkaSize == " + curPkaSize);

        btHandler.post(() -> {
            ReadContentPkg pkg = new ReadContentPkg(pkgNum);
            Single<byte[]> single = mRxBleHelper.writeCharacteristic(mRxBleConnection, UUID.fromString(writeUUID), pkg.getBuf());
            if(single != null) {
                Disposable disposable = single.subscribe(
                        bytes -> {
                            Logger.d(BleService.class, "readFileContent onNext");
                            final StringBuilder stringBuilder = new StringBuilder( bytes.length);
                            for(byte byteChar :  bytes)
                                stringBuilder.append(String.format("%02X ", byteChar));
                            Logger.d(BleService.class, "readFileContent onNext pkg.getBuf() == " + stringBuilder.toString());
                            currentRequestId = BLE_REQUEST_ID_READ_FILE_CONTENT;
                            tmpData = new byte[curPkaSize];
                        },
                        throwable -> {
                            Logger.e(BleService.class, "readFileContent onError");
                            Logger.e(BleService.class, throwable.getMessage(), throwable);
                            if(mReadBleFileListener != null) {
                                mReadBleFileListener.onReadFailed(mFileName, mFileType, ReadBleFileListener.ERR_CODE_EXP);
                            }
                        }
                );
            } else {
                Logger.d(BleService.class, "readFileContent single == null");
                if(mReadBleFileListener != null) {
                    mReadBleFileListener.onReadFailed(mFileName, mFileType, ReadBleFileListener.ERR_CODE_TIMEOUT);
                }
            }
        });
    }

    private void endFileRead() {
        Logger.d(BleService.class, " endFileRead");
        Logger.d(BleService.class, "Send End-Read package");
        btHandler.post(() -> {
            EndReadPkg pkg = new EndReadPkg();
            Single<byte[]> single = mRxBleHelper.writeCharacteristic(mRxBleConnection, UUID.fromString(writeUUID), pkg.getBuf());
            if(single != null) {
                Disposable disposable = single.subscribe(
                        bytes -> {
                            Logger.d(BleService.class, "endFileRead onNext");
                            final StringBuilder stringBuilder = new StringBuilder( bytes.length);
                            for(byte byteChar :  bytes)
                                stringBuilder.append(String.format("%02X ", byteChar));
                            Logger.d(BleService.class, "endFileRead onNext pkg.getBuf() == " + stringBuilder.toString());
                            currentRequestId = BLE_REQUEST_ID_END_READ_FILE;
                            tmpData = new byte[BTConstant.COMMON_ACK_PKG_LENGTH];
                        },
                        throwable -> {
                            Logger.e(BleService.class, "endFileRead onError");
                            Logger.e(BleService.class, throwable.getMessage(), throwable);
                            if(mReadBleFileListener != null) {
                                mReadBleFileListener.onReadFailed(mFileName, mFileType, ReadBleFileListener.ERR_CODE_EXP);
                            }
                        }
                );
            } else {
                Logger.d(BleService.class, "endFileRead single == null");
                btHandler.sendEmptyMessage(5);
                if(mReadBleFileListener != null) {
                    mReadBleFileListener.onReadFailed(mFileName, mFileType, ReadBleFileListener.ERR_CODE_TIMEOUT);
                }
            }
        });
    }

    public boolean isAnyThreadRunning() {
        return  currentRequestId != BLE_REQUEST_ID_AVAILABLE;
    }
}
