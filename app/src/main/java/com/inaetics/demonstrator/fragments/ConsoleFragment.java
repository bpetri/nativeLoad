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
import com.inaetics.demonstrator.logging.LogCatOut;
import com.inaetics.demonstrator.logging.LogCatReader;

/**
 * Created by mjansen on 17-9-15.
 */
public class ConsoleFragment extends Fragment {
    private EditText console;
    private LogCatReader lr;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate fragment layout
        final View rootView = inflater.inflate(R.layout.console_fragment, container, false);
        console = (EditText) rootView.findViewById(R.id.console_log);
        final Handler handler = new Handler();
        lr = new LogCatReader(new LogCatOut()
            {
            @Override
            public void writeLogData(final String line)
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

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // the logreader should be killed when destroyed
        if (lr != null) {
            lr.kill();
        }
    }
}


