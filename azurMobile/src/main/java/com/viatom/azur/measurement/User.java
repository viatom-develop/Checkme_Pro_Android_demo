package com.viatom.azur.measurement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.viatom.azur.utils.LogUtils;



/**
 * User class, contains user information and measurement item
 * @author zouhao
 */
public class User{
	
	// User information
	private UserInfo userInfo;
	// BP calibration item
	private BPCalItem bpCalItem = new BPCalItem();
	// DailyCheck item list
	private ArrayList<DailyCheckItem> dlcList = new ArrayList<DailyCheckItem>();
	// ECG item list
	private ArrayList<ECGItem> ecgList = new ArrayList<ECGItem>();
	// SPO2 item list
	private ArrayList<SPO2Item> spo2list = new ArrayList<SPO2Item>();
	// Thermometer item list
	private ArrayList<TempItem> tempList = new ArrayList<TempItem>();
	// SleepMonitor item list
	private ArrayList<SLMItem> slmList = new ArrayList<SLMItem>();
	// Pedometer item list
	private ArrayList<PedItem> pedList = new ArrayList<PedItem>();
	
	public User() {
		userInfo = new UserInfo();
	}
	
	public User(UserInfo userInfo) {
		super();
		this.userInfo = userInfo;
	}
	
	public UserInfo getUserInfo() {
		return userInfo;
	}

	public ArrayList<DailyCheckItem> getDlcList() {
		return dlcList;
	}
	
	public ArrayList<ECGItem> getEcgList() {
		return ecgList;
	}

	public ArrayList<SPO2Item> getSpo2list() {
		return spo2list;
	}

	public ArrayList<TempItem> getTempList() {
		return tempList;
	}

	// bp item list
	private ArrayList<BPItem> bpList = new ArrayList<BPItem>();

	public ArrayList<SLMItem> getSlmList() {
		return slmList;
	}
	public ArrayList<BPItem> getBpList() {
		return bpList;
	}

	public BPCalItem getBpCalItem() {
		return bpCalItem;
	}
	
	public ArrayList<PedItem> getPedList() {
		return pedList;
	}
	
	/**
	 * Get measurement item list,
	 * such as dlcList, ecgList...
	 * @param type List type want to get
	 * @return Item list
	 */
	public ArrayList<?> getList(int type){
		switch (type) {
		case MeasurementConstant.CMD_TYPE_DLC:
			return dlcList;
		case MeasurementConstant.CMD_TYPE_ECG_LIST:
			return ecgList;
		case MeasurementConstant.CMD_TYPE_SPO2:
			return spo2list;
		case MeasurementConstant.CMD_TYPE_TEMP:
			return tempList;
		case MeasurementConstant.CMD_TYPE_PED:
			return pedList;
		case MeasurementConstant.CMD_TYPE_SLM_LIST:
			return slmList;
		default:
			return null;
		}
	}
	
	public void setBpCalItem(BPCalItem bpCalItem) {
		this.bpCalItem = bpCalItem;
	}

	public void addDLCList(List<DailyCheckItem> items) {
		dlcList.addAll(items);
	}
	
	public void addECGList(List<ECGItem> items) {
		ecgList.addAll(items);
	}
	
	public void addSPO2List(List<SPO2Item> items) {
		spo2list.addAll(items);
	}

	public void addBPList(List<BPItem> items) {
		bpList.addAll(items);
	}
	
	public void addTempList(List<TempItem> items) {
		tempList.addAll(items);
	}
	
	public void addSLMList(List<SLMItem> items) {
		slmList.addAll(items);
	}
	
	public void addPedList(List<PedItem> items) {
		pedList.addAll(items);
	}
	
	/**
	 * @author zouhao
	 * User information class
	 */
	public static class UserInfo{
		
		private byte id = 1, ico = 1, gender;
		private char[] name = new char[16];
		private Date birthDate;
		private int weight, height;
		
		public UserInfo() {
			id = 1;
			ico = 1;
		}
		
		public UserInfo(byte[] buf){
			if (buf == null || buf.length != MeasurementConstant.USER_ITEM_LENGTH) {
				LogUtils.d("user buf length err");
				return;
			}
			id = buf[0];
			for (int i = 0; i < name.length; i++)
				name[i] = (char)buf[i + 1];
			ico = buf[17];
			gender = buf[18];
			weight = (buf[23]&0xFF)+((buf[24]&0xFF)<<8);
			height = (buf[25]&0xFF)+((buf[26]&0xFF)<<8);
			
			Calendar calendar = new GregorianCalendar((buf[19] & 0xFF) 
					+ (((buf[20] & 0xFF)<<8)), (buf[21] & 0xFF) - 1
					, buf[22] & 0xFF);
			birthDate = calendar.getTime();
		}
		
		public byte getID() {
			return id;
		}
		public byte getICO() {
			return ico;
		}
		public byte getGender() {
			return gender;
		}
		public int getWeight() {
			return weight;
		}
		public int getHeight() {
			return height;
		}
		public Date getBirthDate() {
			return birthDate;
		}

		public String getName() {
			if (name == null) {
				return "";
			}
			return String.valueOf(name);
		}
	}
}
