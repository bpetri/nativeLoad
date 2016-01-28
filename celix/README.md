# Celix Android Library project

This is the Android Library used for communication with the Celix framework.
This library acts as a wrapper around Apache Celix which is written in C.  
All the framework files are included in this library project.   
The Apache Celix repository : https://github.com/apache/celix  
Apache Celix website : https://celix.apache.org/  
Bintray location of library : https://bintray.com/marcojansen/apache-celix-android-wrapper/apache_celix_wrapper/view


This wrapper allows you to:

- Start/Stop framework
- Install bundles (By Path)
- Delete bundles  (By Id or Path)
- Start/Stop bundles   (By id or Path)
- Create a config.properties
- Get the output
- Notify when bundle changed, Celix changed(Start/Stop framework) and when there's new output

## How to use

Add this line to your build.gradle dependencies  
`compile 'apache.celix:celix-wrapper:2.0@aar'`

## Small example
MainActivity is implementing Observer
```
protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // Make a configuration object ( This creates a config.properties file )
      Config config = new Config(this);

      // Get a Celix instance
      Celix celix = Celix.getInstance();

      // Keep track all changes
      celix.addObserver(this);

      // Start the framework with the path of your config.properties
      celix.startFramework(config.getConfigPath());

      /* Now you can install bundles by location with celix.installBundle(location)
      It is also possible to start/stop/delete bundles after they're installed */

      // Stop framework
      celix.stopFramework();
}

@Override
public void update(Observable observable, Object data) {
    // Check if Celix started or stopped
    if (data == Update.CELIX_CHANGED) {
        // Celix started or stopped
        Boolean celixRunning = celix.isCelixRunning();
    }

    // Check if log changed
    if (data == Update.LOG_CHANGED) {
        // Log changed, get newest line
        String newLine = celix.getStdio();
    }

    // Check if a bundle changed or installed
    if (data instanceof OsgiBundle) {
        // Bundle changed or new bundle installed
        OsgiBundle bundle = (OsgiBundle) data;
    }
}

```
