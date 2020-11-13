package com.checkme.bluetooth.ble.listener;

public interface GetInfoBleListener {
    public static final byte ERR_CODE_NORMAL = -1;
    public static final byte ERR_CODE_TIMEOUT = -2;
    public static final byte ERR_CODE_BUSY = -3;
    public static final byte ERR_CODE_EXP = -4;

    /**
     *  Called when get info successfully
     * @param checkmeInfo
     */
    public void onGetInfoBleSuccess(String checkmeInfo);

    /**
     * Called when get info failed
     * @param errCode
     */
    public void onGetInfoFailed(byte errCode);
}
