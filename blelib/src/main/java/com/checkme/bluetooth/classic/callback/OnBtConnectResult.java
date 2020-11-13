package com.checkme.bluetooth.classic.callback;


public interface OnBtConnectResult {
    void onBtConnected();
    void onBtConnectedError(Throwable throwable);
}
