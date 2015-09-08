package com.example.bjoern.nativeload;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;


public class MainActivity extends ActionBarActivity implements  Runnable {


    private final static String TAG = MainActivity.class.getName();

    private ArrayList<BundleItem> bundles = new ArrayList<>(20);
    private String bundleLocation;
    private LogCatReader lr;

    public ListView bundleListView;
    public BundleItemAdapter aa;

    private String CONFIG_PROPERTIES = "config.properties";
    private TextView textView_result;

    private String string0;
    private Handler handler;

    private static void setDefaultUncaughtExceptionHandler() {
        try {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    Log.e(TAG, "Uncaught Exception detected in thread {}" + t + " -- " + e);
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "Could not set the Default Uncaught Exception Handler", e);
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
            Log.e(TAG, "Error while retrieving IP Address: " +  e.toString(), e);
        }

        return null;
    }


    public Properties stringToProperties(String s) {
        final Properties p = new Properties();
        try {
            p.load(new StringReader(s));
        }
        catch (IOException e) {
            Log.e(TAG, "Error while converting string to properties: " +  e.toString(), e);
        }
        return p;
    }



    private String propertiesToString(Properties props) {

        String propStr = "";

        Enumeration<?> keys = props.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String val = props.getProperty(key);

            propStr += key + "=" + val + System.getProperty("line.separator");
        }

        return propStr;
    }


    private Properties generateConfiguration(Properties cfg) {


        if (cfg == null) {
            cfg = new Properties();
        }

        /* bundle configuration */
        String bundleCfg = cfg.getProperty("cosgi.auto.start.1","");

        for(BundleItem bundle : bundles) {

            String fullBundlePath = bundleLocation + "/" + bundle.getFilename();

            if (bundle.isChecked() && bundle.getStatus() == BundleItem.BUNDLE_LOCALLY_AVAILABLE && !bundleCfg.contains(fullBundlePath)) {
                bundleCfg += " " + fullBundlePath;
            }
            else if (!bundle.isChecked() && bundleCfg.contains(fullBundlePath)){
                bundleCfg = bundleCfg.replace(fullBundlePath, "");
            }

        }

        cfg.put("cosgi.auto.start.1", bundleCfg);

        /* cache directory */
        if (!cfg.containsKey("org.osgi.framework.storage")) {
            File cacheDir = getBaseContext().getDir("cache", Context.MODE_PRIVATE);
            cfg.put("org.osgi.framework.storage", cacheDir.getAbsolutePath());

            if(cacheDir.isDirectory()) {
                Log.d(TAG, "Cache dir " + cacheDir.toString() + " found.");
            }
            else if (!cacheDir.mkdir() ) {
                Log.e(TAG, "Creation of cache dir " + cacheDir.toString() + " failes.");
            }
        }

        // IPv4 only!
        String ipAdr = getLocalIpAddress();

        if (!cfg.containsKey("RSA_IP")) {
            cfg.put("RSA_IP", ipAdr);
        }

        if (!cfg.containsKey("RSA_PORT")) {
            cfg.put("RSA_PORT", "20888");
        }

        if (!cfg.containsKey("DISCOVERY_CFG_SERVER_IP")) {
            cfg.put("DISCOVERY_CFG_SERVER_IP", ipAdr);
        }

        if (!cfg.containsKey("DISCOVERY_CFG_SERVER_PORT")) {
            cfg.put("DISCOVERY_CFG_SERVER_PORT", "20999");
        }

        if (!cfg.containsKey("DISCOVERY_ETCD_SERVER_IP")) {
            // just a guess that for the etcd server
            String IpWithNoFinalPart  = ipAdr.replaceAll("(.*\\.)\\d+$", "$11");
            cfg.put("DISCOVERY_ETCD_SERVER_IP", IpWithNoFinalPart);
        }

        if (!cfg.containsKey("LOGHELPER_ENABLE_STDOUT_FALLBACK")) {
            cfg.put("LOGHELPER_ENABLE_STDOUT_FALLBACK", "true");
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
                    Log.e(TAG, "Error while writing configuration: " +  e.toString(), e);
                }
            }
        }

        return retVal;
    }


    private void setupInitScreen() {
        final EditText editText_log = (EditText) findViewById(R.id.editText_log);
        final Button btn_start = (Button) findViewById(R.id.button1);

        bundleListView.setVisibility(View.VISIBLE);
        editText_log.setVisibility(View.GONE);

        btn_start.setEnabled(true);
        btn_start.setText("START CELIX");




        btn_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Log.d("Start button", "Started");

                SharedPreferences prefs = getApplicationContext().getSharedPreferences("celixAgent", Context.MODE_PRIVATE);
                String cfgPath =  getApplicationContext().getFilesDir() + "/" + CONFIG_PROPERTIES;
                String cfgStr  = prefs.getString("celixConfig", null);
                Properties cfgProps =  (cfgStr != null) ?  generateConfiguration(stringToProperties(cfgStr))  : generateConfiguration(null);

                if (writeConfiguration(getApplicationContext(), propertiesToString(cfgProps))) {
                    btn_start.setEnabled(false);
                    bundleListView.setVisibility(View.GONE);
                    editText_log.setText("");
                    editText_log.setVisibility(View.VISIBLE);

                    lr.start();

                    startCelix(cfgPath);
                }
            }
        });

    }



    public void showNoConnectionDialog(final MainActivity txt) {
        handler.post(new Runnable() {
            @Override
            public void run() {

                AlertDialog.Builder builder = new AlertDialog.Builder(txt);
                builder.setCancelable(false);
                builder.setTitle("No Internet connection");
                builder.setMessage("There are no bundles locally available yet. You can download some pre-configured bundles by connection to the web.");

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        txt.downloadBundles();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }


    private void downloadBundles() {
        ArrayList<BundleItem> bundleToBeDownLoaded = new ArrayList<>();

        for(BundleItem bundle : bundles) {
            boolean fileExists =  new File(bundleLocation + "/" + bundle.getFilename()).isFile();

            if (!fileExists) {
                bundleToBeDownLoaded.add(bundle);
            }
            else {
                bundle.setStatus(BundleItem.BUNDLE_LOCALLY_AVAILABLE);
            }
        }

        if (!bundleToBeDownLoaded.isEmpty()) {
//            (new DownloaderThread(this, bundleToBeDownLoaded, bundleLocation)).start();
        }
    }

    /**
     * Method for moving the files from the assets to the internal storage
     * so the C code can reach it
     */
    private void moveBundles() {
        AssetManager assetManager = getResources().getAssets();

        String[] files = null;

        try {
            files = assetManager.list("celix_bundles"); //raw.celix_bundles
        } catch (Exception e) {
            Log.e("BundleMover", "ERROR: " + e.toString());
        }

//      Create dir /celix_bundle/ if not exists
//        File bundleDir = getDir("celix_bundles", MODE_PRIVATE);

        for (int i = 0; i < files.length; i++) {

            if (!new File(bundleLocation + files[i]).isFile()) {
                try {
                    InputStream in = assetManager.open("celix_bundles/" + files[i]);
//                    File newFile = new File(bundleLocation + "/"+ files[i]);
//                    OutputStream out = new FileOutputStream(newFile);
                    OutputStream out = openFileOutput(bundleLocation + "/" + files[i], MODE_PRIVATE);
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    in.close();
                    out.flush();
                    out.close();
                    Log.i("BundleMover", files[i] + " copied to " + bundleLocation);
                } catch (Exception e) {
                    Log.e("BundleMover", "ERROR: " + e.toString());
                }
            } else {
                Log.i("BundleMover", files[i] + " already exists");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);

        setDefaultUncaughtExceptionHandler();

//        getResources().openRawResource(R.raw.calculator);

        bundleListView = (ListView) findViewById(R.id.bundleListView);

        bundleLocation = getDir("celix_bundles", MODE_PRIVATE).getAbsolutePath();
//        bundleLocation = getExternalFilesDir(null).toString();

        Log.d("Bundlelocation", bundleLocation);
        Log.d("Bundlelocation", getApplicationContext().getFilesDir() + "      <- hier");

        moveBundles();
        for(String fileName : getExternalFilesDir(null).list()) {
            bundles.add(new BundleItem(fileName, "", false));
        }

//        bundles.add(new BundleItem("log_service.zip", "", true));
//        bundles.add(new BundleItem("log_writer.zip", "", true));
//        bundles.add(new BundleItem("echo_client.zip", "", false));
//        bundles.add(new BundleItem("echo_server.zip", "", false));
//        bundles.add(new BundleItem("topology_manager.zip", "", false));
//        bundles.add(new BundleItem("discovery_etcd.zip", "", false));
//        bundles.add(new BundleItem("remote_service_admin_http.zip", "", false));
//        bundles.add(new BundleItem("remote_shell.zip", "", false));
//        bundles.add(new BundleItem("calculator.zip", "", false));
//        bundles.add(new BundleItem("org.apache.celix.calc.api.Calculator_endpoint.zip", "", false));
//        bundles.add(new BundleItem("discovery_configured.zip", "", false));
//        bundles.add(new BundleItem("apache_celix_examples_hello_world.zip", "", false));

        aa = new BundleItemAdapter(this, R.layout.bundle_item, bundles);
        bundleListView.setAdapter(aa);

        handler = new Handler();

        lr = new LogCatReader(new LogCatOut()
        {
            @Override
            public void writeLogData(final String line) throws IOException
            {
                final EditText editText_log = (EditText) findViewById(R.id.editText_log);
                handler.post(new Runnable()
                {
                    public void run()
                    {
                        editText_log.append(line + System.getProperty("line.separator"));
                    }
                });
            }
        });



        // Load native library
        System.loadLibrary("jni_part");

        initJni();
        setupInitScreen();

        downloadBundles();

    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // TODO: Check this
        handler.removeCallbacks(this);
    }


    public void confirmCelixStart() {
        final Button btn_start = (Button) findViewById(R.id.button1);

        handler.post(new Runnable() {
                         @Override
                         public void run() {

                             btn_start.setText("STOP CELIX");
                             btn_start.setOnClickListener(new View.OnClickListener() {
                                 public void onClick(View v) {
                                         btn_start.setEnabled(false);
                                         Log.d("Start button", "Stopped");
                                         stopCelix();
                                 }
                             });

                             btn_start.setEnabled(true);
                         }
                     });
    }


    public void confirmCelixStop() {

        final Button btn_start = (Button) findViewById(R.id.button1);
        handler.post(new Runnable() {
            @Override
            public void run() {


                Toast.makeText(getBaseContext(), "Celix has stopped", Toast.LENGTH_SHORT).show();

                try {
                    Thread.sleep(5000);
                }
                catch (InterruptedException ignored)
                {
                    // I don't care
                }
                finally {
                    btn_start.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            lr.kill();
                            setupInitScreen();
                        }
                    });
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    protected void showInputDialog(final EditText edittext, String title, String msg, String text, DialogInterface.OnClickListener positiveListener) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        if (text != null) {
            edittext.setText(text);
        }

        alert.setTitle(title);
        alert.setMessage(msg);
        alert.setView(edittext);

        alert.setPositiveButton("OK", positiveListener);

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        final EditText edittext = new EditText(this.getApplicationContext());


        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        final SharedPreferences pref = getApplicationContext().getSharedPreferences("celixAgent", MODE_PRIVATE);
        String baseUrl = pref.getString("bundleUrl", null);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings_bundelUrl) {

            showInputDialog(edittext, "Change Bundle URL", "", baseUrl,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        pref.edit().putString("bundleUrl", edittext.getText().toString()).apply();
                        Toast.makeText(getBaseContext(), "url sucessfully changed", Toast.LENGTH_SHORT).show();
                    }
                }
            );

            return true;
        }
        else if (id == R.id.action_settings_editProperties) {
            final SharedPreferences prefs = getApplicationContext().getSharedPreferences("celixAgent", Context.MODE_PRIVATE);

            String cfgStr  = prefs.getString("celixConfig", null);
            Properties cfgProps =  (cfgStr != null) ?  generateConfiguration(stringToProperties(cfgStr))  : generateConfiguration(null);

            showInputDialog(edittext, "Edit Properties", "", propertiesToString(cfgProps) ,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            prefs.edit().putString("celixConfig", edittext.getText().toString()).apply();
                            Toast.makeText(getBaseContext(), "properties sucessfully changed", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    public native int startCelix(String propertyString);
    public native int stopCelix();
    public native int initJni();


    @Override
    public void run() {

    }
}
