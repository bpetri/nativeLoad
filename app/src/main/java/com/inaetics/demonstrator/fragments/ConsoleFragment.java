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

import com.inaetics.demonstrator.logging.LogCatOut;
import com.inaetics.demonstrator.logging.LogCatReader;
import com.inaetics.demonstrator.model.BundleStatus;
import com.inaetics.demonstrator.model.Config;
import com.inaetics.demonstrator.model.Model;
import com.inaetics.demonstrator.nativeload.R;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by mjansen on 17-9-15.
 */
public class ConsoleFragment extends Fragment {
    private EditText console;
    private Config config;
    private Model model;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.console_fragment,null);
        console = (EditText) rootView.findViewById(R.id.console_log);
        model = Model.getInstance();
        config = model.getConfig();

        final Handler handler = new Handler();
        final LogCatReader lr = new LogCatReader(new LogCatOut()
        {
            @Override
            public void writeLogData(final String line) throws IOException
            {
                handler.post(new Runnable()
                {
                    public void run()
                    {
                       console.append(line + System.getProperty("line.separator"));

                    }
                });
            }
        });
        final Button btn_start = (Button) rootView.findViewById(R.id.start_stop_btn);
        if (model.getCelixStatus() == BundleStatus.CELIX_RUNNING) {
            btn_start.setEnabled(false);
            lr.start();
        }
        btn_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("Start button", "Started");
                SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences("celixAgent", Context.MODE_PRIVATE);
                String cfgPath = getActivity().getApplicationContext().getFilesDir() + "/" + Config.CONFIG_PROPERTIES;
                String cfgStr = prefs.getString("celixConfig", null);
                Properties cfgProps = null;
                if (cfgStr != null)
                    cfgProps = config.generateConfiguration(config.stringToProperties(cfgStr),model.getBundles(),model.getBundleLocation(),getActivity().getBaseContext());
                else
                    cfgProps = config.generateConfiguration(null,model.getBundles(),model.getBundleLocation(),getActivity().getBaseContext());

                if (config.writeConfiguration(getActivity().getApplicationContext(), config.propertiesToString(cfgProps))) {
                    btn_start.setEnabled(false);
                    console.setText("");
                    lr.start();
                    model.getJniCommunicator().startCelix(cfgPath);
                }
            }
        });
        return rootView;
    }

}