/**
 * Licensed under Apache License v2. See LICENSE for more information.
 */

package org.apache.celix.app.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.apache.celix.app.R;

import java.util.ArrayList;
import java.util.List;

public class DialogItemAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    List<Pair<String, Boolean>> items;
    String[] keys;

    public DialogItemAdapter(Context context, String[] _items) {
        this.items = new ArrayList<>();
        for(String item : _items) {
            items.add(new Pair<>(item, false));
        }
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Pair<String, Boolean> getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public ArrayList<String> getInstallBundles() {
        ArrayList<String> result = new ArrayList<>();
        for(Pair<String, Boolean> item : items) {
            if(item.second) {
                result.add(item.first);
            }
        }
        return result;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            //Use convertview for recycling views, smoother scrolling.
            convertView = inflater.inflate(R.layout.dialog_item, parent, false);
            // View holder pattern, find all views once, smoother scrolling
            holder = new ViewHolder();
            holder.dialogCheckbox = (CheckBox) convertView.findViewById(R.id.dialogCheckbox);
            holder.dialogFileName = (TextView) convertView.findViewById(R.id.dialogFileName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

//        if (model.getCelixStatus() == BundleStatus.CELIX_RUNNING) {
//            holder.bundleCheckbox.setVisibility(View.GONE);
//            holder.buttonsLayout.setVisibility(View.VISIBLE);
//        } else {
//            holder.bundleCheckbox.setVisibility(View.VISIBLE);
//            holder.buttonsLayout.setVisibility(View.GONE);
//        }

        final Pair<String, Boolean> item = getItem(position);

        String fileName = item.first;
        boolean isChecked = item.second;

        holder.dialogFileName.setText(fileName);
        holder.dialogCheckbox.setChecked(isChecked);

        holder.dialogCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox box = (CheckBox) view;
                item.second = box.isChecked();
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView dialogFileName;
        CheckBox dialogCheckbox;
    }

    private class Pair<K,V> {
        public K first;
        public V second;

        Pair(K first, V second) {
            this.first = first;
            this.second = second;
        }
    }

}
