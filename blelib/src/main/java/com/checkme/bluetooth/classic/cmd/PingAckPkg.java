package com.checkme.bluetooth.classic.cmd;


import com.checkme.bluetooth.classic.BTConstant;
import com.checkme.bluetooth.utils.CRCUtils;
import com.checkme.bluetooth.utils.LogUtils;

public class PingAckPkg {
	private byte errCode = 0;
	private byte cmd;
	private byte[] buf;
	
	public PingAckPkg(byte[] buf) {
		LogUtils.d("BUG[0]="+(int)buf[0]);
		LogUtils.d("BUG[1]="+(int)buf[1]);
		this.buf=buf;
		if(buf.length!= BTConstant.COMMON_ACK_PKG_LENGTH) {
			LogUtils.d("PingAckPkg length error");
			return;
		}
		if(buf[0]!=(byte)0x55) {
			LogUtils.d("PingAckPkg head error");
			return;
		}else if ((cmd = buf[1]) != BTConstant.ACK_CMD_OK || buf[2] != ~BTConstant.ACK_CMD_OK) {
			LogUtils.d("PingAckPkg cmd word error");
			return;
		}else if (buf[buf.length-1]!= CRCUtils.calCRC8(buf)) {
			LogUtils.d("PingAckPkg CRC error");
			return;
		}
	}

	public boolean isValid() {
		if(buf.length!= BTConstant.COMMON_ACK_PKG_LENGTH) {
			LogUtils.d("PingAckPkg length error");
			return false;
		}
		if(buf[0]!=(byte)0x55) {
			LogUtils.d("PingAckPkg head error");
			return false;
		}else if ((cmd = buf[1]) != BTConstant.ACK_CMD_OK || buf[2] != ~BTConstant.ACK_CMD_OK) {
			LogUtils.d("PingAckPkg cmd word error");
			return false;
		}else if (buf[buf.length-1]!= CRCUtils.calCRC8(buf)) {
			LogUtils.d("PingAckPkg CRC error");
			return false;
		}

		return true;
	}

	public byte getErrCode() {
		return errCode;
	}

	public byte getCmd() {
		return cmd;
	}

	public byte[] getBuf() {
		return buf;
	}
}
