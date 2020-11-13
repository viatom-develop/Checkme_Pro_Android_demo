package com.viatom.bluetooth.classic.cmd;


import com.viatom.bluetooth.classic.BTConstant;
import com.viatom.bluetooth.utils.CRCUtils;
import com.viatom.bluetooth.utils.LogUtils;

public class WriteContentPkg {
	private byte[] buf;
	
	public WriteContentPkg(byte[] dataBuf, int pkgNum, byte cmd) {
		// TODO Auto-generated constructor stub
		if(dataBuf.length > BTConstant.WRITE_CONTENT_PKG_DATA_LENGTH){
			LogUtils.d("WriteContent data length error");
			return;
		}
		buf = new byte[dataBuf.length+BTConstant.COMMON_PKG_LENGTH];
		
		buf[0] = (byte)0xAA;
		buf[1] = cmd;
		buf[2] = (byte)~cmd;
		buf[3] = (byte)(pkgNum);//Package number
		buf[4] = (byte)(pkgNum>>8);
		buf[5] = (byte)(dataBuf.length);//data chunk size
		buf[6] = (byte)(dataBuf.length>>8);
		LogUtils.d("Sending pacakage number"+ pkgNum +", size:"+dataBuf.length);
		for (int i = 0; i < dataBuf.length; i++) {
			buf[i+7] = dataBuf[i];
		}
		buf[buf.length-1] = CRCUtils.calCRC8(buf);
	}

	public byte[] getBuf() {
		return buf;
	}
	
}
