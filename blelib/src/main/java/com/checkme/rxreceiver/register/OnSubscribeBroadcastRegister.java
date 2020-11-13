package com.checkme.rxreceiver.register;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import java.lang.ref.WeakReference;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposables;

public class OnSubscribeBroadcastRegister implements ObservableOnSubscribe<Intent> {

    private final WeakReference<Context> contextWeakReference;
    private final IntentFilter intentFilter;
    private final String broadcastPermission;
    private final Handler schedulerHandler;

    public OnSubscribeBroadcastRegister(Context context, IntentFilter intentFilter, String broadcastPermission, Handler schedulerHandler) {
        this.contextWeakReference = new WeakReference<Context>(context.getApplicationContext());
        this.intentFilter = intentFilter;
        this.broadcastPermission = broadcastPermission;
        this.schedulerHandler = schedulerHandler;
    }

    @Override
    public void subscribe(ObservableEmitter<Intent> emitter) {
        final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                emitter.onNext(intent);
            }
        };

        emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
            @Override
            public void run() {
                if (contextWeakReference != null && contextWeakReference.get() != null) {
                    contextWeakReference.get().unregisterReceiver(broadcastReceiver);
                }
            }
        }));

        if (contextWeakReference != null && contextWeakReference.get() != null) {
            contextWeakReference.get().registerReceiver(broadcastReceiver, intentFilter, broadcastPermission, schedulerHandler);
        }
    }
}
