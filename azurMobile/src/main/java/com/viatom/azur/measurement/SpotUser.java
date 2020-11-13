package com.viatom.azur.measurement;

import java.util.ArrayList;
import java.util.List;

import com.viatom.azur.utils.LogUtils;

/**
 * Spot user class
 * Contains SpotUserInfo and SpotCheckItem list
 * @author zouhao
 */
public class SpotUser {
	
	private SpotUserInfo userInfo;
	private ArrayList<SpotCheckItem> spotCheckList = new ArrayList<SpotCheckItem>();
	
	public SpotUser() {
		super();
		userInfo = new SpotUserInfo();
	}
	public SpotUser(SpotUserInfo userInfo) {
		super();
		this.userInfo = userInfo;
	}

	public SpotUserInfo getUserInfo() {
		return userInfo;
	}

	public ArrayList<SpotCheckItem> getSpotCheckList() {
		return spotCheckList;
	}

	public ArrayList<?> getList(int type) {
		if (type == MeasurementConstant.CMD_TYPE_SPOT) {
			return spotCheckList;
		}
		return null;
	}
	
	public void addSpotList(List<SpotCheckItem> items) {
		spotCheckList.addAll(items);
	}
	
	/**
	 * Spot user information class
	 * @author zouhao
	 */
	public static class SpotUserInfo {
		
		public int id;
		private String patientID;
		public String name;
		private int gender;
		private int unit;
		private int height;
		private int weight;
		private int age;
		public SpotUserInfo(){

		}
		
		public SpotUserInfo(byte[] buf){
			if (buf == null || buf.length != MeasurementConstant.SPOT_USER_ITEM_LENGTH) {
				LogUtils.d("spot user buf length err");
				return;
			}
			id = buf[0];
			//Patient id
			patientID = new String();
			for (int i = 0; i < 32; i++) {
				if (buf[i+1] != 0) {
					patientID += (char)buf[i+1];
				}
			}
			//User name
			name = new String();
			for (int i = 0; i < 32; i++) {
				if (buf[i+32] != 0) {
					name += (char)buf[32+i];
				}
			}
			//Others
			gender = buf[65];
			unit = buf[66];
			height = (buf[67]&0xFF)+((buf[68]&0xFF)<<8);
			weight = (buf[69]&0xFF)+((buf[70]&0xFF)<<8);
			age = buf[71];
		}

		public int getId() {
			return id;
		}

		public String getPatientID() {
			return patientID;
		}

		public String getName() {
			return name;
		}

		public int getGender() {
			return gender;
		}

		public int getUnit() {
			return unit;
		}

		public int getHeight() {
			return height;
		}

		public int getWeight() {
			return weight;
		}

		public int getAge() {
			return age;
		}
		
	}
	
	
}
