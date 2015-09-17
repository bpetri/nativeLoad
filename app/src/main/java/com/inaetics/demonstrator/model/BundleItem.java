/**
 * Licensed under Apache License v2. See LICENSE for more information.
 */

package com.inaetics.demonstrator.model;

import android.util.Log;

public class BundleItem {

    private final String TAG = BundleItem.class.getName();

    private String filename;
    private String description;
    private BundleStatus status;
    private Object statusInfo;
    private boolean checked;

    public BundleItem(String filename, String description, boolean checked) {
        this.filename = filename;
        this.description = description;
        this.status = BundleStatus.BUNDLE_NOT_YET_INITIALIZED;
        this.checked = checked;
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

    public BundleStatus getStatus() {
        return status;
    }

    public void setStatus(BundleStatus status) {
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
