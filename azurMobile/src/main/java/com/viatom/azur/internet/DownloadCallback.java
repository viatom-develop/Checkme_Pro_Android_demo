package com.viatom.azur.internet;

import android.os.Handler;

import com.viatom.azur.element.Constant;
import com.viatom.azur.utils.LogUtils;
import com.viatom.azur.utils.MsgUtils;

import org.xutils.common.Callback;

import java.io.File;

/**
 * Created by wangxiaogang on 2017/5/9.
 */

public class DownloadCallback implements Callback.ProgressCallback<File>{
    private Handler mHandler;
    private int fileType;
    DownloadCallback(Handler handler, int fileType) {
        this.mHandler=handler;
        this.fileType=fileType;
    }

    @Override
    public void onWaiting() {

    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onLoading(long total, long current, boolean isDownloading) {
        if (isDownloading){
            int progress = (Double.valueOf((current * 1.0 / total * 100))).intValue();
            LogUtils.d(progress+"%");
            MsgUtils.sendMsg(mHandler, Constant.MSG_DOWNLOAD_PART_FINISH, fileType,progress);
        }
    }

    @Override
    public void onSuccess(File result) {

    }

    @Override
    public void onError(Throwable ex, boolean isOnCallback) {

    }

    @Override
    public void onCancelled(CancelledException cex) {

    }

    @Override
    public void onFinished() {

    }
}
