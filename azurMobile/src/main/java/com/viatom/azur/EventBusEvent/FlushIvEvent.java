package com.viatom.azur.EventBusEvent;

/**
 * Created by wangxiaogang on 2017/2/17.
 */

public class FlushIvEvent {
    private int uploadState;

    public FlushIvEvent(int uploadState) {
        this.uploadState = uploadState;
    }

    public int getUploadState() {
        return uploadState;
    }

    public void setUploadState(int uploadState) {
        this.uploadState = uploadState;
    }
}
