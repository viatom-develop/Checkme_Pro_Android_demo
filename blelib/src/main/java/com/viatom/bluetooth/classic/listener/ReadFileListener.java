package com.viatom.bluetooth.classic.listener;

public interface ReadFileListener {
	
	public static final byte ERR_CODE_NORMAL = -1;
	public static final byte ERR_CODE_TIMEOUT = -2;
	public static final byte ERR_CODE_BUSY = -3;
	public static final byte ERR_CODE_EXP = -4;
	
	/**
	 * Called when downloading file from bluetooth
	 * @param fileName
	 * @param fileType
	 * @param percentage
	 */
	public void onReadPartFinished(String fileName, byte fileType, float percentage); 
	
	/**
	 * Called when the download is completed
	 * @param fileName
	 * @param fileType
	 * @param fileBuf
	 */
	public void onReadSuccess(String fileName, byte fileType, byte[] fileBuf);
	
	/**
	 * Called when the download is failed
	 * @param fileName
	 * @param fileType
	 * @param errCode
	 */
	public void onReadFailed(String fileName, byte fileType, byte errCode);
}
