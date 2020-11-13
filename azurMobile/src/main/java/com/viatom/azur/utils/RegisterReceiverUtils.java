package com.viatom.azur.utils;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

import static com.github.mikephil.charting.charts.Chart.LOG_TAG;

/**
 * Created by wangxiaogang on 2016/9/28.
 */

public class RegisterReceiverUtils {

    private static ConnectionChangeReceiver mConnectionChangeReceiver = null;

    public static void registerConnectionChangeReceiver(Context context) {
        if (mConnectionChangeReceiver == null)
            mConnectionChangeReceiver = new ConnectionChangeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(mConnectionChangeReceiver, filter);
    }

    public static void unregisterConnectionChangeReceiver(Context context) {
        Log.i(LOG_TAG, "unregisterHomeKeyReceiver");
        if (null != mConnectionChangeReceiver) {
            context.unregisterReceiver(mConnectionChangeReceiver);
        }
    }

}
