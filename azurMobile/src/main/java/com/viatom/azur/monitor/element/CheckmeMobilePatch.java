package com.viatom.azur.monitor.element;

import org.json.JSONException;
import org.json.JSONObject;

public class CheckmeMobilePatch {

	public static final int OS_TYPE_ANDROID = 1;
	public static final int OS_TYPE_IOS = 2;

	private int version;
	private int osType;
	private String address;

	public CheckmeMobilePatch(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null) {
			return;
		}
		version = jsonObject.getInt("Version");
		osType = jsonObject.getInt("OSType");
		/* address = Constant.SERVER_ADDRESS + jsonObject.getString("Address"); */
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getVersion() {
		return version;
	}

	public int getOsType() {
		return osType;
	}

}
