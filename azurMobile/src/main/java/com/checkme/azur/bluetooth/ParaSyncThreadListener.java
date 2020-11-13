package com.checkme.azur.bluetooth;

public interface ParaSyncThreadListener {

	byte ERR_CODE_NORMAL = -1;
	byte ERR_CODE_TIMEOUT = -2;
	byte ERR_CODE_BUSY = -3;
	byte ERR_CODE_EXP = -4;
	
	/**
	 *  Called when ping successfully
	 */
    void onParaSyncSuccess();
	
	/**
	 * Called when ping failed
	 * @param errCode
	 */
    void onParaSyncFailed(byte errCode);
}
