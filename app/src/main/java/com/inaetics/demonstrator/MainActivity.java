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
import com.inaetics.demonstrator.model.Model;
import com.inaetics.demonstrator.model.MyConfig;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;


import apache.celix.Celix;
import apache.celix.model.Update;

public class MainActivity extends AppCompatActivity implements Observer {

    private Model model;
    private MyConfig config;
    private Button btn_start;
    private static final int USE_CAMERA_PERMISSION = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.pager_tab);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
        //Initiate celix
        Celix celix = Celix.getInstance();
        celix.addObserver(this);

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
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

        if (celix.isCelixRunning()) {
            setRunning();
        } else {
            setStopped();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop observing when destroyed
        Celix.getInstance().deleteObserver(this);
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
                showInputDialog(edittext, "Edit Properties", props, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        config.setProperties(edittext.getText().toString());
                        Toast.makeText(getBaseContext(), "properties changed", Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            case R.id.action_startQR:
                int hasCameraPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
                if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA},USE_CAMERA_PERMISSION);
                } else {
                    new IntentIntegrator(this)
                            .setCaptureActivity(CaptureActivity.class)
                            .setOrientationLocked(false)
                            .initiateScan();
                }
                return true;
            case R.id.action_download:
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
                final EditText text = new EditText(this.getApplicationContext());
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

    /**
     * On Android 6+ (api 23+) we have to ask for permissions
     * @param requestCode       code send with permission request
     * @param permissions       list with permissions
     * @param grantResults      results that have been granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case USE_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new IntentIntegrator(this)
                            .setCaptureActivity(CaptureActivity.class)
                            .setOrientationLocked(false)
                            .initiateScan();
                } else {
                    Toast.makeText(this, "No camera permission for QR-code scanner!",Toast.LENGTH_LONG).show();;
                }

        }
    }

    /**
     * Method to start a progress dialog and a download task.
     * @param url   Url where to download from
     */
    private void download(String url) {
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Downloading");
        progress.setMessage("Wait while downloading");
        progress.show();

        DownloadTask task = new DownloadTask(this, progress);
        task.execute(url);
    }

    /**
     * Method called when QR-code scanner has been triggered and finished.
     * If a qr-code is scanned it will process the content.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                String content = result.getContents();
                try {
                    new URL(content);
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
                    if (autostart && !Celix.getInstance().isCelixRunning()) {
                        Celix.getInstance().startFramework(config.getConfigPath());
                    }
                    Toast.makeText(this, "Scanned QR", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    /**
     * Dialog used to show the settings
     * @param edittext         Edittext which contains all the settings
     * @param title            Title of the dialog
     * @param text             Text inside the edittext
     * @param positiveListener Onclicklistener for the change button
     */
    private void showInputDialog(final EditText edittext, String title, String text, DialogInterface.OnClickListener positiveListener) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.DialogTheme);

        if (text != null) {
            edittext.setText(text);
            edittext.setTextColor(ContextCompat.getColor(this,android.R.color.black));
            edittext.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        }

        alert.setTitle(title);
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
        btn_start.setText("Stop");
        btn_start.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Celix.getInstance().stopFramework();
                btn_start.setEnabled(false);
                btn_start.setText("Stopping");
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
        btn_start.setBackgroundColor(ContextCompat.getColor(this, R.color.android_green));

        btn_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String str = "";
                config.putProperty("cosgi.auto.start.1", str);
                btn_start.setEnabled(false);
                btn_start.setText("Starting");
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

    /**
     * Observes a Celix instance. Updates UI when celix has been started/stopped (Changed)
     * @param observable        Celix instance
     * @param data              Information about what has changed
     */
    @Override
    public void update(Observable observable, Object data) {
        if(data == Update.CELIX_CHANGED) {
            if(Celix.getInstance().isCelixRunning()) {
                setRunning();
            } else {
                setStopped();
            }
        }
    }
}
