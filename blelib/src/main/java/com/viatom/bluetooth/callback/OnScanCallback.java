package com.viatom.bluetooth.callback;

import com.polidea.rxandroidble2.RxBleClient;

public interface OnScanCallback {
    void onHandleScanPrepareState(RxBleClient.State state);
    void onReady();
    void onScanStart();
    void onScanFinished();

    void intent2(String deviceName);
}
