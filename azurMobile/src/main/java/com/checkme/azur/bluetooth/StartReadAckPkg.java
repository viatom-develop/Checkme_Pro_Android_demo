package com.checkme.azur.bluetooth;

import com.checkme.azur.utils.CRCUtils;
import com.checkme.azur.utils.LogUtils;

public class StartReadAckPkg {
	private byte cmd = 1;
	private byte errCode = 0;
	private int fileSize;
	
	public StartReadAckPkg(byte[] buf) {
		// TODO Auto-generated constructor stub
		if(buf.length!=BTConstant.COMMON_ACK_PKG_LENGTH){
			LogUtils.d("StartReadAckPkg length error");
			return;
		}
		if(buf[0]!=(byte)0x55){
			LogUtils.d("StartReadAckPkg head error");
			return;
		}else if ((cmd = buf[1])!=BTConstant.ACK_CMD_OK || buf[2]!=~BTConstant.ACK_CMD_OK) {
			LogUtils.d("StartReadAckPkg cmd word error");
			return;
		}else if (buf[buf.length-1]!=CRCUtils.calCRC8(buf)) {
			LogUtils.d("StartReadAckPkg CRC error");
			return;
		}
		
		fileSize = (buf[7]&0xFF) | (buf[8]&0xFF)<<8 | (buf[9]&0xFF)<<16 | (buf[10]&0xFF)<<24;
		if(fileSize<=0){
			LogUtils.d("StartReadAckPkg File size error");
			return;
		}
	}

	public byte getCmd() {
		return cmd;
	}

	public byte getErrCode() {
		return errCode;
	}

	public int getFileSize() {
		return fileSize;
	}
	
}
