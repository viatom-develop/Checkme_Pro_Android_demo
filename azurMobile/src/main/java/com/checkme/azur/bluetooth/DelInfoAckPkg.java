package com.checkme.azur.bluetooth;

import com.checkme.azur.utils.CRCUtils;
import com.checkme.azur.utils.LogUtils;

public class DelInfoAckPkg {
	private byte cmd;
	
	public DelInfoAckPkg(byte[] buf) {
		// TODO Auto-generated constructor stub
		if(buf.length!=BTConstant.COMMON_ACK_PKG_LENGTH){
			LogUtils.d("EndWriteAckPkg length error");
			return;
		}
		if(buf[0]!=(byte)0x55){
			LogUtils.d("EndWriteAckPkg front error");
			return;
		}else if ((cmd = buf[1]) != BTConstant.ACK_CMD_OK || buf[2] != ~BTConstant.ACK_CMD_OK) {
			LogUtils.d("EndWriteAckPkg cmd error");
			return;
		}else if (buf[buf.length-1]!=CRCUtils.calCRC8(buf)) {
			LogUtils.d("EndWriteAckPkg crc error");
			return;
		}
	}

	public byte getCmd() {
		return cmd;
	}
}
