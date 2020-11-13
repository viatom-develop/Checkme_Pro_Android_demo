package com.viatom.azur.element;

import com.viatom.azur.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdatePatch {
	private String version;
	private String fileLocate;
    private List<String> keyList;
	private Map<String,String> lanLocate;

	public UpdatePatch(JSONObject jsonObject) throws JSONException{
		if (jsonObject==null) {
			return;
		}
		version = jsonObject.getString("version");
		fileLocate = jsonObject.getString("fileLocate");
		JSONObject languageLocate = jsonObject.getJSONObject("language");
		String firstLocateKey="English";
        String secondLocateKey="Simplified Chinese/Traditional Chinese/English";
        String thirdLocateKey="Spanish/German/French/Italian/English";
        String fourLocateKey="Polish/Russian/Hungarian/Czech/English";
        keyList=new ArrayList<>();
        keyList.add(firstLocateKey);
        keyList.add(secondLocateKey);
        keyList.add(thirdLocateKey);
        keyList.add(fourLocateKey);
		lanLocate=new HashMap<>();
		lanLocate.put(firstLocateKey,languageLocate.getString(firstLocateKey));
		lanLocate.put(secondLocateKey,languageLocate.getString(secondLocateKey));
		lanLocate.put(thirdLocateKey,languageLocate.getString(thirdLocateKey));
		lanLocate.put(fourLocateKey,languageLocate.getString(fourLocateKey));
		LogUtils.d(fileLocate);
		LogUtils.d(version);
	}

    public List<String> getKeyList() {
        return keyList;
    }

    public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getFileLocate() {
		return fileLocate;
	}

	public void setFileLocate(String fileLocate) {
		this.fileLocate = fileLocate;
	}

	public Map<String, String> getLanLocate() {
		return lanLocate;
	}

	public void setLanLocate(Map<String, String> lanLocate) {
		this.lanLocate = lanLocate;
	}
}
