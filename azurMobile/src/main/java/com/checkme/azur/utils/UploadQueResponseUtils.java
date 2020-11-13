package com.checkme.azur.utils;

import android.content.Context;
import android.os.Handler;

import com.checkme.azur.element.Constant;

import org.xutils.DbManager;

/**
 * Created by wangxiaogang on 2017/2/16.
 */

public class UploadQueResponseUtils {
    public synchronized static void DLCListQueResponse(Context context, Handler handler, DbManager db) {
        if (Constant.uploadDLCList == null || Constant.sUploadUser == null)
            return;
        int size = Constant.uploadDLCList.size();
        for (int i = 0; i < size; i++) {
            if (Constant.uploadDLCList == null || Constant.sUploadUser == null)
                return;
            UploadListUtils.uploadDLCList(context, Constant.uploadDLCList, handler, i,db);
        }
    }

    public synchronized static void ECGListQueResponse(Context context,  Handler handler, DbManager db) {
        if (Constant.uploadECGList == null || Constant.sUploadUser == null)
            return;
        int size = Constant.uploadECGList.size();
        for (int i = 0; i < size; i++) {
            if (Constant.uploadECGList == null || Constant.sUploadUser == null)
                return;
            UploadListUtils.uploadECGList(context, Constant.uploadECGList, handler, i,db);
        }
    }

    public synchronized static void SPO2ListQueResponse( Context context, Handler handler, DbManager db) {
        if (Constant.uploadSPO2List == null || Constant.sUploadUser == null)
            return;
        int size = Constant.uploadSPO2List.size();
        for (int i = 0; i < size; i++) {
            if (Constant.uploadSPO2List == null || Constant.sUploadUser == null)
                return;
            UploadListUtils.uploadSPO2List(context, Constant.uploadSPO2List, handler, i,db);
        }
    }

    public synchronized static void SLMListQueResponse( Context context, Handler handler, DbManager db) {
        if (Constant.uploadSLMList == null || Constant.sUploadUser == null)
            return;
        int size = Constant.uploadSLMList.size();
        for (int i = 0; i < size; i++) {
            if (Constant.uploadSLMList == null || Constant.sUploadUser == null)
                return;
            UploadListUtils.uploadSLMList(context, Constant.uploadSLMList, handler, i,db);
        }
    }

    public synchronized static void TMPListQueResponse(Context context, Handler handler, DbManager db) {
        if (Constant.uploadTMPList == null || Constant.sUploadUser == null)
            return;
        int size = Constant.uploadTMPList.size();
        for (int i = 0; i < size; i++) {
            if (Constant.uploadTMPList == null || Constant.sUploadUser == null)
                return;
            UploadListUtils.uploadTMPList(context, Constant.uploadTMPList, handler, i,db);
        }
    }
}
