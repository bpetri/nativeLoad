package com.inaetics.demonstrator.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.inaetics.demonstrator.R;
import com.inaetics.demonstrator.controller.OsgiBundlesAdapter;
import com.inaetics.demonstrator.model.BundleStatus;
import com.inaetics.demonstrator.model.Model;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import apache.celix.Celix;
import apache.celix.model.OsgiBundle;

/**
 * Created by mjansen on 27-10-15.
 */
public class OsgiBundlesFragment extends Fragment implements Observer {
    private OsgiBundlesAdapter adapter;
    private Handler handler;
    private ListView list;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.bundles_fragment, container, false);
        list = (ListView) rootview.findViewById(R.id.bundles_listview);
        Model model = Model.getInstance();
        model.addObserver(this);
        handler = new Handler();
        adapter = new OsgiBundlesAdapter(getActivity(), R.layout.osgi_bundle_item, model.getOsgiBundles());
        list.setAdapter(adapter);

        return rootview;
    }

    @Override
    public void update(Observable observable, Object o) {
        adapter.notifyDataSetChanged();
    }
}
