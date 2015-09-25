package com.inaetics.demonstrator.model;

/**
 * Created by mjansen on 16-9-15.
 */
public enum BundleStatus {
    BUNDLE_NOT_YET_INITIALIZED,
    BUNDLE_LOCALLY_AVAILABLE,
    BUNDLE_DOWNLOAD_STARTED,
    BUNDLE_UPDATE_PROGRESS_BAR,
    BUNDLE_CONNECTING_STARTED,
    BUNDLE_ENCOUNTERED_ERROR,
    BUNDLE_INSTALLED,
    BUNDLE_RUNNING,
    BUNDLE_STOPPING,
    CELIX_RUNNING,
    CELIX_STOPPED
}
