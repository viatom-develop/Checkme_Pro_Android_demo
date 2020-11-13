package com.viatom.azur.measurement;

import com.viatom.azur.utils.LogUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by wangjiang on 12/28/2017.
 */

public class BPItem implements CommonItem {
    private byte[] dataBuf;
    private Date date;
    private int sys, dia, pr;

    public BPItem(byte[] buf) {
        if (buf.length != MeasurementConstant.BP_ITEM_LENGTH) {
            LogUtils.d("BP item length error");
            return;
        }

        dataBuf = buf;
        Calendar calendar = new GregorianCalendar((buf[0] & 0xFF)
                + ((buf[1] & 0xFF) << 8), (buf[2] & 0xFF) - 1, buf[3] & 0xFF,
                buf[4] & 0xFF, buf[5] & 0xFF, buf[6] & 0xFF);
        date = calendar.getTime();
        sys = (buf[7] & 0xFF) + ((buf[8] & 0xFF) << 8);
        dia = (buf[9] & 0xFF);
        pr = (buf[10] & 0xFF);
    }


    @Override
    public boolean isDownloaded() {
        return true;
    }

    @Override
    public byte[] getDataBuf() {
        return dataBuf;
    }

    @Override
    public Date getDate() {
        return date;
    }

    public int getSys() {
        return sys;
    }

    public int getDia() {
        return dia;
    }

    public int getPr() {
        return pr;
    }
}
