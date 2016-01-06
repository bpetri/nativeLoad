package apache.celix.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
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
    private String configPath;

    public Config(Context context) {
        this.context = context;
        configPath = context.getFilesDir() + "/" + CONFIG_PROPERTIES;
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
        }

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
     *
     * @param key Key of the property you want to retrieve
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
     * Makes a properties object from the given string
     *
     * @param s String you want to parse into a properties object
     * @return Properties object
     */
    private Properties stringToProperties(String s) {
        final Properties p = new Properties();
        try {
            p.load(new StringReader(s));
        } catch (IOException e) {
            Log.e("Config", "Error while converting string to properties: " + e.toString(), e);
        }
        return p;
    }

    /**
     * Translates the properties object to a String in format : key=value
     *
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
     */
    public void setAutostart(ArrayList<String> locations) {
        String autostart = "";
        for (String loc : locations) {
            autostart += loc + " ";
        }
        autostart.trim();
        putProperty("cosgi.auto.start.1", autostart);
    }

    public String getConfigPath() {
        return configPath;
    }


}
