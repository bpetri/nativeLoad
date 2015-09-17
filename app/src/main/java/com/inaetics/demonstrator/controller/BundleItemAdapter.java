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
import android.widget.Button;
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

        switch (status) {
            case BUNDLE_INSTALLED:
                Button inst = (Button) convertView.findViewById(R.id.button);
                inst.setText("Installed");
                inst.setEnabled(false);
                break;
            default:
                break;

        }
        bundleFileName.setText(fileName);



        return convertView;
    }

}
