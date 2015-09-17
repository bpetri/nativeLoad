/**
 * Licensed under Apache License v2. See LICENSE for more information.
 */

package com.inaetics.demonstrator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.inaetics.demonstrator.controller.BundleItemAdapter;
import com.inaetics.demonstrator.controller.MyPagerAdapter;
import com.inaetics.demonstrator.model.BundleStatus;
import com.inaetics.demonstrator.model.Config;
import com.inaetics.demonstrator.model.Model;
import com.inaetics.demonstrator.nativeload.R;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.util.Observable;
import java.util.Observer;
import java.util.Properties;


public class MainActivity extends AppCompatActivity implements Observer{


    private final static String TAG = MainActivity.class.getName();
    public BundleItemAdapter bundleAdapter;
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
        super.setContentView(R.layout.pager_tab);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
//        tabs.setShouldExpand(true);
        tabs.setViewPager(pager);


        model = Model.getInstance();
        model.addObserver(this);
        config = model.getConfig();
        model.setBundleLocation(getExternalFilesDir(null).toString());
        model.moveBundles(getResources().getAssets());
        for(String fileName : getExternalFilesDir(null).list()) {
            model.addBundle(fileName).setStatus(BundleStatus.BUNDLE_LOCALLY_AVAILABLE);
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
            case R.id.action_startQR:
                new IntentIntegrator(this)
                        .setCaptureActivity(ScanActivity.class)
                        .setOrientationLocked(false)
                        .initiateScan();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        }
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
                    cfgProps = config.generateConfiguration(config.stringToProperties(cfgStr),model.getBundles(),model.getBundleLocation(),getBaseContext());
                else
                    cfgProps = config.generateConfiguration(null,model.getBundles(),model.getBundleLocation(),getBaseContext());

                if (config.writeConfiguration(getApplicationContext(), config.propertiesToString(cfgProps))) {
                    btn_start.setEnabled(false);
                    bundleListView.setVisibility(View.GONE);
                    editText_log.setText("");
                    editText_log.setVisibility(View.VISIBLE);
                    lr.start();
                    model.getJniCommunicator().startCelix(cfgPath);
                }
            }
        });

    }


    //    public void confirmCelixStart() {
//        final Button btn_start = (Button) findViewById(R.id.button1);
//
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//
//                btn_start.setText("STOP CELIX");
//                btn_start.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
//                btn_start.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        btn_start.setEnabled(false);
////                        Log.d("Start button", "Stopped");
//                        installBundle(model.getBundleLocation() + "/echo_client.zip");
//                        installBundle(model.getBundleLocation() + "/echo_server.zip");
////                                         stopCe1lix();
//                    }
//                });
//
//                btn_start.setEnabled(true);
//            }
//        });
//    }
//
//
//    public void confirmCelixStop() {
//
//        final Button btn_start = (Button) findViewById(R.id.button1);
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//
//
//                Toast.makeText(getBaseContext(), "Celix has stopped", Toast.LENGTH_SHORT).show();
//
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException ignored) {
//                    // I don't care
//                } finally {
//                    btn_start.setOnClickListener(new View.OnClickListener() {
//                        public void onClick(View v) {
//                            lr.kill();
//                            setupInitScreen();
//                        }
//                    });
//                }
//
//            }
//        });
//    }
//    public void confirmBundleStart(String location) {
//
//        String[] words = location.split("/");
//        String fileName = words[words.length - 1];
//        for (BundleItem b : model.getBundles()) {
//            if (fileName.equals(b.getFilename())) {
//                b.setStatus(BundleStatus.BUNDLE_INSTALLED);
//                Log.e("installed bundle JAVA", fileName);
//            }
//        }
//    }



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

    @Override
    public void update(Observable observable, Object data) {
        if(data != null && data instanceof BundleStatus) {
            switch((BundleStatus)data) {
                case CELIX_RUNNING:
                    Log.d("MainActivity", "Celix running");
                    break;
                case CELIX_STOPPED:
                    Log.d("MainActivity", "Celix stopped");
                    break;
                default: return;
            }
        }
    }
}
