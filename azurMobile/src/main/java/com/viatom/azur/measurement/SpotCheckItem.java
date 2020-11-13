package com.viatom.azur.measurement;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.viatom.azur.utils.LogUtils;

/**
 * Spot check item
 * @author zouhao
 */
public class SpotCheckItem implements Serializable, CommonItem{
	
	/**
	 * Measurement mode
	 */
	public static final int MODE_ECG = 0x01;
	public static final int MODE_SPO2 = 0x02;
	public static final int MODE_TEMP = 0x04;
	
	private static final long serialVersionUID = -3929587803521396309L;
	// Origin data read from files
	private byte[] dataBuf;
	// Measuring date
	private Date date;
	
	private int func;
	private int hr;
	private int qrs;
	private float st;
	private int ecgResultIndex;
	private int ecgImgResult;
	private int spo2;
	private float pi;
	private int spo2ImgResult;
	private float temp;
	private int tempImgResult;
	private int voiceFlag;
	private boolean downloaded;
	private boolean downloading;
	private ECGInnerItem innerItem;
	
	public SpotCheckItem(byte[] buf) {
		if (buf == null || buf.length != MeasurementConstant.SPOT_CHECK_ITEM_LENGTH) {
			LogUtils.d("spot check buf length err");
			return;
		}
		dataBuf = buf;
		
		Calendar calendar = new GregorianCalendar((buf[0] & 0xFF)
				+ ((buf[1] & 0xFF) << 8)
		, (buf[2] & 0xFF) - 1, buf[3] & 0xFF,
		buf[4] & 0xFF, buf[5] & 0xFF, buf[6] & 0xFF);
		date = calendar.getTime();
		
		func = (buf[7] & 0xFF) + ((buf[8] & 0xFF) << 8);
		//ecg
		hr = (buf[9] & 0xFF) + ((buf[10] & 0xFF) << 8);
		qrs = (buf[11] & 0xFF) + ((buf[12] & 0xFF) << 8);
		st = (short)((buf[13] & 0xFF) + ((buf[14] & 0xFF) << 8));
		ecgResultIndex = buf[15];
		ecgImgResult = buf[16];
		//spo2
		spo2 = buf[17];
		pi = ((float)buf[18])/10;
		spo2ImgResult = buf[19];
		//temp
		temp = ((float)((buf[20] & 0xFF) + ((buf[21] & 0xFF) << 8)))/10;
		tempImgResult = buf[22];
		//voice
		voiceFlag = buf[23];
	}


	public byte[] getDataBuf() {
		return dataBuf;
	}


	public Date getDate() {
		return date;
	}


	public int getFunc() {
		return func;
	}


	public int getHr() {
		return hr;
	}


	public int getQrs() {
		return qrs;
	}


	public float getSt() {
		return st;
	}


	public int getEcgResultIndex() {
		return ecgResultIndex;
	}


	public int getEcgImgResult() {
		return ecgImgResult;
	}


	public int getSpo2() {
		return spo2;
	}


	public float getPi() {
		return pi;
	}


	public int getSpo2ImgResult() {
		return spo2ImgResult;
	}


	public float getTemp() {
		return temp;
	}


	public int getTempImgResult() {
		return tempImgResult;
	}


	public int getVoiceFlag() {
		return voiceFlag;
	}


	public boolean isDownloaded() {
		return downloaded;
	}

	public void setDownloaded(boolean downloaded) {
		this.downloaded = downloaded;
	}


	public boolean isDownloading() {
		return downloading;
	}


	public void setDownloading(boolean downloading) {
		this.downloading = downloading;
	}


	public ECGInnerItem getInnerItem() {
		return innerItem;
	}


	public void setInnerItem(ECGInnerItem innerItem) {
		this.innerItem = innerItem;
	}
	
}
