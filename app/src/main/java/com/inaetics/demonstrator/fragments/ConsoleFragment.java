package com.inaetics.demonstrator.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.inaetics.demonstrator.R;
import com.inaetics.demonstrator.logging.LogCatOut;
import com.inaetics.demonstrator.logging.LogCatReader;
import com.inaetics.demonstrator.model.BundleStatus;
import com.inaetics.demonstrator.model.Config;
import com.inaetics.demonstrator.model.Model;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

/**
 * Created by mjansen on 17-9-15.
 */
public class ConsoleFragment extends Fragment implements Observer {
    private EditText console;
    private Config config;
    private Model model;
    private LogCatReader lr;
    private Button btn_start;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.console_fragment,null);
        console = (EditText) rootView.findViewById(R.id.console_log);
        model = Model.getInstance();
        config = model.getConfig();
        model.addObserver(this);

        final Handler handler = new Handler();
        lr = new LogCatReader(new LogCatOut()
            {
            @Override
            public void writeLogData(final String line) throws IOException
                {
                    handler.post(new Runnable()
                    {
                        public void run() {
                            console.append(line + "\n");
                            if (console.getText().length() > 10000) {
                                console.setText(console.getText().toString().substring(5000));
                                console.setSelection(console.getText().length());
                            }

                        }
                    });
                }
        });
        lr.start();

        btn_start = (Button) rootView.findViewById(R.id.start_stop_btn);
        if (model.getCelixStatus() == BundleStatus.CELIX_RUNNING) {
            setRunning();
        } else {
            setStopped();
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (lr != null) {
            lr.kill();
        }
    }

    public void setRunning() {
        btn_start.setText("STOP");
        if (isAdded()) {
            btn_start.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        }
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                model.getJniCommunicator().stopCelix();
            }
        });
        btn_start.setEnabled(true);
    }

    public void setStopped() {
        btn_start.setText("Start");
        if (isAdded()) {
            btn_start.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        }

        btn_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("Start button", "Started");
                SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences("celixAgent", Context.MODE_PRIVATE);
                String cfgPath = getActivity().getApplicationContext().getFilesDir() + "/" + Config.CONFIG_PROPERTIES;
                String cfgStr = prefs.getString("celixConfig", null);
                Properties cfgProps = null;
                if (cfgStr != null)
                    cfgProps = config.generateConfiguration(config.stringToProperties(cfgStr), model.getBundles(), model.getBundleLocation(), getActivity().getBaseContext());
                else
                    cfgProps = config.generateConfiguration(null, model.getBundles(), model.getBundleLocation(), getActivity().getBaseContext());

                if (config.writeConfiguration(getActivity().getApplicationContext(), config.propertiesToString(cfgProps))) {
                    btn_start.setEnabled(false);
                    console.setText("");
                    model.getJniCommunicator().startCelix(cfgPath);
                }
            }
        });
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o == BundleStatus.CELIX_RUNNING) {
            setRunning();
        } else if (o == BundleStatus.CELIX_STOPPED) {
            setStopped();
        }
    }

}


