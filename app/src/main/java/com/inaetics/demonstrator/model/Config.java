package com.inaetics.demonstrator.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
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
    private Properties properties;
    private Context context;

    protected Config(Context context) {
        this.context = context;
        setup();
    }

    public void setup() {
        SharedPreferences prefs = context.getSharedPreferences("CelixAgent", Context.MODE_PRIVATE);
        String props = prefs.getString("celixConfig", null);
        if (props != null) {
            properties = stringToProperties(props);
        } else {
            properties = new Properties();
            properties.put("RSA_PORT", "20888");
            properties.put("DISCOVERY_CFG_SERVER_PORT", "20999");
            properties.put("org.osgi.framework.storage.clean", "onFirstInit");
            properties.put("LOGHELPER_ENABLE_STDOUT_FALLBACK", "true");
        }

        //Following variables will be set every startup
        String ipAdr = getLocalIpAddress();
        if (ipAdr != null) {
            String[] ip = ipAdr.split("\\.");
            properties.put("RSA_IP", ipAdr);
            properties.put("DISCOVERY_CFG_SERVER_IP", ipAdr);
            properties.put("deployment_admin_identification", "Android" + ip[ip.length - 1]);
        } else {
            Toast.makeText(context, "No ip address found! Are you connected?", Toast.LENGTH_LONG).show();
            properties.put("deployment_admin_identification", "Android" + (int) (Math.random() * 745 + 255));
        }
        properties.put("deployment_cache_dir", context.getCacheDir().getAbsolutePath());
        properties.put("org.osgi.framework.storage", context.getDir("cache", Context.MODE_PRIVATE).getAbsolutePath());

        writeProperties();
        prefs.edit().putString("celixConfig", propertiesToString()).apply();
    }

    public void putProperty(String key, String value) {
        properties.put(key, value);
        writeProperties();
        context.getSharedPreferences("CelixAgent", Context.MODE_PRIVATE).edit().putString("celixConfig", propertiesToString()).apply();
    }

    public void setProperties(String propertyString) {
        properties = stringToProperties(propertyString);
        writeProperties();
        context.getSharedPreferences("CelixAgent", Context.MODE_PRIVATE).edit().putString("celixConfig", propertiesToString()).apply();
    }

    public String getProperty(String key) {
        return properties.getProperty(key, "none");
    }

    public void removeProperty(String key) {
        properties.remove(key);
        writeProperties();
        context.getSharedPreferences("CelixAgent", Context.MODE_PRIVATE).edit().putString("celixConfig", propertiesToString()).apply();
    }

    private void writeProperties() {
        // Write properties
        try {
            FileOutputStream openFileOutput = context.openFileOutput(CONFIG_PROPERTIES, Context.MODE_PRIVATE);
            openFileOutput.write(propertiesToString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            Log.e("Config", "Error while retrieving IP Address: " + e.toString(), e);
        }

        return null;
    }

    private Properties stringToProperties(String s) {
        final Properties p = new Properties();
        try {
            p.load(new StringReader(s));
        }
        catch (IOException e) {
            Log.e("Config", "Error while converting string to properties: " + e.toString(), e);
        }
        return p;
    }

    public String propertiesToString() {
        String propStr = "";
        Enumeration<?> keys = properties.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String val = properties.getProperty(key);

            propStr += key + "=" + val + "\n";
        }
        return propStr;
    }

    public void setAutostart(ArrayList<BundleItem> autostart, String bundleLocation) {
        String bundles = "";
        for (BundleItem bundle : autostart) {
            if (bundle.isChecked()) {
                bundles += bundleLocation + "/" + bundle.getFilename() + " ";
            }
        }
        bundles = bundles.trim();
        putProperty("cosgi.auto.start.1", bundles);
    }
}
