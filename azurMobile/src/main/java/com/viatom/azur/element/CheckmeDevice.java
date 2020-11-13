package com.viatom.azur.element;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import com.viatom.azur.bluetooth.GetInfoAckPkg;
import com.viatom.azur.utils.LogUtils;


/**
 * @author zouhao
 *	Checkme主机信息
 */
public class CheckmeDevice {

	private static final String MODE_HOSPITAL = "MODE_HOSPITAL";
	private static final String MODE_HOME = "MODE_HOME";
	private static final String MODE_MULTI = "MODE_MULTI";

	private String region = ""; //地区，如CE,FDA
	private String model = ""; //系列，如6621,6632
	private String hardware = ""; //硬件版本，如8M，16M
	private int software = 0; //软件版本
	private int language = 0; //语言包版本
	private String curLanguage = ""; //当前语言
	private String sn = "";//序列号
	private String spcpVer = "";//通信协议版本
	private String fileVer = "";//文件解析协议版本
	private String name = ""; //设备名
	private String mode = ""; //模式，医院或家庭
	private String branchCode = "";
	private boolean isBranchCodeAvailable = false;
	private boolean available = false; //当前正在使用

	public CheckmeDevice(JSONObject jsonObject) throws JSONException{
		LogUtils.d("Device Info: " + jsonObject.toString());
		region = jsonObject.getString("Region");
		model = jsonObject.getString("Model");
		hardware = jsonObject.getString("HardwareVer");
		software = jsonObject.getInt("SoftwareVer");
		language = jsonObject.getInt("LanguageVer");
		//warning temp, 忽略当前语言错误, 忽略mode获取错误
		try {
			curLanguage = jsonObject.getString("CurLanguage");
			mode = jsonObject.getString("Application");
			if (mode == null || mode.equals("")) {
				//获取不到mode默认为家庭模式
				mode = MODE_HOME;
			}



			if(jsonObject.has("BranchCode") && !TextUtils.isEmpty(jsonObject.optString("BranchCode"))) {
				branchCode = jsonObject.getString("BranchCode");
				isBranchCodeAvailable = true;
			} else {
				isBranchCodeAvailable = false;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		spcpVer = jsonObject.getString("SPCPVer");
		fileVer = jsonObject.getString("FileVer");
		sn = jsonObject.getString("SN");

	}

	public CheckmeDevice(String region, String model, String hardware,
						 int software, int language, String curLanguage) {
		super();
		this.region = region;
		this.model = model;
		this.hardware = hardware;
		this.software = software;
		this.language = language;
		this.curLanguage = curLanguage;
	}

	public CheckmeDevice(String name, boolean available) {
		super();
		this.name = name;
		this.available = available;
	}

	/**
	 * 获取设备信息后，解析成CheckmeInfo
	 * @param checkmeInfo
	 * @return
	 */
	public static CheckmeDevice decodeCheckmeDevice(String checkmeInfo) {
		if (checkmeInfo==null || checkmeInfo.length()==0) {
			return null;
		}
		LogUtils.d(checkmeInfo);
		CheckmeDevice checkmeDevice;
		try {
			checkmeDevice = new CheckmeDevice(new JSONObject(checkmeInfo));
		} catch (JSONException e) {
			e.printStackTrace();
			LogUtils.d("解析数据异常");
			return null;
		}
		return checkmeDevice;
	}

	public String getRegion() {
		return region;
	}

	public String getModel() {
		return model;
	}

	public String getHardware() {
		return hardware;
	}

	public int getSoftware() {
		return software;
	}

	public int getLanguage() {
		return language;
	}

	public String getCurLanguage() {
		return curLanguage;
	}

	public String getSn() {
		return sn;
	}

	public String getSpcpVer() {
		return spcpVer;
	}

	public String getFileVer() {
		return fileVer;
	}

	public String getName() {
		return name;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public String getMode() {
		return mode;
	}

	public String getBranchCode() {
		return branchCode;
	}

	public boolean isBranchCodeAvailable() {
		return isBranchCodeAvailable;
	}
}
