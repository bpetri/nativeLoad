package com.inaetics.demonstrator;

import android.os.Handler;
import android.util.Log;

import com.inaetics.demonstrator.model.BundleStatus;
import com.inaetics.demonstrator.model.Model;

/**
 * Created by dveldhof on 16-9-15.
 */
public class JNICommunicator{

    private Handler handler;
    private Model model;

    public JNICommunicator(Handler handler) {
        this.handler = handler;
        this.model = Model.getInstance();

        initJni();
//        installBundle(model.getBundleLocation() + "/echo_client.zip");
//        installBundle(model.getBundleLocation() + "/echo_server.zip");
    }

    //-------------Callback methods--------------

    /**
     * Callback method called by jni_part when celix is started correctly
     * Changes the status of celix in the model
     */
    public void confirmCelixStart() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.e("JNICommunicator", "Celix started correctly");
                Model.getInstance().setCelixStatus(BundleStatus.CELIX_RUNNING);
                installBundle(model.getBundleLocation() + "/echo_client.zip");
                installBundle(model.getBundleLocation() + "/echo_server.zip");
            }
        });
    }

    /**
     * Callback method called by jni_part when celix is stopped correctly
     * Changes the status of celix in the model
     */
    public void confirmCelixStop() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Model.getInstance().setCelixStatus(BundleStatus.CELIX_STOPPED);
            }
        });
    }

    /**
     * Callback method called by jni_part when a bundle is installed correctly
     * Sets the status of the bundle to installed
     * @param bundleLocation location of the installed bundle
     */
    public void confirmBundleInstalled(final String bundleLocation) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                model.setBundleInstall(bundleLocation);
            }
        });
    }

    /**
     * Callback method called by jni_part when a bundle is started correctly
     * Sets the status of the bundle to started
     * @param bundleLocation location of the started bundle
     */
    public void confirmBundleStart(final String bundleLocation) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                model.setBundleStart(bundleLocation);
            }
        });
    }

    /**
     * Callback method called by jni_part when a bundle is stopped correctly
     * Sets the status of the bundle to stopped
     * @param bundleLocation location of the stopped bundle
     */
    public void confirmBundleStop(String bundleLocation) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                //Set the status of the bundle to stopped
            }
        });
    }

    /**
     * Callback method called by jni_part when a bundle is deleted correctly
     * Sets the status of the bundle to deleted
     * @param bundleLocation location of the deleted bundle
     */
    public void confirmBundleDeleted(String bundleLocation) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                //Set the status of the bundle to deleted
            }
        });
    }


    //--------------Native methods-----------------

    public native int startCelix(String propertyString);
    public native int stopCelix();
    public native int initJni();
    public native int installBundle(String path);
}
