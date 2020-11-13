package com.viatom.bluetooth.ble.listener;

public interface ParaSyncBleListener {
    byte ERR_CODE_NORMAL = -1;
    byte ERR_CODE_TIMEOUT = -2;
    byte ERR_CODE_BUSY = -3;
    byte ERR_CODE_EXP = -4;

    /**
     *  Called when ping successfully
     */
    void onParaSyncBleSuccess();

    /**
     * Called when ping failed
     * @param errCode
     */
    void onParaSyncFailed(byte errCode);
}
