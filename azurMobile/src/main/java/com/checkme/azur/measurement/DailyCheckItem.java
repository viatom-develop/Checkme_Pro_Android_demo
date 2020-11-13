package com.checkme.azur.measurement;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.checkme.azur.utils.LogUtils;

/**
 * DailyCheck item, every item contains an ECGInnerItem
 * @author zouhao
 */
public class DailyCheckItem implements Serializable,CommonItem{
	
	private static final long serialVersionUID = 7489723040177818699L;
	
	// Origin data read from files
	private byte[] dataBuf;
	// Measuring date
	private Date date;
	// HR value
	private int hr;
	// ECG image result
	private byte ecgImgResult;
	// SPO2 value
	private byte spo2;
	// PI value
	private float pi;
	// SPO2 image result
	private byte spo2ImgResult;
	// BP flag
	private int bpFlag;
	// BP value
	private int bp;
	// BP image result
	private byte bpImgResult;
	// Whether contains voice
	private byte voiceFlag;
	// Whether the internal item has been downloaded
	private boolean downloaded = false;
	// Whether the internal item is downloading
	private boolean bDownLoading = false;
	// The internal item, contains ECG data
	private ECGInnerItem innerItem;
	
	public DailyCheckItem(byte[] inBuf){
		
		if(inBuf.length != MeasurementConstant.DAILYCHECK_ITEM_LENGTH){
			LogUtils.d("DLC buf length error");
			return;
		}
		dataBuf = inBuf;
		Calendar calendar = new GregorianCalendar((inBuf[0]&0xFF)+((inBuf[1]&0xFF)<<8)
				,(inBuf[2]&0xFF)-1,inBuf[3]&0xFF,
				inBuf[4]&0xFF,inBuf[5]&0xFF,inBuf[6]&0xFF);
		date = calendar.getTime();
		hr = (inBuf[7]&0xFF)+((inBuf[8]&0xFF)<<8);
		ecgImgResult = inBuf[9];
		spo2 = inBuf[10];
		pi = ((float)(inBuf[11]&0xFF))/10;
		spo2ImgResult = inBuf[12];
		bpFlag = inBuf[13] & 0xFF;
		//relative number: signed, absolutely number: unsigned
		if (bpFlag==0) {
			bp = inBuf[14];
		}else {
			bp = inBuf[14] & 0xFF;
		}
		bpImgResult = inBuf[15];
		voiceFlag = inBuf[16];
	}
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public int getHR() {
		return hr;
	}
	public void setHR(int hR) {
		hr = hR;
	}
	public byte getSPO2() {
		return spo2;
	}
	public void setSPO2(byte sPO2) {
		spo2 = sPO2;
	}
	public float getPI() {
		return pi;
	}
	public void setPI(float pI) {
		pi = pI;
	}
	public int getBP() {
		return bp;
	}
	public void setBP(int bP) {
		bp = bP;
	}
	public byte getVoiceFlag() {
		return voiceFlag;
	}
	public void setVoiceFlag(byte voiceFlag) {
		this.voiceFlag = voiceFlag;
	}
	public boolean isDownloaded() {
		return downloaded;
	}
	public void setDownloaded(boolean downLoadState) {
		downloaded = downLoadState;
	}
	public byte getECGIMGResult() {
		return ecgImgResult;
	}
	public void setECGIMGResult(byte eCGIMGResult) {
		ecgImgResult = eCGIMGResult;
	}
	public byte getSPO2IMGResult() {
		return spo2ImgResult;
	}
	public void setSPO2IMGResult(byte sPO2IMGResult) {
		spo2ImgResult = sPO2IMGResult;
	}
	public byte getBPIMGResult() {
		return bpImgResult;
	}
	public void setBPIMGResult(byte bPIMGResult) {
		bpImgResult = bPIMGResult;
	}
	
	public boolean isbDownLoading() {
		return bDownLoading;
	}

	public void setbDownLoading(boolean bDownLoading) {
		this.bDownLoading = bDownLoading;
	}
	
	public void setInnerItem(ECGInnerItem innerItem) {
		this.innerItem = innerItem;
	}

	public ECGInnerItem getInnerItem() {
		return innerItem;
	}

	public int getBPFlag() {
		return bpFlag;
	}
	
	public byte[] getDataBuf() {
		return dataBuf;
	}
}
