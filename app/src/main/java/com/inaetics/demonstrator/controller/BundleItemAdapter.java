/**
 * Licensed under Apache License v2. See LICENSE for more information.
 */

package com.inaetics.demonstrator.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inaetics.demonstrator.R;
import com.inaetics.demonstrator.model.BundleItem;
import com.inaetics.demonstrator.model.BundleStatus;
import com.inaetics.demonstrator.model.Model;

import java.util.List;

import apache.celix.Celix;

/**
 * Created by bjoern on 29.05.15.
 */
public class BundleItemAdapter extends ArrayAdapter<BundleItem> {

    private LayoutInflater inflater;
    private Model model;
    private int resource;

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
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.installButton.setText("Install");
            holder.runButton.setText("Run");
            holder.installButton.setEnabled(true);
            holder.runButton.setEnabled(true);
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
                if(item.getStatus() == BundleStatus.BUNDLE_LOCALLY_AVAILABLE) {
                    b.setText("Installing");
                    item.setStatus(BundleStatus.BUNDLE_INSTALLING);
                    Celix.getInstance().installBundle(model.getBundleLocation() + "/" + item.getFilename());
//                    model.getJniCommunicator().installBundle(model.getBundleLocation() + "/" + item.getFilename());
                } else if (item.getStatus() == BundleStatus.BUNDLE_INSTALLED) {
                    b.setText("Deleting");
                    item.setStatus(BundleStatus.BUNDLE_DELETING);
                    Celix.getInstance().deleteBundle(model.getBundleLocation() + "/" + item.getFilename());
//                    model.getJniCommunicator().deleteBundle(model.getBundleLocation() + "/" + item.getFilename());
                }
            }
        });

        holder.runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button)v;
                b.setEnabled(false);
                if(item.getStatus() == BundleStatus.BUNDLE_INSTALLED) {
                    b.setText("Starting");
                    item.setStatus(BundleStatus.BUNDLE_STARTING);
                    Celix.getInstance().startBundle(model.getBundleLocation() + "/" + item.getFilename());
//                    model.getJniCommunicator().startBundle(model.getBundleLocation() + "/" + item.getFilename());
                } else if (item.getStatus() == BundleStatus.BUNDLE_RUNNING) {
                    b.setText("Stopping");
                    item.setStatus(BundleStatus.BUNDLE_STOPPING);
                    Celix.getInstance().stopBundle(model.getBundleLocation() + "/" + item.getFilename());
//                    model.getJniCommunicator().stopBundle(model.getBundleLocation() + "/" + item.getFilename());
                }
            }
        });

        switch (status) {
            case BUNDLE_LOCALLY_AVAILABLE:
                holder.installButton.setEnabled(true);
                holder.installButton.setText("Install");
                holder.runButton.setEnabled(false);
                holder.runButton.setText("Not installed");
                break;
            case BUNDLE_INSTALLING:
                holder.installButton.setEnabled(false);
                holder.installButton.setText("Installing");
                holder.runButton.setEnabled(false);
                holder.runButton.setText("Not installed");
                break;
            case BUNDLE_INSTALLED:
                holder.installButton.setEnabled(true);
                holder.installButton.setText("Delete");

                holder.runButton.setEnabled(true);
                holder.runButton.setText("Start");
                break;
            case BUNDLE_STARTING:
                holder.installButton.setEnabled(false);
                holder.installButton.setText("Installed");

                holder.runButton.setEnabled(false);
                holder.runButton.setText("Starting");
            case BUNDLE_RUNNING:
                holder.installButton.setEnabled(false);
                holder.installButton.setText("Installed");

                holder.runButton.setEnabled(true);
                holder.runButton.setText("Stop");
                break;
            case BUNDLE_STOPPING:
                holder.installButton.setEnabled(false);
                holder.installButton.setText("Installed");
                holder.runButton.setEnabled(false);
                holder.runButton.setText("Stopping");
                break;
            case BUNDLE_DELETING:
                holder.installButton.setEnabled(false);
                holder.installButton.setText("Deleting");
                holder.runButton.setEnabled(false);
                holder.runButton.setText("Start");
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
    }

}
