package com.inaetics.demonstrator.model;

import android.content.res.AssetManager;
import android.os.Handler;
import android.util.Log;

import com.inaetics.demonstrator.JNICommunicator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Observable;


/**
 * Created by mjansen on 16-9-15.
 */
public class Model extends Observable {

    private static Model self;
    private ArrayList<BundleItem> bundles;
    private Config config;
    private String bundleLocation;

    private Handler handler;
    private JNICommunicator jniCommunicator;
    private BundleStatus celixStatus;

    private Model() {
        config = new Config();
        bundles = new ArrayList<>();
        handler = new Handler();
    }

    public static Model getInstance() {
        if (self == null) {
            self = new Model();
        }
        return self;
    }

    public void initJNI() {
        if (jniCommunicator == null) {
            jniCommunicator = new JNICommunicator(handler);
        }
    }

    public BundleItem addBundle(String fileName, String description, boolean checked) {
        BundleItem item = new BundleItem(fileName, description, checked);
        bundles.add(item);
        return item;
    }

    public BundleStatus getCelixStatus() {
        return celixStatus;
    }

    public void setCelixStatus(BundleStatus status) {
        if (status != celixStatus) {
            celixStatus = status;
            setChanged();
            notifyObservers(celixStatus);
        }
    }

    public BundleItem addBundle(String fileName, boolean checked) {
        for (BundleItem b : bundles) {
            if (fileName.equals(b.getFilename())) {
                return null;
            }
        }
        return addBundle(fileName, "", checked);
    }

    public ArrayList<BundleItem> getBundles() {
        return bundles;
    }

    public String getBundleLocation() {
        return bundleLocation;
    }

    public void setBundleLocation(String bundleLocation) {
        this.bundleLocation = bundleLocation;
    }

    public Config getConfig() {
        return config;
    }

    public JNICommunicator getJniCommunicator() {
        return jniCommunicator;
    }

    /**
     * Method for moving the files from the assets to the internal storage
     * so the C code can reach it
     */
    public void moveBundles(AssetManager assetManager) {

        String[] files = null;

        Log.e("Arch", System.getProperty("os.arch"));
        if (System.getProperty("os.arch").contains("v7") ||
                System.getProperty("os.arch").contains("v8")) {
            try {
                files = assetManager.list("celix_bundles/armeabi-v7a");
            } catch (IOException e) {
                e.printStackTrace();
            }
            useBundlesv7 = true;
            Log.e("Bundles","Using v7a bundles");
        } else {
            // Use armeabi bundles
            try {
                files = assetManager.list("celix_bundles/armeabi");
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e("Bundles","Using armeabi bundles");
        }

        //Move bundles from assets to internal storage (/data/data/com.inaetics.demonstrator/celix_bundles
        for (String fileName : files) {

            File newFile = new File(bundleLocation + "/" + fileName);
//            if (!newFile.isFile()) {
            moveBundle(assetManager, newFile, fileName);
//            }
        }
    }

    private boolean useBundlesv7;

    private void moveBundle(AssetManager assetManager, File newFile, String fileName) {
        try {
            InputStream in = null;
            if (useBundlesv7) {
                in = assetManager.open("celix_bundles/armeabi-v7a/" + fileName);
            } else {
                in = assetManager.open("celix_bundles/armeabi/" + fileName);
            }

            FileOutputStream out = new FileOutputStream(newFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
            Log.i("BundleMover", fileName + " copied to " + bundleLocation);
        } catch (Exception e) {
            Log.e("BundleMover", "ERROR: " + e.toString());
        }
    }

    public BundleItem getBundleFromLocation(String location) {
        String[] words = location.split("/");
        String fileName = words[words.length - 1];
        for (BundleItem b : bundles) {
            if (fileName.equals(b.getFilename())) {
                return b;
            }
        }
        return null;
    }

    public void setBundleInstall(String location) {
        BundleItem b = getBundleFromLocation(location);
        if (b != null) {
            b.setStatus(BundleStatus.BUNDLE_INSTALLED);
            Log.d("Model", "Bundle " + b.getFilename() + " has been installed");
            setChanged();
            notifyObservers();
        }
    }

    public void setBundleDelete(String location) {
        BundleItem b = getBundleFromLocation(location);
        if (b != null) {
            b.setStatus(BundleStatus.BUNDLE_LOCALLY_AVAILABLE);
            Log.d("Model", "Bundle " + b.getFilename() + " has been deleted");
            setChanged();
            notifyObservers();
        }
    }

    public void setBundleStart(String location) {
        BundleItem b = getBundleFromLocation(location);
        if (b != null) {
            b.setStatus(BundleStatus.BUNDLE_RUNNING);
            Log.d("Model", "Bundle " + b.getFilename() + " has been started");
            setChanged();
            notifyObservers();
        }
    }

    public void setBundleStop(String location) {
        BundleItem b = getBundleFromLocation(location);
        if (b != null) {
            b.setStatus(BundleStatus.BUNDLE_INSTALLED);
            Log.d("Model", "Bundle " + b.getFilename() + " has been stopped");
            setChanged();
            notifyObservers();
        }
    }

    public void resetBundles() {
        for(BundleItem bundle : bundles) {
            bundle.setStatus(BundleStatus.BUNDLE_LOCALLY_AVAILABLE);
        }
        setChanged();
        notifyObservers();
    }
}
