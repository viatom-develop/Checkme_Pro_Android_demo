package com.viatom.azur.bluetooth;

import com.viatom.azur.utils.CRCUtils;
import com.viatom.azur.utils.LogUtils;

import org.json.JSONObject;

/**
 *
 * Created by wangxiaogang on 2016/9/19.
 */

public class ParaSyncPkg {
    private byte[] buf;
    public ParaSyncPkg(JSONObject jsonObject) {
        char[] object=jsonObject.toString().toCharArray();
        LogUtils.d(jsonObject.toString());
        buf=new byte[BTConstant.COMMON_PKG_LENGTH+object.length];
        buf[0]=(byte)0xAA;
        buf[1] = BTConstant.CMD_WORD_PARA_SYNC;
        buf[2] = ~BTConstant.CMD_WORD_PARA_SYNC;
        buf[3] = 0;//Package number, the default is 0
        buf[4] = 0;
        buf[5] = (byte)(buf.length - BTConstant.COMMON_PKG_LENGTH);//data chunk size
        buf[6] = (byte)((buf.length - BTConstant.COMMON_PKG_LENGTH)>>8);
        for (int i = 0; i < object.length; i++) {
            buf[i+7] = (byte)object[i];
        }
        buf[buf.length-1] = CRCUtils.calCRC8(buf);
    }

    public byte[] getBuf() {
        return buf;
    }
}
