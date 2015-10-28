package apache.celix;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import apache.celix.model.Config;
import apache.celix.model.OsgiBundle;

/**
 * Created by mjansen on 28-10-15.
 */
public class Celix {
    private Context context;
    private static Celix self;

    static {
        System.loadLibrary("celix_utils");
        System.loadLibrary("celix_framework");
        System.loadLibrary("jni_part");
    }

    // INIT METHODS/CONSTRUCTORS
    private Celix() {
    }

    /**
     * Singleton pattern, make sure you pass context.
     *
     * @return Celix instance
     */
    public static Celix getInstance() {
        if (self == null)
            self = new Celix();
        return self;
    }

    /**
     * Method to pass context to the celix object.
     * This context is needed to do initialize the celix framework.
     *
     * @param context Application context, ('this' in MainActivity)
     */
    public void setContext(Context context) {
        this.context = context;
    }


    // METHODS

    /**
     * Starts the bundle from specified path
     *
     * @param absolutePath Path where the bundle (.zip) is located
     */
    public void startBundle(String absolutePath) {
        bundleStart(absolutePath);
    }

    /**
     * Installs the bundle from specified path
     *
     * @param bundlePath
     */
    public void installBundle(String bundlePath) {
        bundleInstall(bundlePath);
    }

    public void stopBundle(String bundlePath) {
        bundleStop(bundlePath);
    }

    public void deleteBundle(String bundlePath) {
        bundleDelete(bundlePath);
    }

    /**
     * Starts the celix framework with the config.properties in getFilesDir()
     */
    public void startFramework() {
        startCelix(context.getApplicationContext().getFilesDir() + "/" + Config.CONFIG_PROPERTIES);
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
        String[] bundleStrings = printBundles();
        for (String str : bundleStrings) {
            Scanner sc = new Scanner(str);
            long id = sc.nextLong();
            String status = sc.next();
            String symbolicName = sc.next();
            bundles.add(new OsgiBundle(symbolicName, status, id));
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

    public void printcmessage() {
        printcmsg();
    }

    //--------------Native methods-----------------
    private native int printcmsg();

    private native int initJni();

    private native int startCelix(String propertyString);

    private native int stopCelix();

    private native int bundleInstall(String path);

    private native int bundleStart(String path);

    private native int bundleStop(String path);

    private native int bundleDelete(String path);

    private native String[] printBundles();


}
