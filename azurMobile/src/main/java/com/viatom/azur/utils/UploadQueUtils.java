package com.viatom.azur.utils;

import android.content.Context;
import android.os.Handler;

import com.viatom.azur.EventBusEvent.FlushIvEvent;
import com.viatom.azur.element.Constant;
import com.viatom.azur.element.UploadedItem;
import com.viatom.azur.measurement.DailyCheckItem;
import com.viatom.azur.measurement.ECGItem;
import com.viatom.azur.measurement.MeasurementConstant;
import com.viatom.azur.measurement.SLMItem;
import com.viatom.azur.measurement.SPO2Item;
import com.viatom.azur.measurement.TempItem;

import org.greenrobot.eventbus.EventBus;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangxiaogang on 2017/2/16.
 */

public class UploadQueUtils {
    public static void makeDLCUploadQue(Context context, DbManager db, Handler handler) {
        if (Constant.sUploadUser==null){
//            MsgUtils.sendMsg(handler,Constant.MSG_OBSERVATION_ERROR);
            return;
        }
        List<DailyCheckItem> dailyCheckItemList = FileUtils.readDailyCheckList(Constant.dir, Constant.sUploadUser.getUserInfo().getID() + MeasurementConstant.FILE_NAME_DLC_LIST);
        if (dailyCheckItemList != null && dailyCheckItemList.size() != 0) {
            List<DailyCheckItem> tempDailyCheckItemList = new ArrayList<>();
            for (DailyCheckItem item :
                    dailyCheckItemList) {
                try {
                    String date = StringUtils.makeTimeString(item.getDate());
                    UploadedItem uploadedItem = db.findById(UploadedItem.class, date);
                    if ((uploadedItem == null || !uploadedItem.isUploaded()) && item.isDownloaded()) {
                        tempDailyCheckItemList.add(item);
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
            LogUtils.d("DLCque======");
            //upload list
            if (tempDailyCheckItemList.size() != 0) {
                Constant.uploadDLCList = tempDailyCheckItemList;
                UploadQueResponseUtils.DLCListQueResponse(context, handler,db);
            } else {
                if (Constant.userQue == 0)
                    makeECGUploadQue(context, db, handler);
                else
                    postOrEndOfLoop(context,db,handler);
            }

        } else {
            if (Constant.userQue == 0)
                makeECGUploadQue(context, db, handler);
            else
                postOrEndOfLoop(context,db,handler);
        }
    }

    public static void makeECGUploadQue(Context context, DbManager db, Handler handler) {
        List<ECGItem> ecgItemList = FileUtils.readECGList(Constant.dir, MeasurementConstant.FILE_NAME_ECG_LIST);
        if (ecgItemList != null && ecgItemList.size() != 0) {
            List<ECGItem> tempEcgItemList = new ArrayList<>();
            for (ECGItem item :
                    ecgItemList) {
                try {
                    UploadedItem uploadedItem = db.findById(UploadedItem.class, StringUtils.makeTimeString(item.getDate()));
                    if ((uploadedItem == null || !uploadedItem.isUploaded()) && item.isDownloaded()) {
                        tempEcgItemList.add(item);
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }

            LogUtils.d("ECGque======");
            //upload list
            if (tempEcgItemList.size() != 0) {
                Constant.uploadECGList = tempEcgItemList;
                UploadQueResponseUtils.ECGListQueResponse(context, handler,db);
            } else
                makeSPO2UploadQue(context, db, handler);
        } else {
            makeSPO2UploadQue(context, db, handler);
        }
    }

    public static void makeSPO2UploadQue(Context context, DbManager db, Handler handler) {
        List<SPO2Item> spo2ItemList = FileUtils.readSPO2List(Constant.dir,  MeasurementConstant.FILE_NAME_SPO2_LIST);
        if (spo2ItemList != null && spo2ItemList.size() != 0) {
            List<SPO2Item> tempSpo2ItemList = new ArrayList<>();
            for (SPO2Item item :
                    spo2ItemList) {
                try {
                    UploadedItem uploadedItem = db.findById(UploadedItem.class, StringUtils.makeTimeString(item.getDate()));
                    if (uploadedItem == null || !uploadedItem.isUploaded()) {
                        tempSpo2ItemList.add(item);
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
            LogUtils.d("SPO2que======");
            //upload list
            if (tempSpo2ItemList.size() != 0) {
                Constant.uploadSPO2List = tempSpo2ItemList;
                UploadQueResponseUtils.SPO2ListQueResponse(context, handler,db);
            } else
                makeSLMUploadQue(context, db, handler);
        } else {
            makeSLMUploadQue(context, db, handler);
        }
    }

    public static void makeSLMUploadQue(Context context, DbManager db, Handler handler) {
        List<SLMItem> slmItemList = FileUtils.readSLMList(Constant.dir,MeasurementConstant.FILE_NAME_SLM_LIST);
        if (slmItemList != null && slmItemList.size() != 0) {
            List<SLMItem> tempSlmItemList = new ArrayList<>();
            for (SLMItem item :
                    slmItemList) {
                try {
                    UploadedItem uploadedItem = db.findById(UploadedItem.class, StringUtils.makeTimeString(item.getDate()));
                    if (uploadedItem == null || !uploadedItem.isUploaded()) {
                        tempSlmItemList.add(item);
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
            LogUtils.d("SLMque======");
            //upload list
            if (tempSlmItemList.size() != 0) {
                Constant.uploadSLMList = tempSlmItemList;
                UploadQueResponseUtils.SLMListQueResponse(context, handler,db);
            } else
                makeTMPUploadQue(context, db, handler);

        } else {
            makeTMPUploadQue(context, db, handler);
        }
    }

    public static void makeTMPUploadQue(Context context, DbManager db, Handler handler) {
        List<TempItem> tempItemList = FileUtils.readTempList(Constant.dir,MeasurementConstant.FILE_NAME_TEMP_LIST);
        if (tempItemList != null && tempItemList.size() != 0) {
            List<TempItem> tempTempItemList = new ArrayList<>();
            for (TempItem item :
                    tempItemList) {
                try {
                    UploadedItem uploadedItem = db.findById(UploadedItem.class, StringUtils.makeTimeString(item.getDate()));
                    if (uploadedItem == null || !uploadedItem.isUploaded()) {
                        tempTempItemList.add(item);
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
            LogUtils.d("TMPque======");
            //upload list
            if (tempTempItemList.size() != 0) {
                Constant.uploadTMPList = tempTempItemList;
                UploadQueResponseUtils.TMPListQueResponse(context, handler,db);
            } else {
                postOrEndOfLoop(context,db,handler);
            }
        } else {
            postOrEndOfLoop(context,db,handler);
        }
    }

    public static void postOrEndOfLoop(Context context, DbManager db, Handler handler){
        if (Constant.userList == null)
            Constant.userList=FileUtils.readUserList(Constant.dir,MeasurementConstant.FILE_NAME_USER_LIST);
        if (Constant.userQue >= Constant.userList.length - 1) {
            Constant.sUploadState = Constant.UPLOADED;
            EventBus.getDefault().post(new FlushIvEvent(Constant.UPLOADED));
            Constant.cancelPost();
        } else {
            UploadListUtils.postPatient(context, handler, db);
        }
    }

}
