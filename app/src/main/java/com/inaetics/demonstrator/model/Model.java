package com.inaetics.demonstrator.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;


/**
 * Created by mjansen on 16-9-15.
 */
public class Model {
    private ArrayList<BundleItem> bundles;
    private static Model self;
    private Config config;
    private String bundleLocation;

    private Model() {
        config = new Config();
        bundles = new ArrayList<>();
    }

    public static Model getInstance() {
        if (self == null) {
            self = new Model();
        }
        return self;
    }

    public BundleItem addBundle(String fileName, String description, boolean checked) {
        BundleItem item = new BundleItem(fileName,description,checked);
        bundles.add(item);
        return item;
    }

    public BundleItem addBundle(String fileName) {
        return addBundle(fileName,"",false);
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

    /**
     * Method for moving the files from the assets to the internal storage
     * so the C code can reach it
     */
    public void moveBundles(AssetManager assetManager) {

        String[] files = null;

        try {
            files = assetManager.list("celix_bundles"); //assets/celix_bundles
        } catch (Exception e) {
            Log.e("BundleMover", "ERROR: " + e.toString());
        }

        //Move bundles from assets to internal storage (/data/data/com.inaetics.demonstrator/celix_bundles
        for (String fileName : files) {

            File newFile = new File(bundleLocation + "/" + fileName);
            if (!newFile.isFile()) {
                try {
                    InputStream in = assetManager.open("celix_bundles/" + fileName);
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
        }
    }

}
