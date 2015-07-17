package com.example.bjoern.nativeload;


import android.util.Log;

/**
 * Created by bjoern on 29.05.15.
 */
public class BundleItem {

    private final String TAG = BundleItem.class.getName();

    private String filename;
    private String description;
    private int status;
    private Object statusInfo;
    private boolean checked;

    public static final int BUNDLE_NOT_YET_INITIALIZED = 1000;
    public static final int BUNDLE_LOCALLY_AVAILABLE = 1001;
    public static final int BUNDLE_DOWNLOAD_STARTED = 1002;
    public static final int BUNDLE_DOWNLOAD_COMPLETE = 1003;
    public static final int BUNDLE_UPDATE_PROGRESS_BAR = 1004;
    public static final int BUNDLE_CONNECTING_STARTED = 1005;
    public static final int BUNDLE_ENCOUNTERED_ERROR = 1006;


    public BundleItem(String filename, String description, boolean checked) {
        this.filename = filename;
        this.description = description;
        this.status = BundleItem.BUNDLE_NOT_YET_INITIALIZED;
        this.setChecked(checked);
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public Object getStatusInfo() {
        return statusInfo;
    }

    public void setStatusInfo(Object statusInfo) {
        this.statusInfo = statusInfo;
    }
}
