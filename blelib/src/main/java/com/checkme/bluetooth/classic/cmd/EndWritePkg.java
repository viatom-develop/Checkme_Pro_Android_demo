package com.checkme.bluetooth.classic.cmd;


import com.checkme.bluetooth.classic.BTConstant;
import com.checkme.bluetooth.utils.CRCUtils;

public class EndWritePkg {
	byte[] buf = new byte[BTConstant.COMMON_PKG_LENGTH];
	
	public EndWritePkg(byte cmd) {
		// TODO Auto-generated constructor stub
		buf[0] = (byte)0xAA;
		buf[1] = cmd;
		buf[2] = (byte)~cmd;
		buf[3] = 0;//Package number, the default is 0
		buf[4] = 0;
		buf[5] = 0;//data chunk size, the default is 0
		buf[6] = 0;
		buf[buf.length-1] = CRCUtils.calCRC8(buf);
	}

	public byte[] getBuf() {
		return buf;
	}
	
}
