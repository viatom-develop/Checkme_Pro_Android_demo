package com.checkme.azur.measurement;

import java.util.Date;

/**
 * @author zouhao
 * Common data item interface
 */
public interface CommonItem {
	
	/**
	 * Get measurement date
	 * @return Date
	 */
	public Date getDate();
	
	/**
	 * Get row data
	 * @return Data buffer
	 */
	public byte[] getDataBuf();
	
	/**
	 * Whether the inner item is downloaded
	 * @return
	 */
	public boolean isDownloaded();
}
