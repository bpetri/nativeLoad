package com.inaetics.demonstrator.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
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

    private Context context;

    private Model() {
        bundles = new ArrayList<>();
        handler = new Handler();
    }

    public void setContext(Context context) {
        config = new Config(context);

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
        String abi = Build.CPU_ABI;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String[] abis = Build.SUPPORTED_ABIS;
            for (String ab : abis) {
                try {
                    files = assetManager.list("celix_bundles/" + ab);
                    if (files != null && files.length > 0) {
                        abi = ab;
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                files = assetManager.list("celix_bundles/" + Build.CPU_ABI);
                if (files == null || files.length == 0) {
                    files = assetManager.list("celix_bundles/" + Build.CPU_ABI2);
                    abi = Build.CPU_ABI2;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.e("Bundles", "Using " + abi + " bundles");


        //Move bundles from assets to internal storage (/data/data/com.inaetics.demonstrator/celix_bundles
        for (String fileName : files) {
            File newFile = new File(bundleLocation + "/" + fileName);
            moveBundle(assetManager, newFile, fileName, abi);
        }
    }

    private void moveBundle(AssetManager assetManager, File newFile, String fileName, String abi) {
        try {
            InputStream in = null;
            in = assetManager.open("celix_bundles/" + abi + "/" + fileName);
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
