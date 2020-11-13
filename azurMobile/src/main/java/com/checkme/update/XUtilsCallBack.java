package com.checkme.update;

import org.xutils.common.Callback;

/**
 * Created by gongguopei on 2018/4/10.
 */

public class XUtilsCallBack<ResultType> implements Callback.CommonCallback<ResultType> {
    @Override
    public void onSuccess(ResultType result) {

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
