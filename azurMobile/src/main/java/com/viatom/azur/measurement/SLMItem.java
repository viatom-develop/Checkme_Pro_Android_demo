package com.viatom.azur.measurement;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Sleep Monitor item
 * @author zouhao
 */
public class SLMItem implements Serializable,CommonItem{
	
	private static final long serialVersionUID = 6349962979650581024L;
	
	// Origin data read from files
	private byte[] dataBuf;
	// Start time of measurement
	private Date startTime;
	// Total time of measurement
	private int totalTime;
	// The duration of low oxygen occurs
	private int lowOxygenTime;
	// The number of low oxygen occurs
	private int lowOxygenNum; 
	// The lowest oxygen value of this measurement
	private int lowestOxygen;
	// The average oxygen value of this measurement
	private int averageOxygen;
	// Image result, smile or cry
	private byte imgResult;
	// Whether the internal item has been downloaded
	private boolean downloaded;
	// Whether the internal item is downloading
	private boolean bDownloading;
	// The internal item
	private SLMInnerItem innerItem;
	
	public SLMItem(byte[] buf){
		if (buf.length != MeasurementConstant.SLM_LIST_ITEM_LENGTH) {
			return;
		}
		dataBuf = buf;
		Calendar calendar = new GregorianCalendar((buf[0] & 0xFF)
				+ ((buf[1] & 0xFF) << 8)
		, (buf[2] & 0xFF) - 1, buf[3] & 0xFF,
				buf[4] & 0xFF, buf[5] & 0xFF, buf[6] & 0xFF);
		startTime = calendar.getTime();
		totalTime = (buf[7] & 0xFF) + ((buf[8] & 0xFF) << 8) 
				+ ((buf[9] & 0xFF) << 16) + ((buf[10] & 0xFF) << 24);
		
		lowOxygenTime = (buf[11] & 0xFF)
				+ ((buf[12] & 0xFF) << 8);
		lowOxygenNum = (buf[13] & 0xFF)+((buf[14] & 0xFF) << 8);
		lowestOxygen = (buf[15] & 0xFF);
		averageOxygen = buf[16];
		imgResult=buf[17];
		
	}
	
	public Date getStartTime() {
		return startTime;
	}
	
	public int getLowOxygenTime() {
		return lowOxygenTime;
	}
	public int getTotalTime() {
		return totalTime;
	}

	public int getLowOxygenNum() {
		return lowOxygenNum;
	}
	
	public int getLowestOxygen() {
		return lowestOxygen;
	}
	
	public int getAverageOxygen() {
		return averageOxygen;
	}
	
	public byte getImgResult() {
		return imgResult;
	}

	public SLMInnerItem getInnerItem() {
		return innerItem;
	}

	public boolean isDownloaded() {
		return downloaded;
	}

	public void setDownloaded(boolean downloadState) {
		this.downloaded = downloadState;
	}

	public boolean isbDownloading() {
		return bDownloading;
	}

	public void setbDownloading(boolean bDownloading) {
		this.bDownloading = bDownloading;
	}
	
	public void setInnerItem(SLMInnerItem innerItem) {
		this.innerItem = innerItem;
	}

	@Override
	public Date getDate() {
		// TODO Auto-generated method stub
		return startTime;
	}
	
	@Override
	public byte[] getDataBuf() {
		return dataBuf;
	}
}
