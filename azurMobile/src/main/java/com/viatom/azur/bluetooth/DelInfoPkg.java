package com.viatom.azur.bluetooth;

import com.viatom.azur.utils.CRCUtils;
import com.viatom.azur.utils.LogUtils;

public class DelInfoPkg {
	byte[] buf;
	public DelInfoPkg(String fileName) {
		if (fileName==null || fileName.length()>BTConstant.BT_READ_FILE_NAME_MAX_LENGTH) {
			LogUtils.d("File name or file length error");
			return;
		}
		// +1 as \0, +4 used to represent files number
		buf = new byte[BTConstant.COMMON_PKG_LENGTH + fileName.length() + 1 + 4];
		buf[0] = (byte)0xAA;
		buf[1] = BTConstant.CMD_WORD_DEL_FILE;
		buf[2] = ~BTConstant.CMD_WORD_DEL_FILE;
		buf[3] = 0;//Package number, the default is 0
		buf[4] = 0;
		buf[5] = (byte)(buf.length - BTConstant.COMMON_PKG_LENGTH);//data length
		buf[6] = (byte)((buf.length - BTConstant.COMMON_PKG_LENGTH)>>8);
		//data chunk
		buf[7] = 1;//number of files that want to delete
		buf[8] = 0;
		buf[9] = 0;
		buf[10] = 0;
		
		char[] name = fileName.toCharArray();
		for (int i = 0; i < name.length; i++) {
			buf[i+11] = (byte)name[i];
		}
		
		buf[buf.length-1] = CRCUtils.calCRC8(buf);
	}
	
	public byte[] getBuf() {
		return buf;
	}
	
}
