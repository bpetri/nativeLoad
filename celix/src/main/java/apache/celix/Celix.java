package apache.celix;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Scanner;

import apache.celix.model.Update;
import apache.celix.model.OsgiBundle;

/**
 * Created by mjansen on 28-10-15.
 * Wrapper class which has methods to jni_part.c which communicates with the real framework
 * Is observable and can notify when log is changed, bundle changed/installed
 * or when celix has been started/stopped
 */
public class Celix extends Observable {
    private static Celix self;
    private String stdio;
    private Handler handler;
    private boolean celixRunning = false;

    //Load libraries
    static {
        System.loadLibrary("celix_utils");
        System.loadLibrary("celix_dfi");
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
     * @return  true is celix is running, false if not
     */
    public boolean isCelixRunning() {
        return celixRunning;
    }

    /**
     * Installs the bundle from specified location
     * @param bundlePath    Path where the bundle(.zip) is located
     */
    public void installBundle(String bundlePath) {
        bundleInstall(bundlePath);
    }

    /**
     * Installs and starts a bundle from specified location
     * @param bundlePath    Path where the bundle (.zip) is located
     */
    public void installStartBundle(String bundlePath) {
        bundleInstallStart(bundlePath);
    }

    /**
     * Starts the bundle from specified location
     * @param absolutePath Path where the bundle (.zip) is located
     */
    public void startBundle(String absolutePath) {
        bundleStart(absolutePath);
    }

    /**
     * Starts bundle with specified ID
     * @param id    Id of bundle you want to start.
     */
    public void startBundleById(long id) {
        bundleStartById(id);
    }

    /**
     * Stops bundle with specified location
     * @param bundlePath    Path where the bundle (.zip) is located
     */
    public void stopBundle(String bundlePath) {
        bundleStop(bundlePath);
    }

    /**
     * Stop bundle with specified id
     * @param id    id of the bundle you want to stop
     */
    public void stopBundleById(long id) {
        bundleStopById(id);
    }

    /**
     * Delete bundle with specified location
     * @param bundlePath    Path where the bundle (.zip) is located.
     */
    public void deleteBundle(String bundlePath) {
        bundleDelete(bundlePath);
    }

    /**
     * Delete bundle with specified id
     * @param id    Id of the bundle you want to delete
     */
    public void deleteBundleById(long id) {
        bundleDeleteById(id);
    }

    /**
     * Starts the framework with the config.properties which are specified
     * @param configPath    Path where the config.properties file is located
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
     * @return String array containing strings in format "id status name"
     */
    public String[] getBundles() {
        return printBundles();
    }

    /**
     * Get a list of OsgiBundles containing a id, status and name
     * This list is sorted by id.
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

    /**
     * Callback method, is being called from jni_part.c when celix has changed
     * @param isRunning Boolean if it is now running true=running false=notrunning
     */
    private void setCelixRunning(boolean isRunning) {
        this.celixRunning = isRunning;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setChanged();
                notifyObservers(Update.CELIX_CHANGED);
            }
        });
    }

    /**
     * Callback method, is being called from jni_part.c when log has changed
     * @param newLine   The new log line
     */
    private void confirmLogChanged(String newLine) {
        if (stdio.length() > 25000) {
            stdio = stdio.substring(10000);
        }
        stdio += newLine + "\n";

        handler.post(new Runnable() {
            @Override
            public void run() {
                setChanged();
                notifyObservers(Update.LOG_CHANGED);
            }
        });
    }

    /**
     * Callback method, is being called from jni_part.c when a bundle has been changed/installed
     * @param bundle    BundleString which contains all the information of this bundle
     */
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

        final OsgiBundle osgiBundle = new OsgiBundle(symbolicName, status, id, location);
        handler.post(new Runnable() {
            @Override
            public void run() {
                setChanged();
                notifyObservers(osgiBundle);

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
