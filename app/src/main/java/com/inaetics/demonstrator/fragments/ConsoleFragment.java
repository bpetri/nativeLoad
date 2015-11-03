package com.inaetics.demonstrator.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.inaetics.demonstrator.R;

import java.util.Observable;
import java.util.Observer;

import apache.celix.Celix;
import apache.celix.model.CelixUpdate;

/**
 * Created by mjansen on 17-9-15.
 */
public class ConsoleFragment extends Fragment implements Observer {
    private EditText console;
    private Handler handler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate fragment layout
        final View rootView = inflater.inflate(R.layout.console_fragment, container, false);

        console = (EditText) rootView.findViewById(R.id.console_log);
        handler = new Handler();
        console.setText(Celix.getInstance().getStdio());

        Celix.getInstance().addObserver(this);

        return rootView;
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data == CelixUpdate.LOG_CHANGED) {
            final String newLines = Celix.getInstance().getStdio();
            console.append(newLines);
            if (console.getText().length() > 3000) {
                console.setText(console.getText().toString().substring(500));
            }
            console.setSelection(console.getText().length());
        }
    }
}


