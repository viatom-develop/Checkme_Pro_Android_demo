package com.checkme.azur.monitor.utils;

import android.util.Log;

/**
 * Tools used to pint debug information.
 * 
 * @author zouhao
 */
public class LogUtils {
	/**
	 * Debug flag
	 */
	public static boolean DEBUG = true;

	/**
	 * Log debug information with tag "VD" if DEBUG flag is true
	 * 
	 * @param string
	 */
	public static void d(String string) {
		if (DEBUG)
			Log.d("VD", string);
	}

	/**
	 * Log debug information with tag "VD" if DEBUG flag is true
	 * 
	 * @param string
	 */
	public static void e(String string) {
		if (DEBUG)
			Log.e("VD", string);
	}

	/**
	 * Print debug information if DEBUG flag is true
	 * 
	 * @param string
	 */
	public static void println(String string) {
		if (DEBUG)
			System.out.println(string);
	}
}
