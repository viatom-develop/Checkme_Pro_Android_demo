package com.viatom.azur.monitor.element;

import com.viatom.azur.monitor.utils.LogUtils;
import com.viatom.azur.monitor.utils.NumUtils;

public class OtherData {
	
	public static final int PKG_LENGTH = 1;
	
	private int power;
	
	public OtherData(byte[] buf) {

		
		if (buf==null || buf.length!=PKG_LENGTH) {
			LogUtils.e("other buf length err");
			return;
		}
		
		int index = 0;
		
		power = NumUtils.bToi(buf[index++]);
	}

	public int getPower() {
		return power;
	}
}
