package com.checkme.azur.bluetooth;

import com.checkme.azur.utils.CRCUtils;
import com.checkme.azur.utils.LogUtils;

public class EndReadAckPkg {
	private byte errCode = 0;
	private byte cmd;
	
	public EndReadAckPkg(byte[] buf) {
		// TODO Auto-generated constructor stub
		if(buf.length != BTConstant.COMMON_ACK_PKG_LENGTH) {
			LogUtils.d("EndReadAckPkg length error");
			return;
		}
		if(buf[0]!=(byte)0x55) {
			LogUtils.d("EndReadAckPkg head error");
			return;
		}else if ((cmd = buf[1]) != BTConstant.ACK_CMD_OK || buf[2] != ~BTConstant.ACK_CMD_OK) {
			LogUtils.d("EndReadAckPkg cmd word error");
			return;
		}else if (buf[buf.length-1]!=CRCUtils.calCRC8(buf)) {
			LogUtils.d("EndReadAckPkg CRC error");
			return;
		}
	}

	public byte getErrCode() {
		return errCode;
	}

	public byte getCmd() {
		return cmd;
	}
	
}
