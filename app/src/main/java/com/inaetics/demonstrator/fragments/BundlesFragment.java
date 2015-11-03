package com.inaetics.demonstrator.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.inaetics.demonstrator.R;
import com.inaetics.demonstrator.controller.BundleItemAdapter;
import com.inaetics.demonstrator.model.Model;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by mjansen on 17-9-15.
 */
public class BundlesFragment extends Fragment implements Observer {

    private BundleItemAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bundles_fragment, container, false);

        Model model = Model.getInstance();
        model.addObserver(this);

        adapter = new BundleItemAdapter(getActivity().getBaseContext(),R.layout.bundle_item,model.getBundles(),model);
        ListView bundleList = (ListView) rootView.findViewById(R.id.bundles_listview);

        bundleList.setAdapter(adapter);
        return rootView;
    }

    /**
     * Update adapter if the model has been changed. (Bundle status etc)
     *
     * @param observable The observable we're observing
     * @param o          Data added to the notifyobservers method.
     */
    @Override
    public void update(Observable observable, Object o) {
        adapter.notifyDataSetChanged();
    }
}
