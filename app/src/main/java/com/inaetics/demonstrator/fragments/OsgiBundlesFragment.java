package com.inaetics.demonstrator.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.andexert.expandablelayout.library.ExpandableLayoutListView;
import com.inaetics.demonstrator.R;
import com.inaetics.demonstrator.controller.DialogItemAdapter;
import com.inaetics.demonstrator.controller.OsgiBundlesAdapter;
import com.inaetics.demonstrator.model.Model;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import apache.celix.Celix;
import apache.celix.model.CelixUpdate;
import apache.celix.model.OsgiBundle;

/**
 * Created by mjansen on 27-10-15.
 */
public class OsgiBundlesFragment extends Fragment implements Observer {
    private OsgiBundlesAdapter adapter;
    private ExpandableLayoutListView list;
    private FloatingActionButton fab;
    private Model model;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.osgi_bundles_fragment, container, false);

        model = Model.getInstance();
        Celix.getInstance().addObserver(this);
        list = (ExpandableLayoutListView) rootview.findViewById(R.id.osgi_listview);
        fab = (FloatingActionButton) rootview.findViewById(R.id.fab);

        adapter = new OsgiBundlesAdapter(getActivity(), R.layout.osgi_bundles_row, model.getOsgiBundles());
        list.setAdapter(adapter);

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final OsgiBundle bundle = adapter.getItem(position);
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("Warning");
                dialog.setMessage("Are you sure to delete " + bundle.getSymbolicName() + "?");
                dialog.setNegativeButton("Cancel", null);
                dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Celix.getInstance().deleteBundleById(bundle.getId());

                    }
                });
                dialog.show();
                return true;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        fab.hide();

        return rootview;
    }

    private void showDialog() {
        Context context = getActivity();
        AlertDialog.Builder dialog = new AlertDialog.Builder(context,R.style.DialogTheme);
        dialog.setTitle("Select bundle(s)");
        String[] files = new File(model.getBundleLocation()).list();

        ListView listView = new ListView(context);
        final DialogItemAdapter adapter = new DialogItemAdapter(context, files);
        listView.setAdapter(adapter);
        dialog.setView(listView);

        dialog.setNegativeButton("Install", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String bundleLocation = model.getBundleLocation();
                Celix celix = Celix.getInstance();
                for (String fileName : adapter.getInstallBundles()) {
                    celix.installBundle(bundleLocation + "/" + fileName);
                }
            }
        });

        dialog.setPositiveButton("Start", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String bundleLocation = model.getBundleLocation();
                Celix celix = Celix.getInstance();
                for (String fileName : adapter.getInstallBundles()) {
                    celix.installStartBundle(bundleLocation + "/" + fileName);
                }
            }
        });

        dialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.show();


    }

    @Override
    public void update(Observable observable, final Object o) {
        if (o instanceof OsgiBundle) {
            final OsgiBundle newBundle = (OsgiBundle) o;
            OsgiBundle existingBundle = null;
            for (OsgiBundle b : model.getOsgiBundles()) {
                if (newBundle.getId() == b.getId()) {
                    existingBundle = b;
                }
            }
            if (existingBundle != null) {
                if (!existingBundle.getStatus().equals(newBundle.getStatus())) {
                    if (newBundle.getStatus().equals("Deleted")) {
                        model.getOsgiBundles().remove(existingBundle);
                    } else {
                        existingBundle.setStatus(newBundle.getStatus());
                    }
                    adapter.notifyDataSetChanged();
                }
            } else {
                model.getOsgiBundles().add(newBundle);
                adapter.notifyDataSetChanged();
            }
        } else if (o == CelixUpdate.CELIX_CHANGED) {
            if(Celix.getInstance().isCelixRunning()) {
                fab.show();
            } else {
                fab.hide();
                model.getOsgiBundles().clear();
                adapter.notifyDataSetChanged();
            }
        }

    }
}
