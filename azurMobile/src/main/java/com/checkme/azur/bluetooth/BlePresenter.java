package com.checkme.azur.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.checkme.azur.element.Constant;
import com.checkme.azur.measurement.MeasurementConstant;
import com.checkme.azur.utils.FileDriver;
import com.checkme.bluetooth.Logger;
import com.checkme.bluetooth.ble.RxBleHelper;
import com.checkme.bluetooth.ble.callback.OnBleConnectResult;
import com.checkme.bluetooth.ble.listener.BleConnectionStateListener;
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
import com.checkme.bluetooth.classic.cmd.ReadContentAckPkg;
import com.checkme.bluetooth.classic.cmd.ReadContentPkg;
import com.checkme.bluetooth.classic.cmd.StartReadAckPkg;
import com.checkme.bluetooth.classic.cmd.StartReadPkg;
import com.checkme.bluetooth.utils.StringUtils;
import com.checkme.update.SharedPrefHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by gongguopei on 2019/1/3.
 */
public class BlePresenter implements OnBleConnectResult{

    private static volatile BlePresenter instance;

    private SharedPrefHelper sharedPrefHelper;
    private Handler btHandler;
    private RxBleHelper mRxBleHelper;

    private BlePresenter(Context context) {
        sharedPrefHelper = SharedPrefHelper.newInstance(context.getApplicationContext());
        mRxBleHelper = RxBleHelper.newInstance(context.getApplicationContext());
        btHandler = new Handler(Looper.getMainLooper()) {
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

    public static BlePresenter newInstance(Context context) {
        if(instance == null) {
            synchronized (BlePresenter.class) {
                if(instance == null) {
                    instance = new BlePresenter(context);
                    return instance;
                }
            }
        }
        return instance;
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


    private RxBleDevice mCurrentRxBleDevice;
    private String serviceUUID;
    private String readUUID;
    private String writeUUID;
    private RxBleConnection mRxBleConnection;

    // listener
    PingBleListener mPingBleListener;
    GetInfoBleListener mGetInfoBleListener;
    ParaSyncBleListener mParaSyncBleListener;
    BleConnectionStateListener mBleConnectionStateListener;

    // read/write file
    ReadBleFileListener mReadBleFileListener;
    String mFileName;
    byte mFileType;

    private Disposable connectStateChangeDisposable;
    private Disposable connectDisposable;


    public void connectBLE(@NonNull BluetoothDevice mBluetoothDevice,
                           String serviceUUID, String readUUID, String writeUUID, OnBleConnectResult onBleConnectResult) {
        this.mCurrentRxBleDevice = mRxBleHelper.getRxBleDevice(mBluetoothDevice.getAddress());
        this.serviceUUID = serviceUUID;
        this.readUUID = readUUID;
        this.writeUUID = writeUUID;

        connectStateChangeDisposable = mRxBleHelper.observeConnectionStateChanges(mCurrentRxBleDevice)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mRxBleConnectionState ->  {
                    Logger.d(BlePresenter.class, "ConnectionState == " + mRxBleConnectionState.toString());
                    if(mRxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.DISCONNECTED)) {
                        if (Constant.btConnectFlag) {
                            Constant.btConnectFlag = false;
                        }
                        if(mBleConnectionStateListener != null) {
                            mBleConnectionStateListener.onBLEDisconnect();
                        }
                    }
                });
        connectDisposable = mRxBleHelper.connect(mCurrentRxBleDevice)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        mRxBleConnection -> {
                            Logger.e(BlePresenter.class, "connectBLE connected");
                            this.mRxBleConnection = mRxBleConnection;
                            onBleConnectResult.onBleConnected();
                        },
                        throwable -> {
                            Logger.e(BlePresenter.class, "connectBLE onError");
                            Logger.e(BlePresenter.class, throwable.getMessage(), throwable);
                            onBleConnectResult.onBleConnectedError(throwable);
                        },
                        () -> {
                            Logger.d(BlePresenter.class, "connectBLE onComplete");
                        },
                        disposable -> {
                            Logger.d(BlePresenter.class, "connectBLE onSubscribe");
                        }
                );
    }

    @Override
    public void onBleConnected() {
        Logger.d(BlePresenter.class, " onBleConnected");
        Disposable disposableNo = mRxBleConnection.setupNotification(UUID.fromString(readUUID))
                .flatMap(notificationObservable -> notificationObservable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        bytes -> {
                            Logger.d(BlePresenter.class, "onNext");
                            Logger.d(BlePresenter.class, "bytes == " + bytes);
                            if (bytes != null && bytes.length > 0) {
                                Logger.d(BlePresenter.class, "bytes.length == " + bytes.length);
                                final StringBuilder stringBuilder = new StringBuilder(bytes.length);
                                for(byte byteChar : bytes)
                                    stringBuilder.append(String.format("%02X ", byteChar));
                                Logger.d(BlePresenter.class, "bytes == " + stringBuilder.toString());
                                onCommandReceive(bytes);
                            }
                        },
                        throwable -> {
                            Logger.e(BlePresenter.class, "onError");
                            Logger.e(BlePresenter.class, throwable.getMessage(), throwable);
                        },
                        () -> {
                            Logger.d(BlePresenter.class, "onComplete");
                        },
                        disposable -> {
                            Logger.d(BlePresenter.class, "onSubscribe");
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
            if(start == com.checkme.bluetooth.classic.BTConstant.COMMON_ACK_PKG_LENGTH) {
                com.checkme.bluetooth.classic.cmd.PingAckPkg pingAckPkg = new com.checkme.bluetooth.classic.cmd.PingAckPkg(tmpData);
                byte cmd = pingAckPkg.getCmd();
                byte[] datBuf = pingAckPkg.getBuf();
                if(cmd == com.checkme.bluetooth.classic.BTConstant.ACK_CMD_OK&&datBuf[0]==0x55) {
                    // ping已成功返回
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BlePresenter.class,"Ping Response package validate");
                    if(mPingBleListener != null) {
                        mPingBleListener.onPingBleSuccess();
                    }
                    //                    btHandler.sendEmptyMessage(1);
                } else {
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BlePresenter.class,"Ping Response package error");
                    if(mPingBleListener != null) {
                        mPingBleListener.onPingFailed(PingBleListener.ERR_CODE_NORMAL);
                    }
                }
                start = 0;
            }
        } else if(currentRequestId == BLE_REQUEST_ID_GET_INFO) {
            System.arraycopy(bytes,0, tmpData, start, bytes.length );
            start = start + bytes.length;
            if(start == com.checkme.bluetooth.classic.BTConstant.GET_INFO_ACK_PKG_LENGTH) {
                com.checkme.bluetooth.classic.cmd.GetInfoAckPkg getInfoAckPkg = new GetInfoAckPkg(tmpData);
                if (getInfoAckPkg.getDataBufStr()!=null) {
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BlePresenter.class,"GetInfo Response package validate");
                    Logger.d(BlePresenter.class, "deviceInfo == " + getInfoAckPkg.getDataBufStr());
                    if(mGetInfoBleListener != null) {
                        mGetInfoBleListener.onGetInfoBleSuccess(getInfoAckPkg.getDataBufStr());
                    }
                }else {
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BlePresenter.class,"GetInfo Response package error");
                    if(mGetInfoBleListener != null) {
                        mGetInfoBleListener.onGetInfoFailed(GetInfoBleListener.ERR_CODE_NORMAL);
                    }
                }
                start = 0;
            }
        } else if(currentRequestId == BLE_REQUEST_ID_PARA_SYNC) {
            System.arraycopy(bytes,0, tmpData, start, bytes.length );
            start = start + bytes.length;
            if(start == com.checkme.bluetooth.classic.BTConstant.COMMON_ACK_PKG_LENGTH) {
                com.checkme.bluetooth.classic.cmd.ParaSyncAckPkg paraSyncAckPkg = new ParaSyncAckPkg(tmpData);
                if (paraSyncAckPkg.getCmd() == com.checkme.bluetooth.classic.BTConstant.ACK_CMD_OK) {
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BlePresenter.class,"ParaSync Response package validate");
                    if(mParaSyncBleListener != null) {
                        mParaSyncBleListener.onParaSyncBleSuccess();
                    }
                    //                    interfaceReadFile("usr.dat");
                } else {
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BlePresenter.class,"ParaSync Response package error");
                    if(mParaSyncBleListener != null) {
                        mParaSyncBleListener.onParaSyncFailed(ParaSyncBleListener.ERR_CODE_NORMAL);
                    }
                }
                start = 0;
            }
        } else if(currentRequestId == BLE_REQUEST_ID_START_READ_FILE) {
            System.arraycopy(bytes,0, tmpData, start, bytes.length );
            start = start + bytes.length;
            if (start == com.checkme.bluetooth.classic.BTConstant.COMMON_ACK_PKG_LENGTH) {
                com.checkme.bluetooth.classic.cmd.StartReadAckPkg sraPkg = new StartReadAckPkg(tmpData);
                byte cmd = sraPkg.getCmd();
                if (cmd == com.checkme.bluetooth.classic.BTConstant.ACK_CMD_OK) {
                    if (sraPkg.getFileSize()<=0) {
                        Logger.d(BlePresenter.class,"interfaceReadFile read file info Response package error");
                        if(mReadBleFileListener != null) {
                            mReadBleFileListener.onReadFailed(mFileName, mFileType, ReadBleFileListener.ERR_CODE_EXP);
                        }
                        resetFileInfo();
                        currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    } else {
                        Logger.d(BlePresenter.class,"interfaceReadFile read file info Response package validate");
                        Logger.d(BlePresenter.class,"start read file content");
                        Logger.d(BlePresenter.class," read file content fileSize == " + sraPkg.getFileSize());
                        setCurrentReadFileInfo(sraPkg.getFileSize());
                        if(num == 1) {
                            start = 0;
                            readFileContent(curPkgNum, lastPkgBytes);
                        } else {
                            start = 0;
                            readFileContent(curPkgNum, com.checkme.bluetooth.classic.BTConstant.READ_CONTENT_ACK_DATA_LENGTH_BLE
                                    + com.checkme.bluetooth.classic.BTConstant.READ_CONTENT_ACK_PKG_FRONT_LENGTH);
                        }
                    }
                } else {
                    if(mReadBleFileListener != null) {
                        mReadBleFileListener.onReadFailed(mFileName, mFileType, ReadBleFileListener.ERR_CODE_EXP);
                    }
                    resetFileInfo();
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BlePresenter.class,"interfaceReadFile read file Response package error");
                }
                start = 0;
            }
        }else if(currentRequestId == BLE_REQUEST_ID_READ_FILE_CONTENT) {
            System.arraycopy(bytes,0, tmpData, start, bytes.length );
            start = start + bytes.length;
            Logger.d(BlePresenter.class, "onCommandReceive BLE_REQUEST_ID_READ_FILE_CONTENT");
            Logger.d(BlePresenter.class, "onCommandReceive start == " + start);
            Logger.d(BlePresenter.class, "onCommandReceive curPkaSize == " + curPkaSize);
            if (start == curPkaSize) {
                com.checkme.bluetooth.classic.cmd.ReadContentAckPkg rcap = new ReadContentAckPkg(tmpData);
                byte[] dataBuf = rcap.getDataBuf();
                if(rcap.getCmd() != com.checkme.bluetooth.classic.BTConstant.ACK_CMD_OK){
                    Logger.d(BlePresenter.class, "readFileContent Response package error");
                    resetFileInfo();
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    if(mReadBleFileListener != null) {
                        mReadBleFileListener.onReadFailed(mFileName, mFileType, ReadBleFileListener.ERR_CODE_EXP);
                    }
                } else if(dataBuf==null || dataBuf.length<=0){
                    resetFileInfo();
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BlePresenter.class, "readFileContent Response package size error");
                    if(mReadBleFileListener != null) {
                        mReadBleFileListener.onReadFailed(mFileName, mFileType, ReadBleFileListener.ERR_CODE_EXP);
                    }
                } else {
                    int curPos = curPkgNum * com.checkme.bluetooth.classic.BTConstant.READ_CONTENT_ACK_DATA_LENGTH_BLE;
                    System.arraycopy(dataBuf,0, dataPool, curPos, dataBuf.length );
                    if(mReadBleFileListener != null) {
                        float percent = (curPos + dataBuf.length) * 1.0f / fileSize ;
                        mReadBleFileListener.onBleReadPartFinished(mFileName, mFileType, percent);
                    }
                    Logger.d(BlePresenter.class,"interfaceReadFile read content Response package validate");
                    curPkgNum = curPkgNum  + 1;
                    if(curPkgNum < num) {
                        if (curPkgNum != num - 1 || lastPkgBytes == 0) {
                            Logger.d(BlePresenter.class,"start to read next pkg");
                            readFileContent(curPkgNum, com.checkme.bluetooth.classic.BTConstant.READ_CONTENT_ACK_DATA_LENGTH_BLE
                                    + com.checkme.bluetooth.classic.BTConstant.READ_CONTENT_ACK_PKG_FRONT_LENGTH);
                        } else {
                            Logger.d(BlePresenter.class,"start to read last pkg");
                            readFileContent(curPkgNum,lastPkgBytes);
                        }
                    } else {
                        Logger.d(BlePresenter.class,"stop reading file content");
                        endFileRead();
                    }
                }
                start = 0;
            }
        } else if(currentRequestId == BLE_REQUEST_ID_END_READ_FILE) {
            System.arraycopy(bytes,0, tmpData, start, bytes.length );
            start = start + bytes.length;
            if (start == com.checkme.bluetooth.classic.BTConstant.COMMON_ACK_PKG_LENGTH) {
                com.checkme.bluetooth.classic.cmd.EndReadAckPkg erap = new EndReadAckPkg(tmpData);
                byte cmd  = erap.getCmd();
                if (cmd == com.checkme.bluetooth.classic.BTConstant.ACK_CMD_OK) {
                    if(mReadBleFileListener != null) {
                        float percent = 1.0f;
                        mReadBleFileListener.onBleReadPartFinished(mFileName, mFileType, percent);
                        mReadBleFileListener.onBleReadSuccess(mFileName, mFileType, dataPool);
                    }
                    resetFileInfo();
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BlePresenter.class, "endFileRead Response package validate");
                } else {
                    resetFileInfo();
                    currentRequestId = BLE_REQUEST_ID_AVAILABLE;
                    Logger.d(BlePresenter.class,"endFileRead Response package error");
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
            Logger.e(BlePresenter.class, "service busy...");
            mPingBleListener.onPingFailed(PingBleListener.ERR_CODE_BUSY);
            return;
        }
        Logger.d(BlePresenter.class, " interfacePing");
        btHandler.post(() -> {
            com.checkme.bluetooth.classic.cmd.PingPkg pkg = new com.checkme.bluetooth.classic.cmd.PingPkg();
            Single<byte[]> single = mRxBleHelper.writeCharacteristic(mRxBleConnection, UUID.fromString(writeUUID), pkg.getBuf());
            if(single != null) {
                Disposable disposable = single.subscribe(
                        bytes -> {
                            Logger.d(BlePresenter.class, "interfacePing onNext");
                            final StringBuilder stringBuilder = new StringBuilder( bytes.length);
                            for(byte byteChar :  bytes)
                                stringBuilder.append(String.format("%02X ", byteChar));
                            Logger.d(BlePresenter.class, " interfacePing onNext pkg.getBuf() == " + stringBuilder.toString());
                            currentRequestId = BLE_REQUEST_ID_PING;
                            tmpData = new byte[BTConstant.COMMON_ACK_PKG_LENGTH];
                        },
                        throwable -> {
                            Logger.e(BlePresenter.class, "interfacePing onError");
                            Logger.e(BlePresenter.class, throwable.getMessage(), throwable);
                            mPingBleListener.onPingFailed(PingBleListener.ERR_CODE_EXP);
                        }
                );
            } else {
                Logger.d(BlePresenter.class, "interfacePing single == null");
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
        Logger.d(BlePresenter.class, " interfaceGetInfo");
        btHandler.post(() -> {
            com.checkme.bluetooth.classic.cmd.GetInfoPkg pkg = new GetInfoPkg();
            Single<byte[]> single = mRxBleHelper.writeCharacteristic(mRxBleConnection, UUID.fromString(writeUUID), pkg.getBuf());
            if(single != null) {
                Disposable disposable = single.subscribe(
                        bytes -> {
                            Logger.d(BlePresenter.class, "interfaceGetInfo onNext");
                            final StringBuilder stringBuilder = new StringBuilder( bytes.length);
                            for(byte byteChar :  bytes)
                                stringBuilder.append(String.format("%02X ", byteChar));
                            Logger.d(BlePresenter.class, "interfaceGetInfo onNext pkg.getBuf() == " + stringBuilder.toString());
                            currentRequestId = BLE_REQUEST_ID_GET_INFO;
                            tmpData = new byte[BTConstant.GET_INFO_ACK_PKG_LENGTH];
                        },
                        throwable -> {
                            Logger.e(BlePresenter.class, "interfaceGetInfo onError");
                            Logger.e(BlePresenter.class, throwable.getMessage(), throwable);
                            mGetInfoBleListener.onGetInfoFailed(GetInfoBleListener.ERR_CODE_EXP);
                        }
                );
            } else {
                Logger.d(BlePresenter.class, "interfaceGetInfo single == null");
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
        Logger.d(BlePresenter.class, " interfaceParaSync");
        btHandler.post(() -> {
            JSONObject objectSetTime = new JSONObject();
            try {
                objectSetTime.put(BTConstant.SET_TIME, StringUtils.makeSetTimeString());
            } catch (JSONException e) {

            }
            Logger.d(BlePresenter.class, "objectSetTime  == " + objectSetTime.toString());
            com.checkme.bluetooth.classic.cmd.ParaSyncPkg paraSyncPkg = new ParaSyncPkg(objectSetTime);
            Observable<byte[]> observable = mRxBleHelper.longWriteCharacteristic(mRxBleConnection, UUID.fromString(writeUUID), paraSyncPkg.getBuf());
            if (observable != null) {
                Disposable disposable = observable.subscribe(
                        bytes -> {
                            Logger.d(BlePresenter.class, "interfaceParaSync onNext");
                            final StringBuilder stringBuilder = new StringBuilder(bytes.length);
                            for (byte byteChar : bytes)
                                stringBuilder.append(String.format("%02X ", byteChar));
                            Logger.d(BlePresenter.class, " interfaceParaSync onNext pkg.getBuf() == " + stringBuilder.toString());
                            currentRequestId = BLE_REQUEST_ID_PARA_SYNC;
                            tmpData = new byte[BTConstant.COMMON_ACK_PKG_LENGTH];
                        },
                        throwable -> {
                            Logger.e(BlePresenter.class, "interfaceParaSync onError");
                            Logger.e(BlePresenter.class, throwable.getMessage(), throwable);
                            mParaSyncBleListener.onParaSyncFailed(ParaSyncBleListener.ERR_CODE_EXP);
                        }
                );
            } else {
                Logger.d(BlePresenter.class, "interfaceParaSync single == null");
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
        Logger.d(BlePresenter.class, " interfaceReadFile");
        Logger.d(BlePresenter.class, " fileName == " + fileName);

        String fileVerKey = sharedPrefHelper.readStringValue(Constant.FILE_VER_KEY);
        if (fileVerKey.equals(Constant.FILE_VER_NEW)) {
            byte[] buf=new byte[1];
            buf[0]=0;
            FileDriver.write(Constant.dir, fileName+ MeasurementConstant.QT_FILE_NAME, buf);
        }else{
            byte[] buf=new byte[1];
            buf[0]=1;
            FileDriver.write(Constant.dir, fileName+MeasurementConstant.QT_FILE_NAME, buf);
        }

        btHandler.post(() -> {
            com.checkme.bluetooth.classic.cmd.StartReadPkg startReadPkg = new StartReadPkg(fileName);
            Observable<byte[]> observable = mRxBleHelper.longWriteCharacteristic(mRxBleConnection, UUID.fromString(writeUUID), startReadPkg.getBuf());
            if(observable != null) {
                Disposable disposable = observable.subscribe(
                        bytes -> {
                            Logger.d(BlePresenter.class, "interfaceReadFile onNext");
                            final StringBuilder stringBuilder = new StringBuilder( bytes.length);
                            for(byte byteChar :  bytes)
                                stringBuilder.append(String.format("%02X ", byteChar));
                            Logger.d(BlePresenter.class, " interfaceReadFile onNext pkg.getBuf() == " + stringBuilder.toString());
                            currentRequestId = BLE_REQUEST_ID_START_READ_FILE;
                            tmpData = new byte[BTConstant.COMMON_ACK_PKG_LENGTH];
                        },
                        throwable -> {
                            Logger.e(BlePresenter.class, "interfaceReadFile onError");
                            Logger.e(BlePresenter.class, throwable.getMessage(), throwable);
                            mReadBleFileListener.onReadFailed( mFileName, mFileType, ReadBleFileListener.ERR_CODE_EXP);
                        }
                );
            } else {
                Logger.d(BlePresenter.class, "interfaceReadFile single == null");
                mReadBleFileListener.onReadFailed( mFileName, mFileType, ReadBleFileListener.ERR_CODE_TIMEOUT);
            }
        });
    }


    private void setCurrentReadFileInfo(int fileSize) {
        this.fileSize = fileSize;
        dataPool = new byte[fileSize];
        num = fileSize/ com.checkme.bluetooth.classic.BTConstant.READ_CONTENT_ACK_DATA_LENGTH_BLE;
        lastPkgBytes = fileSize % com.checkme.bluetooth.classic.BTConstant.READ_CONTENT_ACK_DATA_LENGTH_BLE;
        num += (lastPkgBytes == 0) ? 0 : 1;
        lastPkgBytes += com.checkme.bluetooth.classic.BTConstant.READ_CONTENT_ACK_PKG_FRONT_LENGTH;
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
        Logger.d(BlePresenter.class, " readFileContent");
        Logger.d(BlePresenter.class, " pkgNum == " + pkgNum);
        Logger.d(BlePresenter.class, " curPkaSize == " + curPkaSize);

        btHandler.post(() -> {
            com.checkme.bluetooth.classic.cmd.ReadContentPkg pkg = new ReadContentPkg(pkgNum);
            Single<byte[]> single = mRxBleHelper.writeCharacteristic(mRxBleConnection, UUID.fromString(writeUUID), pkg.getBuf());
            if(single != null) {
                Disposable disposable = single.subscribe(
                        bytes -> {
                            Logger.d(BlePresenter.class, "readFileContent onNext");
                            final StringBuilder stringBuilder = new StringBuilder( bytes.length);
                            for(byte byteChar :  bytes)
                                stringBuilder.append(String.format("%02X ", byteChar));
                            Logger.d(BlePresenter.class, "readFileContent onNext pkg.getBuf() == " + stringBuilder.toString());
                            currentRequestId = BLE_REQUEST_ID_READ_FILE_CONTENT;
                            tmpData = new byte[curPkaSize];
                        },
                        throwable -> {
                            Logger.e(BlePresenter.class, "readFileContent onError");
                            Logger.e(BlePresenter.class, throwable.getMessage(), throwable);
                            if(mReadBleFileListener != null) {
                                mReadBleFileListener.onReadFailed(mFileName, mFileType, ReadBleFileListener.ERR_CODE_EXP);
                            }
                        }
                );
            } else {
                Logger.d(BlePresenter.class, "readFileContent single == null");
                if(mReadBleFileListener != null) {
                    mReadBleFileListener.onReadFailed(mFileName, mFileType, ReadBleFileListener.ERR_CODE_TIMEOUT);
                }
            }
        });
    }

    private void endFileRead() {
        Logger.d(BlePresenter.class, " endFileRead");
        Logger.d(BlePresenter.class, "Send End-Read package");
        btHandler.post(() -> {
            com.checkme.bluetooth.classic.cmd.EndReadPkg pkg = new EndReadPkg();
            Single<byte[]> single = mRxBleHelper.writeCharacteristic(mRxBleConnection, UUID.fromString(writeUUID), pkg.getBuf());
            if(single != null) {
                Disposable disposable = single.subscribe(
                        bytes -> {
                            Logger.d(BlePresenter.class, "endFileRead onNext");
                            final StringBuilder stringBuilder = new StringBuilder( bytes.length);
                            for(byte byteChar :  bytes)
                                stringBuilder.append(String.format("%02X ", byteChar));
                            Logger.d(BlePresenter.class, "endFileRead onNext pkg.getBuf() == " + stringBuilder.toString());
                            currentRequestId = BLE_REQUEST_ID_END_READ_FILE;
                            tmpData = new byte[BTConstant.COMMON_ACK_PKG_LENGTH];
                        },
                        throwable -> {
                            Logger.e(BlePresenter.class, "endFileRead onError");
                            Logger.e(BlePresenter.class, throwable.getMessage(), throwable);
                            if(mReadBleFileListener != null) {
                                mReadBleFileListener.onReadFailed(mFileName, mFileType, ReadBleFileListener.ERR_CODE_EXP);
                            }
                        }
                );
            } else {
                Logger.d(BlePresenter.class, "endFileRead single == null");
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
