package com.checkme.bluetooth.callback;

import android.bluetooth.BluetoothDevice;

public interface OnDeviceScanResult {
    void onDeviceFound(BluetoothDevice bluetoothDevice);
    void onDeviceScanError(Throwable throwable);
}
