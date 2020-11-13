package com.viatom.bluetooth.classic.callback;


public interface OnBtConnectResult {
    void onBtConnected();
    void onBtConnectedError(Throwable throwable);
}
