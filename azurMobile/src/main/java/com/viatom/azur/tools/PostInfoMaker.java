package com.viatom.azur.tools;


import com.viatom.azur.element.CheckmeDevice;
import com.viatom.azur.element.CheckmeMobilePatch;

public class PostInfoMaker {

	/**
	 * 获得支持语言列表请求参数
	 *
	 * @param checkmeInfo
	 * @return
	 */
//	public static List<NameValuePair> makeGetLanguageListInfo(
//			CheckmeDevice checkmeInfo) {
//		if (checkmeInfo == null) {
//			return null;
//		} else {
//			List<NameValuePair> list = new ArrayList<NameValuePair>();
//			list.add(new BasicNameValuePair("Region", checkmeInfo.getRegion()));
//			list.add(new BasicNameValuePair("Model", checkmeInfo.getModel()));
//			list.add(new BasicNameValuePair("Hardware", checkmeInfo
//					.getHardware()));
//			list.add(new BasicNameValuePair("Software", String
//					.valueOf(checkmeInfo.getSoftware())));
//			list.add(new BasicNameValuePair("Language", String
//					.valueOf(checkmeInfo.getLanguage())));
//			return list;
//		}
//	}

	/**
	 * @param checkmeInfo
	 * @param wantLanguage
	 * @return 获得升级包信息
	 */
//	public static List<NameValuePair> makeGetCheckmePatchInfo(
//			CheckmeDevice checkmeInfo, String wantLanguage) {
//		if (checkmeInfo == null) {
//			return null;
//		} else {
//			List<NameValuePair> list = new ArrayList<NameValuePair>();
//			list.add(new BasicNameValuePair("Region", checkmeInfo.getRegion()));
//			list.add(new BasicNameValuePair("Model", checkmeInfo.getModel()));
//			list.add(new BasicNameValuePair("Hardware", checkmeInfo
//					.getHardware()));
//			list.add(new BasicNameValuePair("Software", String
//					.valueOf(checkmeInfo.getSoftware())));
//			list.add(new BasicNameValuePair("Language", String
//					.valueOf(checkmeInfo.getLanguage())));
//			list.add(new BasicNameValuePair("WantLanguage", wantLanguage));
//
//			return list;
//		}
//	}

	/**
	 * 获取app升级包
	 * @param curAppVersion
	 * @return
	 */
//	public static List<NameValuePair> makeGetAppPatchInfo(int curAppVersion) {
//		if (curAppVersion < 0) {
//			return null;
//		} else {
//			List<NameValuePair> list = new ArrayList<NameValuePair>();
//			list.add(new BasicNameValuePair("OSType", String.valueOf(CheckmeMobilePatch.OS_TYPE_ANDROID)));
//			list.add(new BasicNameValuePair("Version", String.valueOf(curAppVersion)));
//
//			return list;
//		}
//	}
}
