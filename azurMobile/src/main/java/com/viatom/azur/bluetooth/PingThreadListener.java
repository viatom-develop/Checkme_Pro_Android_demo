package com.viatom.azur.bluetooth;

public interface PingThreadListener {

	byte ERR_CODE_NORMAL = -1;
	byte ERR_CODE_TIMEOUT = -2;
	byte ERR_CODE_BUSY = -3;
	byte ERR_CODE_EXP = -4;
	byte ERR_CODE_MONITOR = -5;
	
	/**
	 *  Called when ping successfully
	 */
	void onPingSuccess();
	
	/**
	 * Called when ping failed
	 * @param errCode
	 */
	void onPingFailed(byte errCode);
}
