package com.viatom.bluetooth.classic.listener;

public interface WriteFileListener {
	public static final byte ERR_CODE_NORMAL = -1;
	public static final byte ERR_CODE_TIMEOUT = -2;
	public static final byte ERR_CODE_BUSY = -3;
	public static final byte ERR_CODE_EXP = -4;
	
	/**
	 * Called when writing file to checkme
	 * @param fileName
	 * @param fileType
	 * @param percentage
	 */
	public void onWritePartFinished(String fileName, byte fileType, float percentage); 
	
	/**
	 * Called when the write is completed
	 * @param fileName
	 * @param fileType
	 */
	public void onWriteSuccess(String fileName, byte fileType);
	
	/**
	 * Called when the write failed
	 * @param fileType
	 * @param errCode
	 */
	public void onWriteFailed(String fileName, byte fileType, byte errCode);
}
