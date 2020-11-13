package com.checkme.bluetooth.classic.cmd;


import com.checkme.bluetooth.classic.BTConstant;
import com.checkme.bluetooth.utils.CRCUtils;
import com.checkme.bluetooth.utils.LogUtils;

public class ReadContentAckPkg {
	byte[] dataBuf;
	int dataLength;
	byte cmd;
	
	public ReadContentAckPkg(byte[] inBuf) {
		if(inBuf.length> BTConstant.READ_CONTENT_ACK_DATA_LENGTH
				+ BTConstant.READ_CONTENT_ACK_PKG_FRONT_LENGTH){
			LogUtils.d("ReadContentAckPkg length error");
			return;
		}
		if(inBuf[0]!=(byte)0x55){
			LogUtils.d("ReadContentAckPkg head error");
			return;
		}else if ((cmd = inBuf[1]) != BTConstant.ACK_CMD_OK || inBuf[2] != ~BTConstant.ACK_CMD_OK) {
			LogUtils.d("StartReadAckPkg cmd word error");
			return;
		}else if (inBuf[inBuf.length-1]!= CRCUtils.calCRC8(inBuf)) {
			LogUtils.d("ReadContentAckPkg CRC error");
			return;
		}
		dataLength = (inBuf[5]&0xFF) | (inBuf[6]&0xFF)<<8;
		if (dataLength>BTConstant.READ_CONTENT_ACK_DATA_LENGTH 
				|| dataLength>inBuf.length-BTConstant.READ_CONTENT_ACK_PKG_FRONT_LENGTH) {
			LogUtils.d("ReadContentAckPkg data length error");
			return;
		}
		dataBuf = new byte[dataLength];
		for (int i = 0; i < dataBuf.length; i++) {
			dataBuf[i] = inBuf[7+i];
		}
	}

	public byte[] getDataBuf() {
		return dataBuf;
	}

	public byte getCmd() {
		return cmd;
	}
	
}
