package com.viatom.azur.utils;

import com.viatom.azur.measurement.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StringUtils {

//	public static String makeFileName(String fileName, int fileType, User user) {
//
//		String tempfileName = fileName.substring(fileName.length()-7, fileName.length());
//		String newFileName = user.getUserInfo().getMedicalId() + tempfileName;
//		return newFileName==null?"":newFileName;
//	}

	public static boolean isUpdateAvailable(String lastVersion, String curVersion) {
		if (lastVersion==null||lastVersion.length()==0||curVersion==null||curVersion.length()==0)
			return false;
		String[] lastVer=lastVersion.split("\\.");
		String[] curVer=curVersion.split("\\.");
		LogUtils.d(lastVer.length+"");
		boolean isUpdateAvailable=false;
		if (Integer.valueOf(lastVer[0])>Integer.valueOf(curVer[0])){
			isUpdateAvailable=true;
		}else {
			if (Integer.valueOf(lastVer[1])>Integer.valueOf(curVer[1])){

				isUpdateAvailable=true;
			}else {
                isUpdateAvailable = Integer.valueOf(lastVer[2]) > Integer.valueOf(curVer[2]);

			}
		}

		return isUpdateAvailable;
	}
	public static String makeTimeString(Date mDate) {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		//        LogUtils.d(date);
		return sDateFormat.format(mDate);
	}
	public static String makeSetTimeString(){
		SimpleDateFormat sDateFormat=new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss",Locale.getDefault());
		String date=sDateFormat.format(new Date());
		LogUtils.d(date);
		return date;
	}

	/**
	 * 字节数组转换为十六进制字符串
	 *
	 * @param b
	 *            byte[] 需要转换的字节数组
	 * @return String 十六进制字符串
	 */
	public static String byte2hex(byte b[]) {
		if (b == null) {
			throw new IllegalArgumentException(
					"Argument b ( byte array ) is null! ");
		}
		String hs = "";
		String stmp;
		for (int n = 0; n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0xff);
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs.toUpperCase();
	}
	public static String int2hex(int b[]) {
		if (b == null) {
			throw new IllegalArgumentException(
					"Argument b ( byte array ) is null! ");
		}
		String hs = "";
		String stmp;
		for (int n = 0; n < b.length; n++) {
			stmp = Integer.toHexString(b[n]);
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs.toUpperCase();
	}
}
