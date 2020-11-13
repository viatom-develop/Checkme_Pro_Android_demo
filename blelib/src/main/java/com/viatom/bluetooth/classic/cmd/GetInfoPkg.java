package com.viatom.bluetooth.classic.cmd;


import com.viatom.bluetooth.classic.BTConstant;
import com.viatom.bluetooth.utils.CRCUtils;

public class GetInfoPkg {
	byte[] buf;
	
	public GetInfoPkg() {
		buf = new byte[BTConstant.COMMON_PKG_LENGTH];
		buf[0] = (byte)0xAA;
		buf[1] = BTConstant.CMD_WORD_GET_INFO;
		buf[2] = ~BTConstant.CMD_WORD_GET_INFO;
		buf[buf.length-1] = CRCUtils.calCRC8(buf);
	}

	public byte[] getBuf() {
		return buf;
	}
	
}
