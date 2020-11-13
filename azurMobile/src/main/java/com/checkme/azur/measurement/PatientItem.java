package com.checkme.azur.measurement;

import org.json.JSONObject;

/**
 * Created by wangjiang on 2017/10/17.
 */

public class PatientItem {
    private int id, ico, weight, height;
    private String name, gender, birth;

    public PatientItem(JSONObject jsonObject) {
        id = jsonObject.optInt("id");
        name = jsonObject.optString("name");
        ico = jsonObject.optInt("ico");
        gender = jsonObject.optString("gender");
        birth = jsonObject.optString("birthday");
        weight = jsonObject.optInt("weight");
        height = jsonObject.optInt("height");
    }

    public int getId() { return id; }
    public String getName() {
        return name;
    }
    public int getIco() {
        return ico;
    }
    public String getGender() {
        return gender;
    }
    public String getBirth() {
        return birth;
    }
    public int getWeight() {
        return weight;
    }
    public int getHeight() {
        return height;
    }
}
