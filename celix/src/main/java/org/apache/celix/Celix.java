package org.apache.celix;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Scanner;

import org.apache.celix.model.CelixUpdate;
import org.apache.celix.model.OsgiBundle;

/**
 * Created by mjansen on 28-10-15.
 */
public class Celix extends Observable {
    private static Celix self;
    private String stdio;
    private Handler handler;
    private boolean celixRunning = false;

    static {
        System.loadLibrary("celix_utils");
        System.loadLibrary("celix_framework");
        System.loadLibrary("jni_part");
    }

    // INIT METHODS/CONSTRUCTORS
    private Celix() {
        handler = new Handler();
        initCallback();
        stdio = "";
    }

    /**
     * Singleton pattern, make sure you pass context.
     *
     * @return Celix instance
     */
    public static Celix getInstance() {
        if (self == null) {
            self = new Celix();
        }
        return self;
    }

    /**
     * Method for checking if celix is running
     * @return
     */
    public boolean isCelixRunning() {
        return celixRunning;
    }

    // METHODS

    /**
     * Installs the bundle from specified path
     *
     * @param bundlePath
     */
    public void installBundle(String bundlePath) {
        bundleInstall(bundlePath);
    }

    public void installStartBundle(String bundlePath) {
        bundleInstallStart(bundlePath);
    }

    /**
     * Starts the bundle from specified path
     *
     * @param absolutePath Path where the bundle (.zip) is located
     */
    public void startBundle(String absolutePath) {
        bundleStart(absolutePath);
    }

    public void startBundleById(long id) {
        bundleStartById(id);
    }


    public void stopBundle(String bundlePath) {
        bundleStop(bundlePath);
    }

    public void stopBundleById(long id) {
        bundleStopById(id);
    }

    public void deleteBundle(String bundlePath) {
        bundleDelete(bundlePath);
    }
    public void deleteBundleById(long id) {
        bundleDeleteById(id);
    }

    /**
     * Starts the celix framework with the config.properties in getFilesDir()
     */
    public void startFramework(String configPath) {
        startCelix(configPath);
    }

    /**
     * Stops the celix framework.
     */
    public void stopFramework() {
        stopCelix();
    }

    /**
     * Get the id/status/name of a bundle
     *
     * @return String array containing strings in format "id status name"
     */
    public String[] getBundles() {
        return printBundles();
    }

    /**
     * Get a list of OsgiBundles containing a id, status and name
     * This list is sorted by id.
     *
     * @return List of OsgiBundles
     */
    public List<OsgiBundle> getBundlesInList() {
        ArrayList<OsgiBundle> bundles = new ArrayList<>();
        bundles.clear();
        String[] bundleStrings = printBundles();
        for (String str : bundleStrings) {
            Scanner sc = new Scanner(str);
            long id = sc.nextLong();
            String status = sc.next();
            String symbolicName = sc.next();
            String location = sc.next();
            bundles.add(new OsgiBundle(symbolicName, status, id, location));
            sc.close();
        }


        Collections.sort(bundles, new Comparator<OsgiBundle>() {
            @Override
            public int compare(OsgiBundle osgiBundle, OsgiBundle t1) {
                if (osgiBundle.getId() > t1.getId()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return bundles;
    }

    /**
     * Returns latest C logs, clears after.
     * Only returns newest log items since last call.
     * @return      String with logs.
     */
    public String getStdio() {
        String result = new String(stdio);
        stdio = "";
        return result;
    }

    // CALLBACK

    private void setCelixRunning(boolean isRunning) {
        this.celixRunning = isRunning;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setChanged();
                notifyObservers(CelixUpdate.CELIX_CHANGED);
            }
        });
    }

    private void confirmLogChanged(String newLine) {
        if (stdio.length() > 25000) {
            stdio = stdio.substring(10000);
        }
        stdio += newLine + "\n";

        handler.post(new Runnable() {
            @Override
            public void run() {
                setChanged();
                notifyObservers(CelixUpdate.LOG_CHANGED);
            }
        });
    }

    private void bundleChanged(String bundle) {
        Scanner sc = new Scanner(bundle);
        long id = sc.nextLong();
        String status = sc.next();
        String symbolicName = sc.next();
        String location = "";
        if (sc.hasNext()) {
            location = sc.next();
        }
        sc.close();


        OsgiBundle osgiBundle = new OsgiBundle(symbolicName, status, id, location);
        final OsgiBundle finalOsgiBundle = osgiBundle;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setChanged();
                notifyObservers(finalOsgiBundle);

            }
        });
    }


    //--------------Native methods-----------------

    private native int startCelix(String propertyString);
    private native int stopCelix();

    private native int bundleInstall(String path);

    private native int bundleInstallStart(String path);

    private native int bundleStart(String path);
    private native int bundleStartById(long id);

    private native int bundleStop(String path);
    private native int bundleStopById(long id);

    private native int bundleDelete(String path);
    private native int bundleDeleteById(long id);

    private native String[] printBundles();
    private native int initCallback();


}
