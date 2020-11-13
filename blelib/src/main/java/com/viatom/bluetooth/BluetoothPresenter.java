package com.viatom.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.polidea.rxandroidble2.RxBleClient;
import com.viatom.bluetooth.ble.RxBleHelper;
import com.viatom.bluetooth.callback.OnDeviceScanResult;
import com.viatom.bluetooth.callback.OnScanCallback;
import com.viatom.bluetooth.classic.RxBluetooth;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;


public class BluetoothPresenter {

    private OnScanCallback mOnScanCallback;

    private Disposable scanDisposable;
    private OnDeviceScanResult mOnDeviceScanResult;
    private boolean interceptDiscovery = false;

    private static BluetoothPresenter instance;
    private RxBleHelper mRxBleHelper;
    private RxBluetooth mRxBluetooth;

    Disposable disposable;

    private BluetoothPresenter(Context context) {
        mRxBleHelper = RxBleHelper.newInstance(context.getApplicationContext());
        mRxBluetooth = new RxBluetooth(context.getApplicationContext());
    }

    public static BluetoothPresenter newInstance(Context context) {
        if(instance == null) {
            synchronized (BluetoothPresenter.class) {
                if(instance == null) {
                    instance = new BluetoothPresenter(context);
                    return instance;
                }
            }
        }
        return instance;
    }

    public boolean isInterceptDiscovery() {
        return interceptDiscovery;
    }

    public void setInterceptDiscovery(boolean interceptDiscovery) {
        this.interceptDiscovery = interceptDiscovery;
    }

    public void setOnScanCallback(OnScanCallback mOnScanCallback) {
        this.mOnScanCallback = mOnScanCallback;
    }

    public void setOnDeviceScanResult(OnDeviceScanResult mOnDeviceScanResult) {
        this.mOnDeviceScanResult = mOnDeviceScanResult;
    }

    public void startBleJobs() {
        RxBleClient.State state = mRxBleHelper.getState();
        switch (state) {
            case READY:
                // everything should work
//                startBleScan();
                if(mOnScanCallback != null) {
                    mOnScanCallback.onReady();
                }
                break;
            default:
                if(mOnScanCallback != null) {
                    mOnScanCallback.onHandleScanPrepareState(state);
                }
                break;
        }
    }

    public void scanBluetooth(Predicate<BluetoothDevice> predicate) {
        if(mOnScanCallback != null) {
            mOnScanCallback.onScanStart();
        }

        scanDisposable = startScanBluetooth()
                .distinct()
                .filter(predicate)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bluetoothDevice -> {
                    if(mOnDeviceScanResult != null) {
                        Logger.d(BluetoothPresenter.class, "device found == " + bluetoothDevice.getName());
                        Logger.d(BluetoothPresenter.class, "device found == " + bluetoothDevice.getAddress());
                        mOnDeviceScanResult.onDeviceFound(bluetoothDevice);
                    }
                });
    }

    public Observable<String> observeDiscovery() {
        return mRxBluetooth.observeDiscovery()
                .filter(s -> s.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }


    private Observable<BluetoothDevice> startScanBluetooth() {
        if(!mRxBluetooth.isDiscovering()) {
            mRxBluetooth.startDiscovery();
        } else {
           disposable =  mRxBluetooth.observeDiscovery()
                    .filter(s -> s.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
                    .subscribe(s -> {
                        Logger.d("BluetoothPresenter", "BluetoothAdapter.ACTION_DISCOVERY_FINISHED");
                        if(!interceptDiscovery) {
                            mRxBluetooth.startDiscovery();
                        } else {
                            if(!disposable.isDisposed()) {
                                disposable.dispose();
                            }
                        }
                    });
            mRxBluetooth.cancelDiscovery();
        }
        return mRxBluetooth.observeDevices();
    }

    public boolean isDiscovering() {
        return mRxBluetooth.isDiscovering();
    }

    public void cancelDiscovery() {
        if(mRxBluetooth.isDiscovering()) {
            interceptDiscovery = true;
            mRxBluetooth.cancelDiscovery();
        }
    }

    public void enableDiscovery() {
        interceptDiscovery = false;
    }

    public void connect(@NonNull BluetoothDevice bluetoothDevice) {
        if(scanDisposable != null && !scanDisposable.isDisposed()) {
            scanDisposable.dispose();
        }
        if(mOnScanCallback != null) {
            mOnScanCallback.onScanFinished();
        }
    }

    public void offline() {
        if(scanDisposable != null && !scanDisposable.isDisposed()) {
            scanDisposable.dispose();
        }
        if(mOnScanCallback != null) {
            mOnScanCallback.onScanFinished();
        }

        if(mOnScanCallback != null) {
            mOnScanCallback.intent2("");
        }
    }

    public void onDestroy() {
        if(scanDisposable != null && !scanDisposable.isDisposed()) {
            scanDisposable.dispose();
        }
    }

}
