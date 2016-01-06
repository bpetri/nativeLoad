package com.inaetics.demonstrator.controller;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.inaetics.demonstrator.model.Model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by marcojansen on 19-11-15.
 * AsyncTask to download bundles from an url to the desired bundle location.
 */
public class DownloadTask extends AsyncTask<String,Void,Void> {
    private ProgressDialog dialog;
    private Context context;

    public DownloadTask(Context context, ProgressDialog dialog) {
        this.dialog = dialog;
        this.context = context;
    }

    /**
     * The downloading background task
     * @param params    First param should be the link where to download from
     * @return          Null.(Void)
     */
    @Override
    protected Void doInBackground(String... params) {
        String link = params[0];
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(link);
        String fileName= URLUtil.guessFileName(link, null, fileExtension);
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection;
        try {
            URL url = new URL(link);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e("Error response",connection.getResponseCode() + " " + connection.getResponseMessage());
            }

            input = connection.getInputStream();
            output = new FileOutputStream(Model.getInstance().getBundleLocation()+ "/" + fileName);

            byte data[] = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1 ) {
                if (isCancelled()) {
                    input.close();
                }
                output.write(data, 0 , count);
            }
        } catch (IOException e) {
            e.printStackTrace();
            dialog.dismiss();
            publishProgress();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    /**
     * If the downloading failed, progress will be updated and a toast will be showed stating
     * it failed.
     * @param values    Is Void
     */
    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        Toast.makeText(context, "Downloading failed!", Toast.LENGTH_SHORT).show();
    }

    /**
     * If downloading went successful, the progress dialog will be dismissed.
     * @param aVoid     Is Void
     */
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Toast.makeText(context, "Downloading successfull!", Toast.LENGTH_SHORT).show();
        dialog.dismiss();

    }
}
