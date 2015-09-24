/**
 * Licensed under Apache License v2. See LICENSE for more information.
 */

package com.inaetics.demonstrator.controller;

import android.content.Context;
import android.content.DialogInterface;
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
import com.inaetics.demonstrator.R;

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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            //Use convertview for recycling views, smoother scrolling.
            convertView = inflater.inflate(resource,parent,false);
            // View holder pattern, find all views once, smoother scrolling
            holder = new ViewHolder();
            holder.buttonsLayout = (LinearLayout) convertView.findViewById(R.id.buttons_layout);
            holder.bundleCheckbox = (CheckBox) convertView.findViewById(R.id.bundleCheckbox);
            holder.bundleFileName = (TextView) convertView.findViewById(R.id.bundleFileName);
            holder.installButton =  (Button) convertView.findViewById(R.id.install_btn);
            holder.runButton = (Button) convertView.findViewById(R.id.run_btn);
            holder.stopButton = (Button) convertView.findViewById(R.id.stop_btn);
            holder.stopButton.setEnabled(false);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.installButton.setText("Install");
            holder.runButton.setText("Run");
            holder.stopButton.setText("Stop");
            holder.installButton.setEnabled(true);
            holder.runButton.setEnabled(true);
            holder.stopButton.setEnabled(false);
        }


        if (model.getCelixStatus() == BundleStatus.CELIX_RUNNING) {
            holder.bundleCheckbox.setVisibility(View.GONE);
            holder.buttonsLayout.setVisibility(View.VISIBLE);
        } else {
            holder.bundleCheckbox.setVisibility(View.VISIBLE);
            holder.buttonsLayout.setVisibility(View.GONE);
        }

        final BundleItem item = getItem(position);

        String fileName = item.getFilename();
        BundleStatus status = item.getStatus();
        boolean isChecked = item.isChecked();
        holder.bundleCheckbox.setChecked(isChecked);
        holder.bundleFileName.setText(fileName);
        holder.bundleCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox box = (CheckBox) view;
                item.setChecked(box.isChecked());
            }
        });

        holder.installButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Button b = (Button)v;
                b.setEnabled(false);
                b.setText("Installing");
                model.getJniCommunicator().installBundle(model.getBundleLocation() + "/" + item.getFilename());
            }
        });

        holder.runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button)v;
                b.setEnabled(false);
                b.setText("Starting");
                model.getJniCommunicator().startBundle(model.getBundleLocation() + "/" + item.getFilename());
            }
        });

        holder.stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button)v;
                b.setEnabled(false);
                b.setText("Stopping");
                model.getJniCommunicator().stopBundle(model.getBundleLocation() + "/" + item.getFilename());
            }
        });

        switch (status) {
            case BUNDLE_RUNNING:
                holder.runButton.setEnabled(false);
                holder.runButton.setText("Running");
                holder.stopButton.setEnabled(true);
            case BUNDLE_INSTALLED:
                holder.installButton.setEnabled(false);
                holder.installButton.setText("Installed");
                break;
            default:
                holder.runButton.setEnabled(false);
                holder.runButton.setText("Not installed");
        }
        holder.bundleFileName.setText(fileName);
        return convertView;
    }

    static class ViewHolder {
        LinearLayout buttonsLayout;
        TextView bundleFileName;
        CheckBox bundleCheckbox;
        Button installButton;
        Button runButton;
        Button stopButton;
    }

}
