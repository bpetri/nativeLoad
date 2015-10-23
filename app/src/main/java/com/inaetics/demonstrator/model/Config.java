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
 * Class used for keeping all information related to the config.properties.
 * Contains a properties object with the same information as the config.properties file
 */
public class Config {
    public static final String CONFIG_PROPERTIES = "config.properties";
    private Properties properties;
    private Context context;

    Config(Context context) {
        this.context = context;
        setup();
    }

    /**
     * Sets up when config gets created.
     * Retrieves old config.properties and always sets a few properties which change often. (ip etc)
     */
    private void setup() {
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

    /**
     * Put a property, overwrites the current property if it exists
     * Creates a new one if it doesn't exist yet.
     *
     * @param key   Key of the property you want to put
     * @param value Value of the property you want to put
     */
    public void putProperty(String key, String value) {
        properties.put(key, value);
        writeProperties();
        context.getSharedPreferences("CelixAgent", Context.MODE_PRIVATE).edit().putString("celixConfig", propertiesToString()).apply();
    }

    /**
     * Set properties from string.
     * Converts a string to a properties object and uses this one.
     *
     * @param propertyString Property string you want to use
     */
    public void setProperties(String propertyString) {
        properties = stringToProperties(propertyString);
        writeProperties();
        context.getSharedPreferences("CelixAgent", Context.MODE_PRIVATE).edit().putString("celixConfig", propertiesToString()).apply();
    }

    /**
     * Returns the property corresponding to the key
     * @param key       Key of the property you want to retrieve
     * @return Value of the property with the given key
     */
    public String getProperty(String key) {
        return properties.getProperty(key, "none");
    }

    public void removeProperty(String key) {
        properties.remove(key);
        writeProperties();
        context.getSharedPreferences("CelixAgent", Context.MODE_PRIVATE).edit().putString("celixConfig", propertiesToString()).apply();
    }

    /**
     * Writes properties to properties.config file.
     */
    private void writeProperties() {
        // Write properties
        try {
            FileOutputStream openFileOutput = context.openFileOutput(CONFIG_PROPERTIES, Context.MODE_PRIVATE);
            openFileOutput.write(propertiesToString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the ip address of this phone
     * @return Ip address ( like 192.168.0.50 )
     */
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

    /**
     * Makes a properties object from the given string
     * @param s     String you want to parse into a properties object
     * @return Properties object
     */
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

    /**
     * Translates the properties object to a String in format : key=value
     * @return
     */
    public String propertiesToString() {
        String propStr = "";
        for (String key : properties.stringPropertyNames()) {
            propStr += key + "=" + properties.get(key) + "\n";
        }
        return propStr;
    }

    /**
     * Checks what bundles are checked and should be autostarted.
     * Autostart is a configuration property : cosgi.auto.start.1=....
     * Adds checked bundles to the autostart property
     * @param autostart         All bundles
     * @param bundleLocation    Where are the bundles located
     */
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
