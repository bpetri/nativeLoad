package com.inaetics.demonstrator.model;

/**
 * Created by mjansen on 16-9-15.
 */
public enum BundleStatus {
    BUNDLE_NOT_YET_INITIALIZED,
    BUNDLE_LOCALLY_AVAILABLE,
    BUNDLE_INSTALLING,
    BUNDLE_INSTALLED,
    BUNDLE_STARTING,
    BUNDLE_RUNNING,
    BUNDLE_STOPPING,
    BUNDLE_DELETING,
    CELIX_RUNNING,
    CELIX_STOPPED
}
