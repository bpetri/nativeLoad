package com.inaetics.demonstrator.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import apache.celix.model.Config;
import apache.celix.model.OsgiBundle;


/**
 * Created by mjansen on 16-9-15.
 * Class that holds most of the information of the application
 */
public class Model extends Observable {

    private static Model self;
    private boolean bundlesMoved;
    private ArrayList<OsgiBundle> osgiBundles;
    private MyConfig config;
    private String bundleLocation;

    private Model() {
        bundlesMoved = false;
        osgiBundles = new ArrayList<>();
    }

    public void setContext(Context context) {
        config = new MyConfig(context);
    }

    /**
     * Method for retrieving all OSGi bundles that are available
     * @return ArrayList of Osgibundles that are available
     */
    public ArrayList<OsgiBundle> getOsgiBundles() {
        return osgiBundles;
    }

    public static Model getInstance() {
        if (self == null) {
            self = new Model();
        }
        return self;
    }

    /**
     * Method to check if the bundles are moved
     * @return true if bundles are moved, else false
     */
    public boolean areBundlesMoved() {
        return bundlesMoved;
    }

    /**
     * Method for retrieving the directory of all the Osgi bundles
     * @return location of the directory were all the Osgi are located
     */
    public String getBundleLocation() {
        return bundleLocation;
    }

    /**
     * Method to set the directory of all the Osgi bundles
     * @param bundleLocation location of all the Osgi bundles
     */
    public void setBundleLocation(String bundleLocation) {
        this.bundleLocation = bundleLocation;
    }

    /**
     * Method for retrieving the configuration
     * @return MyConfig with configuration for Celix
     */
    public MyConfig getConfig() {
        return config;
    }

    /**
     * Method for moving the files from the assets to the internal storage
     * so the C code can reach it
     */
    public void moveBundles(AssetManager assetManager) {

        String[] files = null;

        //Get cpu_abi for SDK versions above API 21
        String cpu_abi = Build.CPU_ABI;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String[] abis = Build.SUPPORTED_ABIS;
            for (String ab : abis) {
                try {
                    files = assetManager.list("celix_bundles/" + ab);
                    if (files != null && files.length > 0) {
                        cpu_abi = ab;
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            //Get cpu_abi for SDK version below API 21
            try {
                files = assetManager.list("celix_bundles/" + Build.CPU_ABI);
                if (files == null || files.length == 0) {
                    files = assetManager.list("celix_bundles/" + Build.CPU_ABI2);
                    cpu_abi = Build.CPU_ABI2;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.e("Bundles", "Using " + cpu_abi + " bundles");


        if (files != null) {
            //Move bundles from assets to internal storage (/data/data/com.inaetics.demonstrator/celix_bundles
            for (String fileName : files) {
                File bundleFile = new File(bundleLocation + "/" + fileName);
                try {
                    InputStream in = assetManager.open("celix_bundles/" + cpu_abi + "/" + fileName);
                    if (moveBundle(in, bundleFile)) {
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            bundlesMoved = true;
        } else {
            Log.e("Zips", "No zips for supported abis found!");
        }
    }

    /**
     * Moves the File (bundle) from the Assets folder to the internal storage
     * @param in InputStream to read from
     * @param newFile File to write to
     * @return true if bundles has been succesfuly moved, false if not
     */
    private boolean moveBundle(InputStream in, File newFile) {
        String fileName = newFile.getName();
        try {
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
            return true;
        } catch (Exception e) {
            Log.e("BundleMover", "ERROR: " + e.toString());
            return false;
        }
    }
}
