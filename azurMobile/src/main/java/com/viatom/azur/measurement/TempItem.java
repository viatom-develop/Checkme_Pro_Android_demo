package com.viatom.azur.measurement;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.viatom.azur.utils.LogUtils;

/**
 * Temperature item, used for thermometer measurement
 * @author zouhao
 */
public class TempItem implements CommonItem{
	
	// Origin data read from files
	private byte[] dataBuf;
	// Measuring date
	private Date date;
	// Measuring mode, body or thing
	private byte measuringMode;
	// Measuring result, in Celsius
	private float result;
	// Image result, smile or cry
	private byte imgResult;
	
	public TempItem(byte[] buf){
		
		if(buf.length!=MeasurementConstant.TEMP_ITEM_LENGHT){
			LogUtils.d("Temp buf length error");
			return;
		}
		dataBuf = buf;
		Calendar calendar = new GregorianCalendar((buf[0] & 0xFF)
				+ ((buf[1] & 0xFF) << 8), (buf[2] & 0xFF) - 1, buf[3] & 0xFF,
				buf[4] & 0xFF, buf[5] & 0xFF, buf[6] & 0xFF);
		date = calendar.getTime();
		measuringMode = buf[7];
		result = (float) ((float) ((buf[8] & 0xFF) + ((buf[9] & 0xFF) << 8))) / 10;
		imgResult = buf[10];
	}
	
	public Date getDate() {
		return date;
	}
	public byte getMeasuringMode() {
		return measuringMode;
	}
	public float getResult() {
		return result;
	}
	public byte getImgResult() {
		return imgResult;
	}
	
	public byte[] getDataBuf() {
		return dataBuf;
	}
	
	@Override
	public boolean isDownloaded() {
		// TODO Auto-generated method stub
		return true;
	}
}
