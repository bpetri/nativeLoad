package com.inaetics.demonstrator.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.inaetics.demonstrator.R;
import com.inaetics.demonstrator.model.OsgiBundle;

import java.util.List;

/**
 * Created by mjansen on 27-10-15.
 */
public class OsgiBundlesAdapter extends ArrayAdapter<OsgiBundle> {
    private int resource;
    private LayoutInflater inflater;

    public OsgiBundlesAdapter(Context context, int resource, List<OsgiBundle> objects) {
        super(context, resource, objects);
        this.resource = resource;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(resource, parent, false);
        }
        OsgiBundle bundle = getItem(position);
        TextView id, status, sname;
        id = (TextView) convertView.findViewById(R.id.id_text);
        status = (TextView) convertView.findViewById(R.id.status_text);
        sname = (TextView) convertView.findViewById(R.id.symb_name_text);

        id.setText(bundle.getId() + "");
        status.setText(bundle.getStatus());
        sname.setText(bundle.getSymbolicName());


        return convertView;
    }
}
