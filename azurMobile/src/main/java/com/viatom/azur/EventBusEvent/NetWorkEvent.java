package com.viatom.azur.EventBusEvent;

/**
 * Created by wangxiaogang on 2016/9/28.
 */

public class NetWorkEvent {
    private boolean isConnected;

    public NetWorkEvent(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }
}
