package com.viatom.azur.utils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.viatom.azur.EventBusEvent.FlushIvEvent;
import com.viatom.azur.element.Constant;
import com.viatom.azur.internet.UploadCallback;
import com.viatom.azur.measurement.DailyCheckItem;
import com.viatom.azur.measurement.ECGItem;
import com.viatom.azur.measurement.MeasurementConstant;
import com.viatom.azur.measurement.SLMItem;
import com.viatom.azur.measurement.SPO2Item;
import com.viatom.azur.measurement.TempItem;
import com.viatom.azur.tools.PreferenceUtils;
import com.viatom.newazur.BuildConfig;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.http.app.HttpRetryHandler;
import org.xutils.x;

import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by wangxiaogang on 2017/2/16.
 */

public class UploadListUtils {

    public synchronized static void postPatient(Context context, final Handler handler, DbManager db) {
        Constant.userQue++;
        LogUtils.d("Constant.userQue==>" + Constant.userQue);
        if (Constant.userList == null)
            Constant.userList = FileUtils.readUserList(Constant.dir, MeasurementConstant.FILE_NAME_USER_LIST);
        Constant.sUploadUser = Constant.userList[Constant.userQue];
        String deviceName = PreferenceUtils.readStrPreferences(context, "PreDeviceName");
        if (PreferenceUtils.readStrPreferences(context, deviceName + Constant.sUploadUser.getUserInfo().getID()) == null) {
            String content = InternetUtils.patientToString(context);
            /** 判断https证书是否成功验证 */
            /*SSLContext sslContext = InternetUtils.getSSLContext(context);
            if (null == sslContext) {
                if (BuildConfig.DEBUG)
                    LogUtils.d("Error:Can't Get SSLContext!");
                return;
            }*/
            RequestParams params = makeParams(Constant.PATIENT_RESOURCE_URL, context, content);
            Callback.Cancelable cancelable = x.http().post(params, new Callback.ProgressCallback<JSONObject>() {
                @Override
                public void onWaiting() {
                    LogUtils.d("onWaiting");
                }

                @Override
                public void onStarted() {
                    LogUtils.d("onStarted");
                    Constant.sUploadState = Constant.UPLOADING;
                    EventBus.getDefault().post(new FlushIvEvent(Constant.UPLOADING));

                }

                @Override
                public void onLoading(long total, long current, boolean isDownloading) {

                }

                @Override
                public void onSuccess(JSONObject result) {
                    LogUtils.d(result.toString());
                    MsgUtils.sendMsg(handler, result, Constant.MSG_PATIENT_SUCCESS);

                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    ex.printStackTrace();
                    MsgUtils.sendMsg(handler, ex, Constant.MSG_PATIENT_ERROR);

                }

                @Override
                public void onCancelled(CancelledException cex) {

                }

                @Override
                public void onFinished() {

                }
            });
            Constant.mCancelables.add(cancelable);
        } else {
            UploadQueUtils.makeDLCUploadQue(context, db, handler);
        }
    }

    public synchronized static void uploadDLCList(Context context, List<DailyCheckItem> items, final Handler handler, int position, DbManager db) {
        LogUtils.d(position + "uploadDLCList");
        if (Constant.sUploadUser == null)
            return;
        String content = JsonUtils.makeDailyCheckObservation(context, items.get(position));
        doPost(context, content, handler, Constant.CMD_TYPE_DLC, db);
    }

    public synchronized static void uploadECGList(Context context, List<ECGItem> items, final Handler handler, int position, DbManager db) {
        LogUtils.d(position + "uploadECGList");
        if (Constant.sUploadUser == null)
            return;
        String content = JsonUtils.makeECGObservation(context, items.get(position));
        doPost(context, content, handler, Constant.CMD_TYPE_ECG_LIST, db);
    }

    public synchronized static void uploadSPO2List(Context context, List<SPO2Item> items, final Handler handler, int position, DbManager db) {
        LogUtils.d(position + "uploadSPO2List");
        if (Constant.sUploadUser == null)
            return;
        String content = JsonUtils.makeSPO2Observation(context, items.get(position));
        doPost(context, content, handler, Constant.CMD_TYPE_SPO2, db);

    }

    public synchronized static void uploadSLMList(Context context, List<SLMItem> items, final Handler handler, int position, DbManager db) {
        LogUtils.d(position + "uploadSLMList");
        if (Constant.sUploadUser == null)
            return;
        String content = JsonUtils.makeSlmObservation(context, items.get(position));
        doPost(context, content, handler, Constant.CMD_TYPE_SLM_LIST, db);
    }

    public synchronized static void uploadTMPList(Context context, List<TempItem> items, final Handler handler, int position, DbManager db) {
        LogUtils.d(position + "uploadTMPList");
        if (Constant.sUploadUser == null)
            return;
        String content = JsonUtils.makeTemperatureObservation(context, items.get(position));
        doPost(context, content, handler, Constant.CMD_TYPE_TEMP, db);
    }

    private static void doPost(Context context, String content, Handler handler, int fileType, DbManager db) {
        /** 判断https证书是否成功验证 */
        /*SSLContext sslContext = InternetUtils.getSSLContext(context);
        if (null == sslContext) {
            if (BuildConfig.DEBUG)
                LogUtils.d("Error:Can't Get SSLContext!");
            return;
        }*/
        RequestParams params = makeParams(Constant.OBSERVATION_URL, context, content);
        postUtil(context, handler, params, fileType, db);
    }

    private static RequestParams makeParams(String url, Context context, String content) {
        RequestParams params = new RequestParams(url);
        params.addHeader("Authorization", InternetUtils.makeAuthorization(context));
        params.addHeader("Accept", "application/json+fhir");
        params.addHeader("Content-Type", "application/json+fhir");
        params.setAsJsonContent(true);
        params.setBodyContent(content);
        params.setConnectTimeout(Constant.CONNECT_TIMEOUT);
//        params.setSslSocketFactory(sslSocketFactory); //绑定SSL证书(https请求)
        params.setCancelFast(true);
        params.setHttpRetryHandler(new HttpRetryHandler());
        params.setMaxRetryCount(3);
        return params;
    }

    private static void postUtil(Context context, Handler handler, RequestParams params, int fileType, DbManager db) {
        Callback.Cancelable cancelable = x.http().post(params, new UploadCallback(context, handler, fileType, db));
        Constant.mCancelables.add(cancelable);
    }
}
