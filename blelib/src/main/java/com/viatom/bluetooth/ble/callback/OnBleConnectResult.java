package com.viatom.bluetooth.ble.callback;

public interface OnBleConnectResult {
    void onBleConnected();
    void onBleConnectedError(Throwable throwable);
}
