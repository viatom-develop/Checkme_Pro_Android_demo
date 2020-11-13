package com.viatom.azur.element;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by wangxiaogang on 2017/2/16.
 */

@Table(name = "UploadedItem", onCreated = "CREATE UNIQUE INDEX index_name ON UploadedItem(date)")
public class UploadedItem {
    @Column(name = "date",isId = true,autoGen = false)
    private String date;
    @Column(name = "isUploaded")
    private boolean isUploaded;
    @Column(name = "deviceName")
    private String deviceName;

    public UploadedItem() {
    }

    public UploadedItem(String date, boolean isUploaded, String deviceName) {
        this.date = date;
        this.isUploaded = isUploaded;
        this.deviceName=deviceName;
    }

    public boolean isUploaded() {
        return isUploaded;
    }

    public void setUploaded(boolean uploaded) {
        isUploaded = uploaded;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
