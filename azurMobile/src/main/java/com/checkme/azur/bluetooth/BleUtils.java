package com.checkme.azur.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.checkme.azur.utils.LogUtils;
import com.checkme.bluetooth.ble.callback.OnBleConnectResult;
import com.checkme.bluetooth.ble.listener.GetInfoBleListener;
import com.checkme.bluetooth.ble.listener.ParaSyncBleListener;
import com.checkme.bluetooth.ble.listener.PingBleListener;
import com.checkme.bluetooth.ble.listener.ReadBleFileListener;

import org.json.JSONObject;

/**
 * Created by gongguopei on 2019/1/2.
 */
public class BleUtils extends Service implements BTBinder {

    // BLE UUID
    public final static String CHECKME_BLE_SERVICE_UUID = "14839ac4-7d7e-415c-9a42-167340cf2339";
    public final static String CHECKME_BLE_WRITE_UUID = "8b00ace7-eb0b-49b0-bbe9-9aee0a26e1a3";
    public final static String CHECKME_BLE_READ_UUID = "0734594a-a8e7-4b1a-a6b1-cd5243059a57";



    LocalBinder mLocalBinder = new LocalBinder();

    public class LocalBinder extends Binder {

        public BleUtils getService() {
            return BleUtils.this;
        }

    }


    BlePresenter blePresenter;

    @Override
    public void onCreate() {
        super.onCreate();

        blePresenter = BlePresenter.newInstance(getApplicationContext());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        LogUtils.d("BT service is Unbinded");
        return true;
    }


    @Override
    public void interfaceConnect(BluetoothDevice bluetoothDevice, BTConnectListener listener) {
        if(blePresenter != null) {
            blePresenter.connectBLE(bluetoothDevice,
                    CHECKME_BLE_SERVICE_UUID,
                    CHECKME_BLE_READ_UUID,
                    CHECKME_BLE_WRITE_UUID, new OnBleConnectResult() {
                        @Override
                        public void onBleConnected() {
                            listener.onConnectSuccess();
                            blePresenter.onBleConnected();
                        }

                        @Override
                        public void onBleConnectedError(Throwable throwable) {
                            listener.onConnectFailed(BTConnectListener.ERR_CODE_EXP);
                        }
                    });
        }
    }

    @Override
    public void interfaceParaSync(JSONObject jsonObject, int timeout, ParaSyncThreadListener listener) {
        if(blePresenter != null) {
            blePresenter.interfaceParaSync(new ParaSyncBleListener() {
                @Override
                public void onParaSyncBleSuccess() {
                    listener.onParaSyncSuccess();
                }

                @Override
                public void onParaSyncFailed(byte errCode) {
                    listener.onParaSyncFailed(errCode);
                }
            });
        }
    }

    @Override
    public void interfaceWriteFile(String fileName, byte[] fileBuf, byte cmd, int timeout, WriteFileListener listener) {
        //
    }

    @Override
    public void interfaceReadFile(String fileName, byte fileType, int timeOut, ReadFileListener listener) {
        if(blePresenter != null) {
            blePresenter.interfaceReadFile(fileName, fileType, new ReadBleFileListener() {
                @Override
                public void onBleReadPartFinished(String fileName, byte fileType, float percentage) {
                    listener.onReadPartFinished(fileName, fileType, percentage);
                }

                @Override
                public void onBleReadSuccess(String fileName, byte fileType, byte[] fileBuf) {
                    listener.onReadSuccess(fileName, fileType, fileBuf);
                }

                @Override
                public void onReadFailed(String fileName, byte fileType, byte errCode) {
                    listener.onReadFailed(fileName, fileType, errCode);
                }
            });
        }
    }

    @Override
    public void interfaceGetInfo(int timeOut, GetInfoThreadListener listener) {
        if(blePresenter != null) {
            blePresenter.interfaceGetInfo(new GetInfoBleListener() {
                @Override
                public void onGetInfoBleSuccess(String checkmeInfo) {
                    listener.onGetInfoSuccess(checkmeInfo);
                }

                @Override
                public void onGetInfoFailed(byte errCode) {
                    listener.onGetInfoFailed(errCode);
                }
            });
        }
    }

    @Override
    public void interfacePing(int timeOut, PingThreadListener listener) {
        if(blePresenter != null) {
            blePresenter.interfacePing(new PingBleListener() {
                @Override
                public void onPingBleSuccess() {
                    listener.onPingSuccess();
                }

                @Override
                public void onPingFailed(byte errCode) {
                    listener.onPingFailed(errCode);
                }
            });
        }
    }

    @Override
    public void interfaceInterruptAllThread() {

    }

    @Override
    public boolean isAnyThreadRunning() {
        if(blePresenter != null) {
            return blePresenter.isAnyThreadRunning();
        }
        return false;
    }
}
