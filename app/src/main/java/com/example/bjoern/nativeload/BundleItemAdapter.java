package com.example.bjoern.nativeload;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by bjoern on 29.05.15.
 */
public class BundleItemAdapter extends ArrayAdapter<BundleItem> {

    private final String TAG = BundleItemAdapter.class.getName();

    int resource;

    public BundleItemAdapter(Context _context, int _resource, List<BundleItem> _items) {
        super(_context, _resource, _items);
        resource = _resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout bundleView;
        final BundleItem item = getItem(position);

        String fileName = item.getFilename();
        int status = item.getStatus();
        boolean isChecked = item.isChecked();

        if (convertView == null) {
            bundleView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi;
            vi = (LayoutInflater) getContext().getSystemService(inflater);
            vi.inflate(resource, bundleView, true);
        } else {
            bundleView = (LinearLayout) convertView;
        }

        TextView bundleFileName = (TextView) bundleView.findViewById(R.id.bundleFileName);
        CheckBox bundleCheckbox = (CheckBox) bundleView.findViewById(R.id.bundleCheckbox);
        ProgressBar bundleProgressBar = (ProgressBar) bundleView.findViewById(R.id.bundleProgressBar);

        bundleFileName.setText(fileName);


        switch (status) {

            case BundleItem.BUNDLE_NOT_YET_INITIALIZED:
                bundleProgressBar.setVisibility(View.GONE);
                bundleCheckbox.setVisibility(View.VISIBLE);
                bundleCheckbox.setEnabled(false);
                bundleCheckbox.setChecked(false);
                break;
            case BundleItem.BUNDLE_DOWNLOAD_COMPLETE:
                Log.e("BundleItem", "COMPLETE:  " + fileName);
            case BundleItem.BUNDLE_LOCALLY_AVAILABLE:
                Log.e("BundleItem", "LOC_AV:  " + fileName);

                bundleProgressBar.setVisibility(View.GONE);

                bundleCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        item.setChecked(isChecked);
                    }
                });
                bundleCheckbox.setVisibility(View.VISIBLE);
                bundleCheckbox.setEnabled(true);
                bundleCheckbox.setChecked(isChecked);
                break;
            case BundleItem.BUNDLE_CONNECTING_STARTED:
                bundleProgressBar.setVisibility(View.VISIBLE);
                bundleCheckbox.setVisibility(View.VISIBLE);
                bundleCheckbox.setEnabled(false);
                bundleCheckbox.setChecked(false);


                break;
            case BundleItem.BUNDLE_DOWNLOAD_STARTED:
                Log.e("BundleItem", "DOWN_START:  " + fileName);

                bundleCheckbox.setEnabled(false);
                bundleCheckbox.setVisibility(View.GONE);

                Integer fileSizeInKB = (Integer) item.getStatusInfo();
                bundleProgressBar.setProgress(0);
                bundleProgressBar.setMax(fileSizeInKB);
                break;
            case BundleItem.BUNDLE_UPDATE_PROGRESS_BAR:
                Log.e("BundleItem", "UPD_PROGRESS:  " + fileName);

                bundleCheckbox.setEnabled(false);
                bundleCheckbox.setVisibility(View.GONE);
                Integer totalReadInKB = (Integer) item.getStatusInfo();
                bundleProgressBar.setProgress(totalReadInKB);
                break;
            case BundleItem.BUNDLE_ENCOUNTERED_ERROR:
                Log.e("BundleItem", "ENC_ERR:  " + fileName);

                bundleProgressBar.setVisibility(View.GONE);
                bundleCheckbox.setVisibility(View.VISIBLE);
                bundleCheckbox.setEnabled(false);
                bundleCheckbox.setChecked(false);

                String message = (String) item.getStatusInfo();
                Toast.makeText(getContext(), item.getFilename() + " " + message, Toast.LENGTH_SHORT).show();
                break;
            default:
                Log.e("Status", "UNKNOWN " + fileName + " " + status);
        }


        return bundleView;
    }

}
