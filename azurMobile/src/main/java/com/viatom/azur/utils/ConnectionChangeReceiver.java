package com.viatom.azur.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;


import com.viatom.azur.EventBusEvent.NetWorkEvent;
import com.viatom.newazur.R;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by wangxiaogang on 2016/9/28.
 */

public class ConnectionChangeReceiver extends BroadcastReceiver {
    private static final String TAG = ConnectionChangeReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "网络状态改变");
        boolean success = false;

        //获得网络连接服务
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // State state = connManager.getActiveNetworkInfo().getState();
        // 获取WIFI网络连接状态
        NetworkInfo.State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        // 判断是否正在使用WIFI网络
        if (NetworkInfo.State.CONNECTED == state) {
            success = true;
            EventBus.getDefault().post(new NetWorkEvent(true));
        }

        if (!isPad(context)) {
            // 获取GPRS网络连接状态
            state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
            // 判断是否正在使用GPRS网络
            if (NetworkInfo.State.CONNECTED == state) {
                success = true;
                EventBus.getDefault().post(new NetWorkEvent(true));
            }
        }

        if (!success) {
            EventBus.getDefault().post(new NetWorkEvent(false));
            Toast.makeText(context.getApplicationContext(), context.getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isPad(Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephony.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE;
    }
}
