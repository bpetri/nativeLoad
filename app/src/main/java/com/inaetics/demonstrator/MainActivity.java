/**
 * Licensed under Apache License v2. See LICENSE for more information.
 */

package com.inaetics.demonstrator;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.inaetics.demonstrator.controller.DownloadTask;
import com.inaetics.demonstrator.controller.MyPagerAdapter;
import com.inaetics.demonstrator.model.BundleStatus;
import com.inaetics.demonstrator.model.Model;
import com.inaetics.demonstrator.model.MyConfig;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;


import apache.celix.Celix;
import apache.celix.model.CelixUpdate;
import apache.celix.model.Config;
public class MainActivity extends AppCompatActivity implements Observer {

    private Model model;
    private MyConfig config;
    private Button btn_start;
    private ViewPager pager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.pager_tab);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
        //Initiate celix
        Celix celix = Celix.getInstance();
        celix.addObserver(this);

        pager = (ViewPager) findViewById(R.id.pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(pager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        pager.setOffscreenPageLimit(2);

        model = Model.getInstance();
        model.setContext(this);

        config = model.getConfig();

        // Only one time!! After configuration change don't do it again.
        if (!model.areBundlesMoved()) {
            File dirLocation = getExternalFilesDir(null);
            if (dirLocation == null) {
                dirLocation = getCacheDir();
            }
            model.setBundleLocation(dirLocation.getAbsolutePath());
            model.moveBundles(getResources().getAssets());
        }
        btn_start = (Button) findViewById(R.id.start_btn);

        if (model.getCelixStatus() == BundleStatus.CELIX_RUNNING) {
            setRunning();
        } else {
            setStopped();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mConnReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings_editProperties:
                final EditText edittext = new EditText(this.getApplicationContext());
                String props = config.propertiesToString();
                showInputDialog(edittext, "Edit Properties", "", props, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        config.setProperties(edittext.getText().toString());
                        Toast.makeText(getBaseContext(), "properties changed", Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            case R.id.action_startQR:
                new IntentIntegrator(this)
                        .setCaptureActivity(ScanActivity.class)
                        .setOrientationLocked(false)
                        .initiateScan();
                return true;
            case R.id.action_download:
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
                final EditText text = new EditText(this);
                builder.setView(text);
                builder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        download(text.getText().toString());
                    }
                });

                builder.setNegativeButton("Cancel", null);
                builder.setTitle("Download bundle from URL");
                AlertDialog realDialog = builder.create();
                realDialog.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
                realDialog.show();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void download(String url) {
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Downloading");
        progress.setMessage("Wait while downloading");
        progress.show();

        DownloadTask task = new DownloadTask(this, progress);
        task.execute(url);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                String content = result.getContents();
                try {
                    URL url = new URL(content);
                    // Is a url!
                    download(content);
                } catch (MalformedURLException e) {
                    //Not an url
                    Scanner sc = new Scanner(content);
                    boolean autostart = false;
                    while (sc.hasNextLine()) {
                        String[] keyValue = sc.nextLine().split("=");
                        if (keyValue[0].equals("cosgi.auto.start.1")) {
                            autostart = true;
                            String startBundles = "";
                            Scanner bscan = new Scanner(keyValue[1]);
                            while (bscan.hasNext()) {
                                startBundles += model.getBundleLocation() + "/" + bscan.next() + " ";
                            }
                            bscan.close();
                            config.putProperty(keyValue[0], startBundles);
                        } else {
                            try {
                                config.putProperty(keyValue[0], keyValue[1]);
                            } catch (ArrayIndexOutOfBoundsException ex) {
                                //Ignore property there is no key/value combination
                                Log.e("Scanner", "couldn't scan: " + Arrays.toString(keyValue));
                            }
                        }
                    }
                    sc.close();
                    if (autostart && model.getCelixStatus() != BundleStatus.CELIX_RUNNING) {
                        Celix.getInstance().startFramework(config.getConfigPath());
                    }
                    Toast.makeText(this, "Scanned QR", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    /**
     * Dialog used to show the settings
     *
     * @param edittext         Edittext which contains all the settings
     * @param title            Title of the dialog
     * @param msg              Message of the dialog
     * @param text             Text inside the edittext
     * @param positiveListener Onclicklistener for the change button
     */
    private void showInputDialog(final EditText edittext, String title, String msg, String text, DialogInterface.OnClickListener positiveListener) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.DialogTheme);

        if (text != null) {
            edittext.setText(text);
            edittext.setTextColor(ContextCompat.getColor(this,android.R.color.black));
            edittext.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        }

        alert.setTitle(title);
        alert.setMessage(msg);
        alert.setView(edittext);

        alert.setPositiveButton("Save", positiveListener);

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        AlertDialog realDialog = alert.create();
        realDialog.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
        realDialog.show();
    }

    /**
     * Method triggered when celix is running.
     * Changes button, onclicklistener to a stop button.
     */
    private void setRunning() {
        btn_start.setText("STOP");
        btn_start.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Celix.getInstance().stopFramework();
                btn_start.setEnabled(false);
            }
        });
        btn_start.setEnabled(true);
    }

    /**
     * Method triggered when celix is stopped (Not running)
     * Changes button to a start button and the onclicklistener.
     */
    private void setStopped() {
        btn_start.setText("Start");
        btn_start.setBackgroundColor(ContextCompat.getColor(this, R.color.celix_blue));

        btn_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String str = "";
                config.putProperty("cosgi.auto.start.1", str);
                btn_start.setEnabled(false);
                Celix.getInstance().startFramework(config.getConfigPath());
            }

        });
        btn_start.setEnabled(true);
    }

    // Used to determine if the ip has changed.
    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("Network", "network changes detected");
            String ip = config.getLocalIpAddress();
            if (ip != null) {
                if (!config.getProperty("RSA_IP").equals(ip)) {
                    Log.v("RSA_IP", "Putting new IP" + ip);
                    config.putProperty("RSA_IP", ip);
                }
                if (!config.getProperty("DISCOVERY_CFG_SERVER_IP").equals(ip)) {
                    Log.v("DISCOVERY_CFG_SERVER_IP", "Putting new IP" + ip);
                    config.putProperty("DISCOVERY_CFG_SERVER_IP", ip);
                }
            }
        }
    };

    @Override
    public void update(Observable observable, Object data) {
        if(data == CelixUpdate.CELIX_CHANGED) {
            if(Celix.getInstance().isCelixRunning()) {
                setRunning();
            } else {
                setStopped();
            }
        }
    }
}
