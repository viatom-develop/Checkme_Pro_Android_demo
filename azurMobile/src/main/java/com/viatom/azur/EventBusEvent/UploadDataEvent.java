package com.viatom.azur.EventBusEvent;

/**
 * Created by wangxiaogang on 2017/2/17.
 */

public class UploadDataEvent {
    private boolean isRightToUpload;

    public UploadDataEvent(boolean isRightToUpload) {
        this.isRightToUpload = isRightToUpload;
    }

    public boolean isRightToUpload() {
        return isRightToUpload;
    }

    public void setRightToUpload(boolean rightToUpload) {
        isRightToUpload = rightToUpload;
    }
}
