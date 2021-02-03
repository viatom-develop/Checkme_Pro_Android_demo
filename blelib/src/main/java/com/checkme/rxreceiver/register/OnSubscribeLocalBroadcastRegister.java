package com.checkme.rxreceiver.register;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposables;

public class OnSubscribeLocalBroadcastRegister implements ObservableOnSubscribe<Intent> {
    private final Context context;
    private final IntentFilter intentFilter;
    private final LocalBroadcastManager localBroadcastManager;

    public OnSubscribeLocalBroadcastRegister(Context context, IntentFilter intentFilter) {
        this.context = context.getApplicationContext();
        this.intentFilter = intentFilter;
        localBroadcastManager = LocalBroadcastManager.getInstance(this.context);
    }

    @Override
    public void subscribe(ObservableEmitter<Intent> emitter){
        final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                emitter.onNext(intent);
            }
        };

        emitter.setDisposable(Disposables.fromRunnable(new Runnable() {
            @Override
            public void run() {
                localBroadcastManager.unregisterReceiver(broadcastReceiver);
            }
        }));

        localBroadcastManager.registerReceiver(broadcastReceiver, intentFilter);
    }
}
