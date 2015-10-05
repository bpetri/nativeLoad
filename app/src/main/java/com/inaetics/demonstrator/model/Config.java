package com.inaetics.demonstrator.model;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by mjansen on 16-9-15.
 */
public class Config {
    public static final String CONFIG_PROPERTIES = "config.properties";

    protected Config() {
    }

    public String getLocalIpAddress()
    {

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Config", "Error while retrieving IP Address: " +  e.toString(), e);
        }

        return null;
    }

    public Properties stringToProperties(String s) {
        final Properties p = new Properties();
        try {
            p.load(new StringReader(s));
        }
        catch (IOException e) {
            Log.e("Config", "Error while converting string to properties: " + e.toString(), e);
        }
        return p;
    }

    public String propertiesToString(Properties props) {

        String propStr = "";

        Enumeration<?> keys = props.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String val = props.getProperty(key);

            propStr += key + "=" + val + System.getProperty("line.separator");
        }

        return propStr;
    }


    public Properties generateConfiguration(Properties cfg, ArrayList<BundleItem> bundles, String bundleLocation, Context context) {


        if (cfg == null) {
            cfg = new Properties();
        }

        /* bundle configuration */
        String bundleCfg = cfg.getProperty("cosgi.auto.start.1","");

        for(BundleItem bundle : bundles) {

            String fullBundlePath = bundleLocation + "/" + bundle.getFilename();

            if (bundle.isChecked() && bundle.getStatus() == BundleStatus.BUNDLE_LOCALLY_AVAILABLE && !bundleCfg.contains(fullBundlePath)) {
                bundleCfg += " " + fullBundlePath;
            }
            else if (!bundle.isChecked() && bundleCfg.contains(fullBundlePath)){
                bundleCfg = bundleCfg.replace(fullBundlePath, "");
            }

        }

        cfg.put("cosgi.auto.start.1", bundleCfg);

        /* cache directory */
        if (!cfg.containsKey("org.osgi.framework.storage")) {
            File cacheDir = context.getDir("cache", Context.MODE_PRIVATE);
            cfg.put("org.osgi.framework.storage", cacheDir.getAbsolutePath());

            if(cacheDir.isDirectory()) {
                Log.d("Config", "Cache dir " + cacheDir.toString() + " found.");
            }
            else if (!cacheDir.mkdir() ) {
                Log.e("Config", "Creation of cache dir " + cacheDir.toString() + " failes.");
            }
        }

        // IPv4 only!
        String ipAdr = getLocalIpAddress();
        if(!cfg.containsKey("RSA_IP")) {
            cfg.put("RSA_IP", ipAdr);
        }

        if (!cfg.containsKey("RSA_PORT")) {
            cfg.put("RSA_PORT", "20888");
        }

        if(!cfg.containsKey("DISCOVERY_CFG_SERVER_IP")) {
            cfg.put("DISCOVERY_CFG_SERVER_IP", ipAdr);
        }

        if (!cfg.containsKey("DISCOVERY_CFG_SERVER_PORT")) {
            cfg.put("DISCOVERY_CFG_SERVER_PORT", "20999");
        }

        if (!cfg.containsKey("LOGHELPER_ENABLE_STDOUT_FALLBACK")) {
            cfg.put("LOGHELPER_ENABLE_STDOUT_FALLBACK", "true");
        }
        if (!cfg.containsKey("org.osgi.framework.storage.clean")) {
            cfg.put("org.osgi.framework.storage.clean", "onFirstInit");
        }

        return cfg;
    }


    public boolean writeConfiguration(Context ctx, String cfgStr) {
        BufferedWriter writer = null;
        boolean retVal = true;
        try {
            FileOutputStream openFileOutput = ctx.openFileOutput(CONFIG_PROPERTIES, Context.MODE_PRIVATE);
            openFileOutput.write(cfgStr.getBytes());
        } catch (Exception e) {
            retVal = false;
            throw new RuntimeException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.e("Config", "Error while writing configuration: " +  e.toString(), e);
                }
            }
        }

        return retVal;
    }
}
