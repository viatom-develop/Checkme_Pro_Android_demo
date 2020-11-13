package com.viatom.bluetooth.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.List;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.Single;


public class RxBleHelper {

    private static volatile RxBleHelper instance;
    private RxBleClient rxBleClient;

    private RxBleHelper(Context context) {
        rxBleClient = RxBleClient.create(context.getApplicationContext());
    }

    public static RxBleHelper newInstance(Context context) {
        if(instance == null) {
            synchronized (RxBleHelper.class) {
                if(instance == null) {
                    instance = new RxBleHelper(context);
                    return instance;
                }
            }
        }
        return instance;
    }

    public  RxBleClient.State getState() {
        return rxBleClient.getState();
    }

    public Observable<RxBleClient.State> observeStateChanges() {
        return rxBleClient.observeStateChanges();
    }

    public Observable<ScanResult> scanDevice() {
        return rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // change if needed
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
                        .build()
        ).filter(scanResult ->
            !TextUtils.isEmpty(scanResult.getBleDevice().getName())
        );
    }

    public Observable<ScanResult> scanDevice(final String filterKeywords) {
        return rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // change if needed
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
                        .build()
        ).filter(scanResult -> !TextUtils.isEmpty(scanResult.getBleDevice().getName())
                && scanResult.getBleDevice().getName().startsWith(filterKeywords));
    }

    public Observable<RxBleConnection> connect(@NonNull String macAddress) {
        RxBleDevice device = rxBleClient.getBleDevice(macAddress);
        return device.establishConnection(false);
    }

    public Observable<RxBleConnection> connect(@NonNull RxBleDevice device ) {
        return device.establishConnection(false);
    }

    public Observable<RxBleConnection.RxBleConnectionState> observeConnectionStateChanges(@NonNull RxBleDevice device) {
        return device.observeConnectionStateChanges();
    }

    public Single<BluetoothGattService> discoverServices(@NonNull RxBleConnection mRxBleConnection, String serviceUUID) {
        return mRxBleConnection.discoverServices()
                .flatMap(mRxBleDeviceServices -> mRxBleDeviceServices.getService(UUID.fromString(serviceUUID)));
    }

    public Single<BluetoothGattCharacteristic> getCharacteristic(@NonNull RxBleConnection mRxBleConnection, String serviceUUID, String uuid) {
        return mRxBleConnection.discoverServices()
                .flatMap(mRxBleDeviceServices -> mRxBleDeviceServices.getCharacteristic(UUID.fromString(serviceUUID), UUID.fromString(uuid)));
    }

    public Observable<byte[]> setupNotification(RxBleDevice mCurrentRxBleDevice, BluetoothGattCharacteristic characteristic) {
        return mCurrentRxBleDevice.establishConnection(false)
                .flatMap(mRxConnection -> mRxConnection.setupNotification(characteristic))
                .flatMap(notificationObservable -> notificationObservable);

    }

    public Observable<byte[]> setupNotification(RxBleConnection mRxConnection, BluetoothGattCharacteristic characteristic) {
        return mRxConnection.setupNotification(characteristic)
                .flatMap(notificationObservable -> notificationObservable);

    }

    public Observable readCharacteristic(RxBleDevice mCurrentRxBleDevice, BluetoothGattCharacteristic readCharacteristic) {
        return mCurrentRxBleDevice.establishConnection(false)
                .flatMapSingle(mRxConnection -> mRxConnection.readCharacteristic(readCharacteristic));
    }

    public Observable<byte[]> writeCharacteristic(RxBleDevice mCurrentRxBleDevice, BluetoothGattCharacteristic writeCharacteristic,
            byte[] bytesToWrite) {
        return mCurrentRxBleDevice.establishConnection(false)
                .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(writeCharacteristic, bytesToWrite));
    }

    public Observable<byte[]> writeCharacteristic(RxBleDevice mCurrentRxBleDevice, UUID writeUUID,
                                                  byte[] bytesToWrite) {
        return mCurrentRxBleDevice.establishConnection(false)
                .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(writeUUID, bytesToWrite));
    }

    public Observable<byte[]> writeCharacteristic(RxBleConnection rxBleConnection, BluetoothGattCharacteristic writeCharacteristic,
          byte[] bytesToWrite) {
        return rxBleConnection.writeCharacteristic(writeCharacteristic, bytesToWrite).toObservable();
    }

    public Single<byte[]> writeCharacteristic(RxBleConnection rxBleConnection, UUID writeUUID, byte[] bytesToWrite) {
        return rxBleConnection.writeCharacteristic(writeUUID, bytesToWrite);
    }

    public Observable<byte[]> longWriteCharacteristic(RxBleConnection rxBleConnection, UUID writeUUID, byte[] bytesToWrite) {
        return rxBleConnection.createNewLongWriteBuilder()
                .setCharacteristicUuid(writeUUID)
                .setBytes(bytesToWrite)
                .build();
    }

    public RxBleDevice getRxBleDevice(String mAddrsss) {
        return rxBleClient.getBleDevice(mAddrsss);
    }


}
