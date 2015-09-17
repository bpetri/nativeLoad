/**
 * Licensed under Apache License v2. See LICENSE for more information.
 */

package com.inaetics.demonstrator.controller;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.inaetics.demonstrator.model.BundleItem;
import com.inaetics.demonstrator.model.BundleStatus;
import com.inaetics.demonstrator.model.Model;
import com.inaetics.demonstrator.nativeload.R;

import java.util.List;

/**
 * Created by bjoern on 29.05.15.
 */
public class BundleItemAdapter extends ArrayAdapter<BundleItem> {

    private final String TAG = BundleItemAdapter.class.getName();
    private LayoutInflater inflater;
    private Model model;

    int resource;

    public BundleItemAdapter(Context _context, int _resource, List<BundleItem> _items, Model model) {
        super(_context, _resource, _items);
        this.model = model;
        inflater = LayoutInflater.from(_context);
        resource = _resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(resource,parent,false);
        }
        LinearLayout buttonsLayout = (LinearLayout) convertView.findViewById(R.id.buttons_layout);
        TextView bundleFileName = (TextView) convertView.findViewById(R.id.bundleFileName);
        CheckBox bundleCheckbox = (CheckBox) convertView.findViewById(R.id.bundleCheckbox);
        ProgressBar bundleProgressBar = (ProgressBar) convertView.findViewById(R.id.bundleProgressBar);
        if (model.getCelixStatus() == BundleStatus.CELIX_RUNNING) {
            bundleCheckbox.setVisibility(View.GONE);
            buttonsLayout.setVisibility(View.VISIBLE);
        } else {
            bundleCheckbox.setVisibility(View.VISIBLE);
            buttonsLayout.setVisibility(View.GONE);
        }

        final BundleItem item = getItem(position);
        String fileName = item.getFilename();
        BundleStatus status = item.getStatus();
        boolean isChecked = item.isChecked();



        bundleFileName.setText(fileName);


        switch (status) {

            case BUNDLE_NOT_YET_INITIALIZED:
                bundleProgressBar.setVisibility(View.GONE);
                bundleCheckbox.setVisibility(View.VISIBLE);
                bundleCheckbox.setEnabled(false);
                bundleCheckbox.setChecked(false);
                break;
            case BUNDLE_LOCALLY_AVAILABLE:

                bundleProgressBar.setVisibility(View.GONE);

                bundleCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        item.setChecked(isChecked);
                    }
                });
//                bundleCheckbox.setVisibility(View.VISIBLE);
                bundleCheckbox.setEnabled(true);
                bundleCheckbox.setChecked(isChecked);
                break;
            case BUNDLE_CONNECTING_STARTED:
                bundleProgressBar.setVisibility(View.VISIBLE);
                bundleCheckbox.setVisibility(View.VISIBLE);
                bundleCheckbox.setEnabled(false);
                bundleCheckbox.setChecked(false);


                break;
            case BUNDLE_DOWNLOAD_STARTED:
                Log.e("BundleItem", "DOWN_START:  " + fileName);

                bundleCheckbox.setEnabled(false);
                bundleCheckbox.setVisibility(View.GONE);

                Integer fileSizeInKB = (Integer) item.getStatusInfo();
                bundleProgressBar.setProgress(0);
                bundleProgressBar.setMax(fileSizeInKB);
                break;
            case BUNDLE_UPDATE_PROGRESS_BAR:
                Log.e("BundleItem", "UPD_PROGRESS:  " + fileName);

                bundleCheckbox.setEnabled(false);
                bundleCheckbox.setVisibility(View.GONE);
                Integer totalReadInKB = (Integer) item.getStatusInfo();
                bundleProgressBar.setProgress(totalReadInKB);
                break;
            case BUNDLE_ENCOUNTERED_ERROR:
                Log.e("BundleItem", "ENC_ERR:  " + fileName);

                bundleProgressBar.setVisibility(View.GONE);
                bundleCheckbox.setVisibility(View.VISIBLE);
                bundleCheckbox.setEnabled(false);
                bundleCheckbox.setChecked(false);

                String message = (String) item.getStatusInfo();
                Toast.makeText(getContext(), item.getFilename() + " " + message, Toast.LENGTH_SHORT).show();
                break;
            default:
                Log.e("Status", "UNKNOWN " + fileName + " " + status);
        }


        return convertView;
    }

}
