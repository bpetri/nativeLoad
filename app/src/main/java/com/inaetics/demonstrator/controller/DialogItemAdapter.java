/**
 * Licensed under Apache License v2. See LICENSE for more information.
 */

package com.inaetics.demonstrator.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import com.inaetics.demonstrator.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter used for the install dialog on the OsgiBundlesFragment.
 */
public class DialogItemAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    List<Pair> items;

    /**
     * Constructor of DialogItemAdapter
     * Creates a new ArrayList from the given items and sets all boolean values to false.
     * This means that all items are unchecked
     * @param context Context for inflating the layout
     * @param _items String[] holding all the bundle names.
     */
    public DialogItemAdapter(Context context, String[] _items) {
        this.items = new ArrayList<>();
        for(String item : _items) {
            items.add(new Pair(item, false));
        }
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Pair getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Method for retrieving all the selected bundles
     * @return ArrayList with strings that represent the bundles
     */
    public ArrayList<String> getInstallBundles() {
        ArrayList<String> result = new ArrayList<>();
        for(Pair item : items) {
            if(item.checked) {
                result.add(item.bundle);
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
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Pair item = getItem(position);

        String fileName = item.bundle;
        boolean isChecked = item.checked;

        holder.dialogCheckbox.setChecked(isChecked);
        holder.dialogCheckbox.setText(fileName);

        holder.dialogCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox box = (CheckBox) view;
                item.checked = box.isChecked();
            }
        });

        return convertView;
    }

    static class ViewHolder {
        CheckBox dialogCheckbox;
    }

    /**
     * Class for pairing a bundle to a boolean
     */
    private class Pair {
        public String bundle;
        public Boolean checked;

        Pair(String bundle, boolean checked) {
            this.bundle = bundle;
            this.checked = checked;
        }
    }

}
