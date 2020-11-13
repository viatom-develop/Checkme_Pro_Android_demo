package com.checkme.azur.monitor.tools;

import android.content.Context;
import android.content.SharedPreferences;

import com.checkme.azur.monitor.utils.LogUtils;

public class PreferenceUtils {

	/**
	 * 保存本地Long设置
	 * @param key
	 * @param value
	 */
	public static void savePreferences(Context context, String key, long value) {
		if (key == null || context == null) {
			return;
		}
		SharedPreferences preferences;
		SharedPreferences.Editor editor;
		preferences = context.getSharedPreferences("viatom", context.MODE_WORLD_READABLE);
		editor = preferences.edit();
		editor.putLong(key, value);
		editor.commit();
	}

	/**
	 * 保存本地Int设置
	 * @param key
	 * @param value
	 */
	public static void savePreferences(Context context, String key, int value) {
		if (key == null || context == null) {
			return;
		}
		SharedPreferences preferences;
		SharedPreferences.Editor editor;
		preferences = context.getSharedPreferences("viatom", context.MODE_WORLD_READABLE);
		editor = preferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	/**
	 * 保存本地String设置
	 * @param key
	 * @param value
	 */
	public static void savePreferences(Context context, String key, String value) {
		if (key == null || context == null || value == null) {
			return;
		}
		SharedPreferences preferences;
		SharedPreferences.Editor editor;
		preferences = context.getSharedPreferences("viatom", context.MODE_WORLD_READABLE);
		editor = preferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	/**
	 * 保存本地bool设置
	 * @param key
	 * @param value
	 */
	public static void savePreferences(Context context, String key, Boolean value) {
		if (key == null || context == null || value == null) {
			return;
		}
		SharedPreferences preferences;
		SharedPreferences.Editor editor;
		preferences = context.getSharedPreferences("viatom", context.MODE_WORLD_READABLE);
		editor = preferences.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	/**
	 * 读取本地long设置
	 * @param key
	 * @return
	 */
	public static long readLongPreferences(Context context, String key) {
		if (key == null || context == null) {
			return 0;
		}
		SharedPreferences preferences;
		preferences = context.getSharedPreferences("viatom"
				, context.MODE_WORLD_READABLE);
		long value = preferences.getLong(key, 0);

		return value;
	}

	/**
	 * 读取本地Int设置
	 * @param key
	 * @return
	 */
	public static int readIntPreferences(Context context, String key) {
		if (key == null || context == null) {
			return 0;
		}
		SharedPreferences preferences;
		preferences = context.getSharedPreferences("viatom"
				, context.MODE_WORLD_READABLE);
		int value = preferences.getInt(key, 0);

		return value;
	}

	/**
	 * 读取本地Bool设置
	 * @param key
	 * @return
	 */
	public static boolean readBoolPreferences(Context context, String key) {
		if (key == null || context == null) {
			return false;
		}
		SharedPreferences preferences;
		preferences = context.getSharedPreferences("viatom"
				, context.MODE_WORLD_READABLE);
		boolean value = preferences.getBoolean(key, false);

		return value;
	}

	/**
	 * 读取本地String设置
	 * @param key
	 * @return
	 */
	public static String readStrPreferences(Context context, String key) {
		if (key == null || context == null) {
			return null;
		}
		SharedPreferences preferences;
		preferences = context.getSharedPreferences("viatom"
				, context.MODE_WORLD_READABLE);
		String value = preferences.getString(key, null);

		return value;
	}

	/**
	 * 删除所有设置
	 * @param context
	 */
	public static void removeAllPreferences(Context context) {
		if (context == null) {
			return;
		}
		LogUtils.d("删除所有设置");
		SharedPreferences preferences;
		SharedPreferences.Editor editor;
		preferences = context.getSharedPreferences("viatom", context.MODE_WORLD_READABLE);
		editor = preferences.edit();
		editor.clear();
		editor.commit();
	}
}
