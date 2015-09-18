/**
 * Licensed under Apache License v2. See LICENSE for more information.
 */

package com.inaetics.demonstrator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import com.inaetics.demonstrator.fragments.BundlesFragment;
import com.inaetics.demonstrator.fragments.ConsoleFragment;
import com.inaetics.demonstrator.model.BundleItem;
import com.inaetics.demonstrator.model.BundleStatus;
import com.inaetics.demonstrator.model.Config;
import com.inaetics.demonstrator.model.Model;
import com.inaetics.demonstrator.nativeload.R;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.util.Observable;
import java.util.Observer;
import java.util.Properties;


public class MainActivity extends AppCompatActivity{


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
        super.onCreate(null);
        super.setContentView(R.layout.pager_tab);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            BundlesFragment left = new BundlesFragment();
            ConsoleFragment right = new ConsoleFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.left_container,left).commit();
            getSupportFragmentManager().beginTransaction().add(R.id.right_container,right).commit();
        } else {
            ViewPager pager = (ViewPager) findViewById(R.id.pager);
            PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
            MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
            pager.setAdapter(pagerAdapter);
            tabs.setViewPager(pager);
        }



        model = Model.getInstance();
        config = model.getConfig();
        model.setBundleLocation(getExternalFilesDir(null).toString());
        model.moveBundles(getResources().getAssets());
        for(String fileName : getExternalFilesDir(null).list()) {
            BundleItem b = model.addBundle(fileName);
            if (b!=null) {
                b.setStatus(BundleStatus.BUNDLE_LOCALLY_AVAILABLE);
            }
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
}
