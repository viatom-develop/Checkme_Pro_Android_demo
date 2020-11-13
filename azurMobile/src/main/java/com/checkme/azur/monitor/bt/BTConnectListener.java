package com.checkme.azur.monitor.bt;

public interface BTConnectListener{
	
	public static final byte ERR_CODE_NORMAL = -1;
	public static final byte ERR_CODE_TIMEOUT = -2;
	public static final byte ERR_CODE_EXP = -3;

	/**
	 * Called when Connect successfully
	 */
	public void onTraceConnectSuccess();

	/**
	 * Called when Connect failed
	 * @param errCode
	 */
	public void onTraceConnectFailed(byte errCode);
}
