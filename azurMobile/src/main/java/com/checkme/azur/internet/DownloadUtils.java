package com.checkme.azur.internet;

import android.os.Handler;

import org.xutils.common.Callback;
import org.xutils.common.task.PriorityExecutor;
import org.xutils.http.RequestParams;
import org.xutils.x;

/**
 * Created by wangxiaogang on 2017/2/20.
 */

public class DownloadUtils {
    public static Callback.Cancelable downloadPatch(String urlStr, String fileName, int fileType,
                                                    Handler handler){
        RequestParams params=new RequestParams(urlStr);
        params.setAutoResume(true);
        params.setAutoRename(false);
        params.setSaveFilePath(fileName);
        params.setExecutor(new PriorityExecutor(2,true));
        params.setCancelFast(true);
        Callback.Cancelable cancelable= x.http().get(params, new DownloadCallback(handler,fileType));
        return cancelable;
    } 
}
