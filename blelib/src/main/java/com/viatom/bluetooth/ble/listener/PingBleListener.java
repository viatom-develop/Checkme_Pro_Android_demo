package com.viatom.bluetooth.ble.listener;

public interface PingBleListener {

    byte ERR_CODE_NORMAL = -1;
    byte ERR_CODE_TIMEOUT = -2;
    byte ERR_CODE_BUSY = -3;
    byte ERR_CODE_EXP = -4;

    /**
     *  Called when ping successfully
     */
    void onPingBleSuccess();

    /**
     * Called when ping failed
     * @param errCode
     */
    void onPingFailed(byte errCode);
}
