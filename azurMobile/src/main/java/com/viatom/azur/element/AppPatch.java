package com.viatom.azur.element;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author zouhao
 * App升级包
 */
public class AppPatch implements Comparable<AppPatch>{
	private String region;
	private String model;
	private String hardware;
	private int version;
	private String address;
	private int dependLanguageVersion;

	public AppPatch(JSONObject jsonObject) throws JSONException{
		if (jsonObject==null) {
			return;
		}
		region = jsonObject.getString("AppRegion");
		model = jsonObject.getString("AppModel");
		hardware = jsonObject.getString("AppHardware");
		version = jsonObject.getInt("AppVersion");
		address = jsonObject.getString("AppAddress");
		dependLanguageVersion = jsonObject.getInt("AppDependLanguageVersion");
	}

	public String getAddress() {
		return address;
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

	public int getVersion() {
		return version;
	}

	public int getDependLanguageVersion() {
		return dependLanguageVersion;
	}

	@Override
	public int compareTo(AppPatch o) {
		// TODO Auto-generated method stub
		if(version>o.getVersion()){
			return 1;//比o大，返回1
		}else if (version<o.getVersion()) {
			return -1;//比o小，返回-1
		}else {
			return 0;//相等，返回0
		}
	}
}
