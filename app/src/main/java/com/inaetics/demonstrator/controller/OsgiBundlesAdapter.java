package com.inaetics.demonstrator.controller;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andexert.expandablelayout.library.ExpandableLayoutItem;
import com.inaetics.demonstrator.R;
import com.inaetics.demonstrator.model.BundleStatus;

import java.util.List;

import apache.celix.Celix;
import apache.celix.model.OsgiBundle;

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
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(resource, parent, false);
            holder = new ViewHolder();
            holder.layout = (ExpandableLayoutItem) convertView.findViewById(R.id.layout_item);
            holder.header = holder.layout.getHeaderLayout();
            holder.content = holder.layout.getContentLayout();

            holder.tv_id = (TextView) holder.header.findViewById(R.id.id_text);
            holder.tv_status = (TextView) holder.header.findViewById(R.id.status_text);
            holder.tv_symName = (TextView) holder.header.findViewById(R.id.symb_name_text);

            holder.startStop = (ImageButton) holder.content.findViewById(R.id.start_stop_button);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final OsgiBundle bundle = getItem(position);

        holder.tv_id.setText(bundle.getId() + "");
        holder.tv_status.setText(bundle.getStatus());
        holder.tv_symName.setText(bundle.getSymbolicName());

        holder.startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("osgiAdapter", "Onclick called");
                ImageButton b = (ImageButton) v;
                b.setEnabled(false);
                String status = bundle.getStatus();
                if (status.equals("Installed") || status.equals("Resolved")) {
                    holder.startStop.setImageResource(R.drawable.ic_block_black_48dp);
                    Celix.getInstance().startBundleById(bundle.getId());
                } else if (status.equals("Active")) {
                    holder.startStop.setImageResource(R.drawable.ic_block_black_48dp);
                    Celix.getInstance().stopBundleById(bundle.getId());
                }
            }
        });

        String status = bundle.getStatus();

        switch (status) {
            case "Active":
                holder.startStop.setImageResource(R.drawable.ic_pause_circle_filled_black_48dp);
                holder.startStop.setEnabled(true);
                break;
            case "Installed":
            case "Resolved":
                holder.startStop.setImageResource(R.drawable.ic_play_circle_filled_black_48dp);
                holder.startStop.setEnabled(true);
                break;
            case "Starting":
            case "Stopping":
            case "Deleted":
            default:
                holder.startStop.setImageResource(R.drawable.ic_block_black_48dp);
                holder.startStop.setEnabled(false);
        }

        return convertView;
    }

    static class ViewHolder {
        ExpandableLayoutItem layout;
        FrameLayout header, content;
        TextView tv_id, tv_status, tv_symName;
        ImageButton startStop;
    }
}
