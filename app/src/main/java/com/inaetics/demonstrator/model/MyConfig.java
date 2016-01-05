package com.inaetics.demonstrator.model;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import apache.celix.model.Config;

/**
 * Created by marcojansen on 5-1-16.
 */
public class MyConfig extends Config {
    private Context context;


    public MyConfig(Context context) {
        super(context);
        this.context = context;
        setupSettings();
    }

    /**
     * Adds all the standard properties that will be used by some of the Celix and INAETICS bundles
     */
    private void setupSettings() {
        putProperty("RSA_PORT", "20888");
        putProperty("DISCOVERY_CFG_SERVER_PORT", "20999");
        putProperty("org.osgi.framework.storage.clean", "onFirstInit");
        putProperty("LOGHELPER_ENABLE_STDOUT_FALLBACK", "true");
        //Following variables will be set every startup
        String ipAdr = getLocalIpAddress();
        if (ipAdr != null) {
            String[] ip = ipAdr.split("\\.");
            putProperty("RSA_IP", ipAdr);
            putProperty("DISCOVERY_CFG_SERVER_IP", ipAdr);
            putProperty("deployment_admin_identification", "Android" + ip[ip.length - 1]);
        } else {
            Toast.makeText(context, "No ip address found! Are you connected?", Toast.LENGTH_LONG).show();
            putProperty("deployment_admin_identification", "Android" + (int) (Math.random() * 745 + 255));
        }
        putProperty("deployment_cache_dir", context.getCacheDir().getAbsolutePath());
        putProperty("org.osgi.framework.storage", context.getDir("cache", Context.MODE_PRIVATE).getAbsolutePath());
    }


    /**
     * Retrieves the IPv4 address of the phone
     * @return Ip address ( like 192.168.0.50 )
     */
    public String getLocalIpAddress() {

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Config", "Error while retrieving IP Address: " + e.toString(), e);
        }

        return null;
    }


}
