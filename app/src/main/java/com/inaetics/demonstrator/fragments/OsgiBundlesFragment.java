package com.inaetics.demonstrator.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.inaetics.demonstrator.R;
import com.inaetics.demonstrator.controller.OsgiBundlesAdapter;
import com.inaetics.demonstrator.model.Model;

import java.util.Observable;
import java.util.Observer;

import apache.celix.Celix;
import apache.celix.model.OsgiBundle;

/**
 * Created by mjansen on 27-10-15.
 */
public class OsgiBundlesFragment extends Fragment implements Observer {
    private OsgiBundlesAdapter adapter;
    private ListView list;
    private Model model;
    private Handler handler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.bundles_fragment, container, false);

        model = Model.getInstance();
        Celix.getInstance().addObserver(this);
        handler = new Handler();
        list = (ListView) rootview.findViewById(R.id.bundles_listview);
        adapter = new OsgiBundlesAdapter(getActivity(), R.layout.osgi_bundle_item, model.getOsgiBundles());
        list.setAdapter(adapter);

        return rootview;
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
                    existingBundle.setStatus(newBundle.getStatus());
                    adapter.notifyDataSetChanged();
                }
            } else {
                model.getOsgiBundles().add(newBundle);
                adapter.notifyDataSetChanged();
            }
        }

    }
}
