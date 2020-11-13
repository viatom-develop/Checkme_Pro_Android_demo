package com.viatom.azur.bluetooth;

import com.viatom.azur.utils.CRCUtils;
import com.viatom.azur.utils.LogUtils;

public class StartWritePkg {
	private byte[] buf;
	
	public StartWritePkg(String fileName, int fileSize, byte cmd){
		if(fileName.length()>BTConstant.BT_WRITE_FILE_NAME_MAX_LENGTH || fileName.length()==0){
			LogUtils.d("File name error");
			return;
		}
		buf = new byte[BTConstant.COMMON_PKG_LENGTH + fileName.length() + 1 + 4];
		buf[0] = (byte)0xAA;
		buf[1] = cmd;
		buf[2] = (byte)~cmd;
		buf[3] = 0; //Package number, the default is 0
		buf[4] = 0;
		buf[5] = (byte)(buf.length - BTConstant.COMMON_PKG_LENGTH);//data chunk size
		buf[6] = (byte)((buf.length - BTConstant.COMMON_PKG_LENGTH)>>8);
		//File size
		buf[7] = (byte)(fileSize);
		buf[8] = (byte)(fileSize>>8);
		buf[9] = (byte)(fileSize>>16);
		buf[10] = (byte)(fileSize>>24);
		//File name
		char[] tempFileName = fileName.toCharArray();
		for (int i = 0; i < tempFileName.length; i++) {
			buf[i+11] = (byte)tempFileName[i];
		}
		buf[buf.length-1] = CRCUtils.calCRC8(buf);
	}
	
	public byte[] getBuf() {
		return buf;
	}
}
