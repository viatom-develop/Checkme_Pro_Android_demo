package com.viatom.azur.utils;

import android.content.Context;
import android.os.Handler;

import com.viatom.azur.EventBusEvent.FlushIvEvent;
import com.viatom.azur.element.Constant;
import com.viatom.azur.element.UploadedItem;
import com.viatom.azur.measurement.DailyCheckItem;
import com.viatom.azur.measurement.ECGItem;
import com.viatom.azur.measurement.SLMItem;
import com.viatom.azur.measurement.SPO2Item;
import com.viatom.azur.measurement.TempItem;
import com.viatom.azur.tools.PreferenceUtils;

import org.greenrobot.eventbus.EventBus;
import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

/**
 * Created by wangxiaogang on 2017/2/16.
 */

public class ObservationResponseUtils {
    public synchronized static void DLCResponse(final Context context, final Handler handler, final DbManager db) {
        ++Constant.DLCQue;
        if (Constant.uploadDLCList == null) {
            LogUtils.d("uploadBDCList==null");
            return;
        }
        LogUtils.d(Constant.uploadDLCList.size() + "size" + Constant.DLCQue);
        if (Constant.DLCQue >= Constant.uploadDLCList.size() - 1) {
            String deviceName = PreferenceUtils.readStrPreferences(context, "PreDeviceName");
            for (DailyCheckItem item :
                    Constant.uploadDLCList) {
                UploadedItem uploadedItem = new UploadedItem(StringUtils.makeTimeString(item.getDate()), true, deviceName);
                try {
                    db.saveOrUpdate(uploadedItem);
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
            Constant.DLCQue = -1;
            if (Constant.userQue == 0)
                UploadQueUtils.makeECGUploadQue(context, db, handler);
            else
                UploadQueUtils.postOrEndOfLoop(context, db, handler);
        }
    }

    public synchronized static void ECGResponse(final Context context, final Handler handler, final DbManager db) {
        ++Constant.ECGQue;
        if (Constant.uploadECGList == null) {
            LogUtils.d("uploadECGList==null");
            return;
        }
        LogUtils.d(Constant.uploadECGList.size() + "size" + Constant.ECGQue);
        if (Constant.ECGQue >= Constant.uploadECGList.size() - 1) {
            String deviceName = PreferenceUtils.readStrPreferences(context, "PreDeviceName");
            for (ECGItem item :
                    Constant.uploadECGList) {
                UploadedItem uploadedItem = new UploadedItem(StringUtils.makeTimeString(item.getDate()), true, deviceName);
                try {
                    db.saveOrUpdate(uploadedItem);
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
            Constant.ECGQue = -1;
            UploadQueUtils.makeSPO2UploadQue(context, db, handler);
        }
    }

    public synchronized static void SPO2Response(final Context context, final Handler handler, final DbManager db) {
        ++Constant.SPO2Que;
        if (Constant.uploadSPO2List == null) {
            LogUtils.d("uploadSPO2List==null");
            return;
        }
        LogUtils.d(Constant.uploadSPO2List.size() + "size" + Constant.SPO2Que);
        if (Constant.SPO2Que >= Constant.uploadSPO2List.size() - 1) {
            String deviceName = PreferenceUtils.readStrPreferences(context, "PreDeviceName");
            for (SPO2Item item :
                    Constant.uploadSPO2List) {
                UploadedItem uploadedItem = new UploadedItem(StringUtils.makeTimeString(item.getDate()), true, deviceName);
                try {
                    db.saveOrUpdate(uploadedItem);
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
            Constant.SPO2Que = -1;
            UploadQueUtils.makeSLMUploadQue(context, db, handler);
        }
    }

    public synchronized static void SLMResponse(final Context context, final Handler handler, final DbManager db) {
        ++Constant.SLMQue;
        if (Constant.uploadSLMList == null) {
            LogUtils.d("uploadRLMList==null");
            return;
        }
        LogUtils.d(Constant.uploadSLMList.size() + "size" + Constant.SLMQue);
        if (Constant.SLMQue >= Constant.uploadSLMList.size() - 1) {
            String deviceName = PreferenceUtils.readStrPreferences(context, "PreDeviceName");
            for (SLMItem item :
                    Constant.uploadSLMList) {
                UploadedItem uploadedItem = new UploadedItem(StringUtils.makeTimeString(item.getDate()), true, deviceName);
                try {
                    db.saveOrUpdate(uploadedItem);
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
            Constant.SLMQue = -1;
            UploadQueUtils.makeTMPUploadQue(context, db, handler);
        }
    }

    public synchronized static void TMPResponse(final Context context, final Handler handler, final DbManager db) {
        ++Constant.TMPQue;
        if (Constant.uploadTMPList == null) {
            LogUtils.d("uploadTMPList==null");
            return;
        }
        LogUtils.d(Constant.uploadTMPList.size() + "size" + Constant.TMPQue);
        if (Constant.TMPQue >= Constant.uploadTMPList.size() - 1) {
            String deviceName = PreferenceUtils.readStrPreferences(context, "PreDeviceName");
            for (TempItem item :
                    Constant.uploadTMPList) {
                UploadedItem uploadedItem = new UploadedItem(StringUtils.makeTimeString(item.getDate()), true, deviceName);
                try {
                    db.saveOrUpdate(uploadedItem);
                } catch (DbException e) {
                    e.printStackTrace();
                }

            }
            //TODO end
            Constant.TMPQue = -1;
            UploadQueUtils.postOrEndOfLoop(context, db, handler);

        }
    }
}
