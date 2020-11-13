package com.checkme.azur.monitor.bt;

/**
 *	ReadThread Listener
 * @author zouhao
 */
public interface ReadThreadListener {
	
	public static final byte ERR_CODE_NORMAL = -1;
	public static final byte ERR_CODE_TIMEOUT = -2;
	public static final byte ERR_CODE_BUSY = -3;
	public static final byte ERR_CODE_EXP = -4;
	
	public void onReadThreadFinished(byte[] buf);
	
	public void onReadThreadFailed(byte errCode);
}