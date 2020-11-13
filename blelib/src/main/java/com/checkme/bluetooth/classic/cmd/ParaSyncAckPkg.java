package com.checkme.bluetooth.classic.cmd;


import com.checkme.bluetooth.classic.BTConstant;
import com.checkme.bluetooth.utils.CRCUtils;
import com.checkme.bluetooth.utils.LogUtils;

/**
 * Created by wangxiaogang on 2016/9/19.
 */

public class ParaSyncAckPkg {
    private byte cmd;
    public ParaSyncAckPkg(byte[] buf) {
        // TODO Auto-generated constructor stub
        if(buf.length != BTConstant.COMMON_ACK_PKG_LENGTH) {
            LogUtils.d("ParaSyncAckPkg length error");
            return;
        }
        if(buf[0]!=(byte)0x55) {
            LogUtils.d("ParaSyncAckPkg head error");
        }else if ((cmd = buf[1]) != BTConstant.ACK_CMD_OK || buf[2] != ~BTConstant.ACK_CMD_OK) {
            LogUtils.d("ParaSyncAckPkg cmd word error");
        }else if (buf[buf.length-1]!= CRCUtils.calCRC8(buf)) {
            LogUtils.d("ParaSyncAckPkg CRC error");
        }
    }

    public byte getCmd() {
        return cmd;
    }

    public byte getErrCode() {
        return (byte) 0;
    }
}
