package com.viatom.bluetooth.callback;

import com.github.ivbaranov.rxbluetooth.events.ConnectionStateEvent;
import com.polidea.rxandroidble2.RxBleConnection;

public interface OnConnectCallback {
    void onConnectStateChange(ConnectionStateEvent connectionStateEvent);
    void onBleConnectStateChange(RxBleConnection.RxBleConnectionState state);
    void onConnectedError(Throwable throwable);
}
