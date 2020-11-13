package com.viatom.azur.internet;

import android.content.Context;
import android.os.Handler;

import com.viatom.azur.EventBusEvent.FlushIvEvent;
import com.viatom.azur.activity.BaseActivity;
import com.viatom.azur.element.Constant;
import com.viatom.azur.utils.LogUtils;
import com.viatom.azur.utils.MsgUtils;
import com.viatom.azur.utils.ObservationResponseUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.common.Callback;
import org.xutils.x;

/**
 * Created by wangxiaogang on 2017/5/9.
 */

public class UploadCallback implements Callback.ProgressCallback<JSONObject> {
    private Handler handler;
    private int fileType;
    private DbManager db;
    private Context context;

    public UploadCallback(Context context,Handler handler, int fileType,DbManager db) {
        this.context=context;
        this.handler = handler;
        this.fileType = fileType;
        this.db=db;
    }

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
        x.task().run(new Runnable() {
            @Override
            public void run() {
                switch (fileType){
                    case Constant.CMD_TYPE_DLC:
                        ObservationResponseUtils.DLCResponse(context, handler, db);
                        break;
                    case Constant.CMD_TYPE_ECG_LIST:
                        ObservationResponseUtils.ECGResponse(context, handler, db);
                        break;
                    case Constant.CMD_TYPE_SPO2:
                        ObservationResponseUtils.SPO2Response(context, handler, db);
                        break;
                    case Constant.CMD_TYPE_SLM_LIST:
                        ObservationResponseUtils.SLMResponse(context, handler, db);
                        break;
                    case Constant.CMD_TYPE_TEMP:
                        ObservationResponseUtils.TMPResponse(context, handler, db);
                        break;
                }
            }
        });

    }

    @Override
    public void onError(Throwable ex, boolean isOnCallback) {
        ex.printStackTrace();
        MsgUtils.sendMsg(handler, Constant.MSG_OBSERVATION_ERROR, fileType);

    }

    @Override
    public void onCancelled(CancelledException cex) {

    }

    @Override
    public void onFinished() {

    }
}
