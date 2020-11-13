package com.checkme.azur.measurement;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.checkme.azur.utils.LogUtils;

/**
 * Pedometer Item
 * @author zouhao
 */
public class PedItem implements CommonItem{
	
	// Origin data read from files
	private byte[] dataBuf;
	// Measuring date
	private Date date;
	// Walk steps
	private int steps;
	// Walk distance
	private double distance;
	// Walk speed
	private double speed;
	// Consume calories
	private double calorie;
	// Reduce fat
	private double fat;
	// Total time 
	private int totalTime;
	
	public PedItem(byte[] buf) {
		
		if(buf.length!=MeasurementConstant.PED_ITEM_LENGTH){
			LogUtils.d("Ped buf length error");
			return;
		}
		dataBuf = buf;
		Calendar calendar = new GregorianCalendar((buf[0]&0xFF)+((buf[1]&0xFF)<<8)
				,(buf[2]&0xFF)-1,buf[3]&0xFF,
				buf[4]&0xFF,buf[5]&0xFF,buf[6]&0xFF);
		date = calendar.getTime();
		steps = ((buf[7]&0xFF)<<0) + ((buf[8]&0xFF)<<8) 
				+ ((buf[9]&0xFF)<<16) + ((buf[10]&0xFF)<<24);
		distance = ((buf[11]&0xFF)<<0) + ((buf[12]&0xFF)<<8) 
				+ ((buf[13]&0xFF)<<16) + ((buf[14]&0xFF)<<24);
		distance /= 100;
		speed = ((buf[15]&0xFF)<<0) + ((buf[16]&0xFF)<<8) 
				+ ((buf[17]&0xFF)<<16) + ((buf[18]&0xFF)<<24);
		speed /= 10;
		calorie = ((buf[19]&0xFF)<<0) + ((buf[20]&0xFF)<<8) 
				+ ((buf[21]&0xFF)<<16) + ((buf[22]&0xFF)<<24);
		calorie /= 100;
		fat = ((buf[23]&0xFF)<<0) + ((buf[24]&0xFF)<<8);
		fat /= 100;
		totalTime = ((buf[25]&0xFF)<<0) + ((buf[26]&0xFF)<<8) 
				+ ((buf[27]&0xFF)<<16) + ((buf[28]&0xFF)<<24);
	}
	
	public Date getDate() {
		return date;
	}
	public int getSteps() {
		return steps;
	}
	public double getDistance() {
		return distance;
	}
	public double getSpeed() {
		return speed;
	}
	public double getCalorie() {
		return calorie;
	}
	public double getFat() {
		return fat;
	}
	public int getTotalTime() {
		return totalTime;
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
