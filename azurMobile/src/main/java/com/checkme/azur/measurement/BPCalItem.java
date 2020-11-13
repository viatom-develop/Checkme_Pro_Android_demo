package com.checkme.azur.measurement;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.checkme.azur.utils.LogUtils;


/**
 * BP calibration item
 * @author zouhao
 */
public class BPCalItem {
	
	private int id;
	private byte calType = MeasurementConstant.BP_TYPE_NONE; //Calibration Type
	private Date calDate;
	private byte sbp;
	
	public BPCalItem() {
		// TODO Auto-generated constructor stub
	}
	
	public BPCalItem(byte[] buf) {
		// TODO Auto-generated constructor stub
		if (buf.length!=MeasurementConstant.BPCAL_ITEM_LENGHT) {
			LogUtils.d("BpCalItem length error");
			return;
		}
		//Relative calibration
		calType = MeasurementConstant.BP_TYPE_RE;
		
		id = buf[0];
		Calendar calendar = new GregorianCalendar((buf[1] & 0xFF)
				+ ((buf[2] & 0xFF) << 8)
		, (buf[3] & 0xFF) - 1, buf[4] & 0xFF,
		buf[5] & 0xFF, buf[6] & 0xFF, buf[7] & 0xFF);
		calDate = calendar.getTime();
		
		if(buf[11]!=0){
			//Absolutely calibration
			calType = MeasurementConstant.BP_TYPE_ABS;
			sbp = buf[11];
		}
	}
	
	public Date getCalDate() {
		return calDate;
	}

	public byte getCalType() {
		return calType;
	}

	public byte getSbp() {
		return sbp;
	}

	public int getId() {
		return id;
	}
	
}
