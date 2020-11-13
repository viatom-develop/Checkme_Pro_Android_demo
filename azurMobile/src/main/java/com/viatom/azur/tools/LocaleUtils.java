package com.viatom.azur.tools;

import java.util.Locale;

public class LocaleUtils {

	/**
	 * 是否是西方语言环境
	 * @return
	 */
	public static boolean isWestLanguage() {

		if (Locale.getDefault().getLanguage().equals(Locale.CHINESE.getLanguage()) ||
				Locale.getDefault().getLanguage().equals(Locale.JAPANESE.getLanguage())) {
			return false;
		}else {
			return true;
		}
	}
}
