package com.viatom.update;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public class SharedPrefHelper {
    private static volatile SharedPrefHelper instance;

    private SharedPreferences sharedPreferences;

    public static SharedPrefHelper newInstance(@NonNull Context context) {
        if(instance == null) {
            synchronized (SharedPrefHelper.class) {
                if(instance == null) {
                    instance = new SharedPrefHelper(context.getApplicationContext());
                    return instance;
                }
            }
        }
        return instance;
    }

    private SharedPrefHelper(Context context) {
        String packageName = context.getPackageName();
        sharedPreferences = context.getSharedPreferences(packageName, Context.MODE_PRIVATE);
    }

    public void putIntValue(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int readIntValue(String key) {
        return readIntValue(key, 0);
    }

    public int readIntValue(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public void putStringValue(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String readStringValue(String key) {
        return readStringValue(key, "");
    }

    public String readStringValue(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public void putBooleanValue(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean readBooleanValue(String key) {
        return readBooleanValue(key, false);
    }

    public boolean readBooleanValue(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }
}
