package com.checkme.azur.element;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author zouhao
 * 语言包
 */
public class LanguagePatch implements Comparable<LanguagePatch>{
	private String region;
	private String model;
	private String hardware;
	private int version;
	private String address;
	private List<String> supportLanguageList = new ArrayList<String>();
	private String languages = new String();

	public LanguagePatch(JSONObject jsonObject) throws JSONException{
		if (jsonObject==null) {
			return;
		}
		region = jsonObject.getString("LanguageRegion");
		model = jsonObject.getString("LanguageModel");
		hardware = jsonObject.getString("LanguageHardware");
		version = jsonObject.getInt("LanguageVersion");
		JSONArray jsonArray = jsonObject.getJSONArray("LanguageLanguages");
		for (int i = 0; i < jsonArray.length(); i++) {
			languages += jsonArray.getString(i) + ",";
		}
		address = jsonObject.getString("LanguageAddress");
	}

	public String getAddress() {
		return address;
	}

	public List<String> getSupportLanguageList() {
		return supportLanguageList;
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

	public String getLanguages() {
		return languages;
	}

	@Override
	public int compareTo(LanguagePatch o) {
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
