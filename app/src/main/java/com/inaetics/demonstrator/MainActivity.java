/**
 * Licensed under Apache License v2. See LICENSE for more information.
 */

package com.inaetics.demonstrator;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.inaetics.demonstrator.controller.MyPagerAdapter;
import com.inaetics.demonstrator.fragments.BundlesFragment;
import com.inaetics.demonstrator.fragments.ConsoleFragment;
import com.inaetics.demonstrator.model.BundleItem;
import com.inaetics.demonstrator.model.BundleStatus;
import com.inaetics.demonstrator.model.Config;
import com.inaetics.demonstrator.model.Model;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;


public class MainActivity extends AppCompatActivity implements Observer {

    private Model model;
    private Config config;
    private Button btn_start;
    private ViewPager pager;

    static {
        System.loadLibrary("celix_utils");
        System.loadLibrary("celix_framework");
        System.loadLibrary("jni_part");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        super.setContentView(R.layout.pager_tab);
        // Receiver used to check network status ( On network status change the ip should be refreshed)
        registerReceiver(mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        //Check if in landscape or portrait
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            BundlesFragment left = new BundlesFragment();
            ConsoleFragment right = new ConsoleFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.left_container,left).commit();
            getSupportFragmentManager().beginTransaction().add(R.id.right_container,right).commit();
        } else {
            pager = (ViewPager) findViewById(R.id.pager);
            PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
            MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
            pager.setAdapter(pagerAdapter);
            tabs.setViewPager(pager);
        }

        model = Model.getInstance();
        model.setContext(this);
        model.addObserver(this);
        config = model.getConfig();
        // Only one time!! After configuration change don't do it again.
        if (model.getBundles().isEmpty()) {
            model.setBundleLocation(getExternalFilesDir(null).toString());
            model.moveBundles(getResources().getAssets());
            for (String fileName : getExternalFilesDir(null).list()) {
                BundleItem b;
                switch (fileName) {
                    case "discovery_etcd.zip":
                    case "remote_service_admin_http.zip":
                    case "topology_manager.zip":
                        b = model.addBundle(fileName, true);
                        break;
                    default:
                        b = model.addBundle(fileName, false);
                        break;
                }
                if (b != null) {
                    b.setStatus(BundleStatus.BUNDLE_LOCALLY_AVAILABLE);
                }
            }
        }
        btn_start = (Button) findViewById(R.id.start_btn);
        if (model.getCelixStatus() == BundleStatus.CELIX_RUNNING) {
            setRunning();
        } else {
            setStopped();
        }
        model.initJNI();

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

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                String content = result.getContents();
                Scanner sc = new Scanner(content);
                boolean autostart = false;
                Log.e("Content", content);
                while (sc.hasNextLine()) {
                    String[] keyValue = sc.nextLine().split("=");
                    Log.e("content", Arrays.toString(keyValue));
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
                        } catch (ArrayIndexOutOfBoundsException e) {
                            //Ignore property there is no key/value combination
                            Log.e("Scanner", "couldn't scan: " + Arrays.toString(keyValue));
                        }
                    }
                }
                sc.close();
                if (autostart && model.getCelixStatus() != BundleStatus.CELIX_RUNNING) {
                    String cfgPath = getApplicationContext().getFilesDir() + "/" + Config.CONFIG_PROPERTIES;
                    model.getJniCommunicator().startCelix(cfgPath);
                }
                Toast.makeText(this, "Scanned QR", Toast.LENGTH_SHORT).show();
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

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        if (text != null) {
            edittext.setText(text);
            edittext.setTextColor(getResources().getColor(android.R.color.black));
            edittext.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
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

    /**
     * Method triggered when celix is running.
     * Changes button, onclicklistener to a stop button.
     */
    private void setRunning() {
        btn_start.setText("STOP");
        btn_start.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                model.getJniCommunicator().stopCelix();
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
        btn_start.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        btn_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String cfgPath = getApplicationContext().getFilesDir() + "/" + Config.CONFIG_PROPERTIES;
                config.setAutostart(model.getBundles(), model.getBundleLocation());
                btn_start.setEnabled(false);
                model.getJniCommunicator().startCelix(cfgPath);
                if (pager != null) {
                    pager.setCurrentItem(1);
                }
            }

        });
        btn_start.setEnabled(true);
    }

    // Used to determine if the ip has changed.
    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("Network", "network changes detected");
            String ip = config.getLocalIpAddress();
            if (ip != null) {
                if (!config.getProperty("RSA_IP").equals(ip)) {
                    Log.e("RSA_IP", "Putting new IP" + ip);
                    config.putProperty("RSA_IP", ip);
                }
                if (!config.getProperty("DISCOVERY_CFG_SERVER_IP").equals(ip)) {
                    Log.e("DISCOVERY_CFG_SERVER_IP", "Putting new IP" + ip);
                    config.putProperty("DISCOVERY_CFG_SERVER_IP", ip);
                }
            }
        }
    };


    /**
     * Observes the model and checks if there's a update from celix (that it is running or stopped)
     */
    @Override
    public void update(Observable observable, Object o) {
        if (o == BundleStatus.CELIX_RUNNING) {
            setRunning();
        } else if (o == BundleStatus.CELIX_STOPPED) {
            setStopped();
        }
    }
}
