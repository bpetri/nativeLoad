/**
 * Licensed under Apache License v2. See LICENSE for more information.
 */

package com.inaetics.demonstrator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.inaetics.demonstrator.controller.BundleItemAdapter;
import com.inaetics.demonstrator.logging.LogCatOut;
import com.inaetics.demonstrator.logging.LogCatReader;
import com.inaetics.demonstrator.model.BundleItem;
import com.inaetics.demonstrator.model.BundleStatus;
import com.inaetics.demonstrator.model.Config;
import com.inaetics.demonstrator.model.Model;
import com.inaetics.demonstrator.nativeload.R;

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


public class MainActivity extends AppCompatActivity implements  Runnable {


    private final static String TAG = MainActivity.class.getName();
    private LogCatReader lr;

    public ListView bundleListView;
    public BundleItemAdapter bundleAdapter;
    private Handler handler;
    private Model model;
    private Config config;

    static {
        System.loadLibrary("celix_utils");
        System.loadLibrary("celix_framework");
        System.loadLibrary("jni_part");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);

        bundleListView = (ListView) findViewById(R.id.bundleListView);
        model = Model.getInstance();
        config = model.getConfig();
        model.setBundleLocation(getExternalFilesDir(null).toString());
        model.moveBundles(getResources().getAssets());
        for(String fileName : getExternalFilesDir(null).list()) {
            model.addBundle(fileName).setStatus(BundleStatus.BUNDLE_LOCALLY_AVAILABLE);
        }
        bundleAdapter = new BundleItemAdapter(this, R.layout.bundle_item, model.getBundles());
        bundleListView.setAdapter(bundleAdapter);

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
        initJni();
        setupInitScreen();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        final EditText edittext = new EditText(this.getApplicationContext());
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("celixAgent", MODE_PRIVATE);
        switch (item.getItemId()) {
            case R.id.action_settings_bundelUrl:
                String baseUrl = pref.getString("bundleUrl", null);
                showInputDialog(edittext, "Change Bundle URL", "", baseUrl,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                pref.edit().putString("bundleUrl", edittext.getText().toString()).apply();
                                Toast.makeText(getBaseContext(), "url sucessfully changed", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                return true;
            case R.id.action_settings_editProperties:
                String cfgStr  = pref.getString("celixConfig", null);
                Properties cfgProps = null;
                if (cfgStr != null)
                    cfgProps = config.generateConfiguration(config.stringToProperties(cfgStr),model.getBundles(),model.getBundleLocation(),getBaseContext());
                else
                    cfgProps = config.generateConfiguration(null, model.getBundles(), model.getBundleLocation(), getBaseContext());

                showInputDialog(edittext, "Edit Properties", "", config.propertiesToString(cfgProps),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                pref.edit().putString("celixConfig", edittext.getText().toString()).apply();
                                Toast.makeText(getBaseContext(), "properties sucessfully changed", Toast.LENGTH_SHORT).show();
                            }
                        }
                );

                return true;
        }
        return super.onOptionsItemSelected(item);
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
                String cfgPath = getApplicationContext().getFilesDir() + "/" + Config.CONFIG_PROPERTIES;
                String cfgStr = prefs.getString("celixConfig", null);
                Properties cfgProps = null;
                if (cfgStr != null)
                    cfgProps = config.generateConfiguration(config.stringToProperties(cfgStr), model.getBundles(), model.getBundleLocation(), getBaseContext());
                else
                    cfgProps = config.generateConfiguration(null, model.getBundles(), model.getBundleLocation(), getBaseContext());

                if (config.writeConfiguration(getApplicationContext(), config.propertiesToString(cfgProps))) {
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





    public void confirmCelixStart() {
        final Button btn_start = (Button) findViewById(R.id.button1);

        handler.post(new Runnable() {
            @Override
            public void run() {

                btn_start.setText("STOP CELIX");
                btn_start.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                btn_start.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        btn_start.setEnabled(false);
//                        Log.d("Start button", "Stopped");
                        installBundle(model.getBundleLocation() + "/echo_client.zip");
                        installBundle(model.getBundleLocation() + "/echo_server.zip");
//                                         stopCelix();
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
                } catch (InterruptedException ignored) {
                    // I don't care
                } finally {
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
    public void confirmBundleStart(String location) {

        String[] words = location.split("/");
        String fileName = words[words.length - 1];
        for (BundleItem b : model.getBundles()) {
            if (fileName.equals(b.getFilename())) {
                b.setStatus(BundleStatus.BUNDLE_INSTALLED);
                Log.e("installed bundle JAVA", fileName);
            }
        }
    }



    protected void showInputDialog(final EditText edittext, String title, String msg, String text, DialogInterface.OnClickListener positiveListener) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        if (text != null) {
            edittext.setText(text);
            edittext.setTextColor(getResources().getColor(android.R.color.black));
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



    public native int startCelix(String propertyString);
    public native int stopCelix();
    public native int initJni();
    public native int installBundle(String path);

    @Override
    public void run() {

    }
}
