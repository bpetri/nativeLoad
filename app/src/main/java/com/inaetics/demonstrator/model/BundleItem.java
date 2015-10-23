/**
 * Licensed under Apache License v2. See LICENSE for more information.
 */

package com.inaetics.demonstrator.model;


public class BundleItem {
    private String filename;
    private BundleStatus status;
    private boolean checked;

    public BundleItem(String filename, boolean checked) {
        this.filename = filename;
        this.status = BundleStatus.BUNDLE_NOT_YET_INITIALIZED;
        this.checked = checked;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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


}
