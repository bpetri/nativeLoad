package com.inaetics.demonstrator.fragments;

import android.os.Bundle;
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
import apache.celix.model.Update;

/**
 * Created by mjansen on 17-9-15.
 * Fragment for showing the output of the Celix framework and bundles
 */
public class ConsoleFragment extends Fragment implements Observer {
    private EditText console;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate fragment layout
        final View rootView = inflater.inflate(R.layout.console_fragment, container, false);

        console = (EditText) rootView.findViewById(R.id.console_log);
        console.setText(Celix.getInstance().getStdio());

        Celix.getInstance().addObserver(this);

        return rootView;
    }

    /**
     * Delete observer when view gets destroyed
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Celix.getInstance().deleteObserver(this);
    }

    /**
     * Observes the Celix Instance. If log has changed it will request the new lines
     * and append this to the console.
     * @param observable        The observable ( Celix in this case )
     * @param data              Data send with this update call
     */
    @Override
    public void update(Observable observable, Object data) {
        if (data == Update.LOG_CHANGED) {
            final String newLines = Celix.getInstance().getStdio();
            console.append(newLines);
            if (console.getText().length() > 3000) {
                console.setText(console.getText().toString().substring(500));
            }
            console.setSelection(console.getText().length());
        }
    }
}


