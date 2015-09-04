/**
 * Copyright (c) 2011 Mujtaba Hassanpur.
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.example.bjoern.nativeload;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Downloads a file in a thread. Will send messages to the
 * MainActivity activity to update the progress bar.
 */
public class DownloaderThread extends Thread
{
	// constants
    private final String TAG = DownloaderThread.class.getName();

    private static final int DOWNLOAD_BUFFER_SIZE = 4096;
    public static String DEFAULT_BUNDLE_LOCATION = "https://github.com/bpetri/celix-bundles/blob/master/";

    // instance variables
	private MainActivity parentActivity;
	private ArrayList<BundleItem> downloadBundles;
    private String destFolder;
	private String baseUrl;


	/**
	 * Instantiates a new DownloaderThread object.
	 * @param parentActivity Reference to MainActivity activity.
	 * @param inUrl String representing the URL of the file to be downloaded.
	 */
	public DownloaderThread(MainActivity inParentActivity, ArrayList<BundleItem> inBundles, String dst)
	{
		if(inBundles != null)
		{
            downloadBundles = inBundles;
		}
        destFolder = dst;
		parentActivity = inParentActivity;

        SharedPreferences prefs = inParentActivity.getApplicationContext().getSharedPreferences("celixAgent", Context.MODE_PRIVATE);
        baseUrl = prefs.getString("bundleUrl", null);

        if (baseUrl == null) {
            baseUrl = DEFAULT_BUNDLE_LOCATION + System.getProperty("os.arch");
            prefs.edit().putString("bundleUrl", DEFAULT_BUNDLE_LOCATION).apply();
        }
        else {
            baseUrl = baseUrl + "/" + System.getProperty("os.arch");
        }
    }


    private boolean networkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) parentActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            try {

                Log.d(TAG, "Trying to connect to : " + baseUrl);

                URL url = new URL(baseUrl);
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setRequestProperty("User-Agent", "test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1000); // mTimeout is in seconds
                urlc.connect();
                int responseCode = urlc.getResponseCode();

                Log.d(TAG, "ResponseCode is : " +responseCode);

                return true;

            } catch (Exception e) {
                Log.e(TAG, "Error checking connection: " + e.getMessage(), e);
                return false;
            }
        } else {
            return false;
        }
    }

    private void refreshView(final MainActivity parentActivity) {
        parentActivity.runOnUiThread(
        new Runnable() {
            @Override
            public void run() {
                Log.d("REFRESDH ", "refreshview now");
                parentActivity.bundleAdapter.notifyDataSetChanged();
                        //setProgressBarIndeterminateVisibility();
                //parentActivity.bundleListView.postInvalidate();
            }
        });
    }

	/**
	 * Connects to the URL of the file, begins the download, and notifies the
	 * MainActivity activity of changes in state. Writes the file to
	 * the root of the SD card.
	 */
	@Override
	public void run() {

        URL url;
        URLConnection conn;
        int fileSize, lastSlash;
        String fileName;
        BufferedInputStream inStream;
        BufferedOutputStream outStream;
        File outFile;
        FileOutputStream fileStream;

        if(networkAvailable()) {

            Log.v(TAG, "Network available");
            for (BundleItem downloadBundle : downloadBundles) {

                String downloadUrl = baseUrl + "/" + downloadBundle.getFilename();
                Log.v(TAG, "opening connection to " + downloadBundle.getFilename());

                downloadBundle.setStatus(BundleItem.BUNDLE_CONNECTING_STARTED);
                refreshView(parentActivity);

                try {
                    url = new URL(downloadUrl);


                    Log.v(TAG, "opening connection to " + downloadUrl);

                    conn = url.openConnection();
                    conn.setUseCaches(false);
                    fileSize = conn.getContentLength();

                    // get the filename
                    lastSlash = url.toString().lastIndexOf('/');
                    fileName = "file.bin";
                    if (lastSlash >= 0) {
                        fileName = url.toString().substring(lastSlash + 1);
                    }
                    if (fileName.equals("")) {
                        fileName = "file.bin";
                    }

                    // notify download start
                    Integer fileSizeInKB = fileSize / 1024;

                    downloadBundle.setStatus(BundleItem.BUNDLE_DOWNLOAD_STARTED);
                    downloadBundle.setStatusInfo(fileSizeInKB);
                    refreshView(parentActivity);

                    // start download
                    Log.v(TAG, "saving bundle to " + destFolder + "/" + fileName);

                    inStream = new BufferedInputStream(conn.getInputStream());
                    outFile = new File(destFolder + "/" + fileName);
                    fileStream = new FileOutputStream(outFile);
                    outStream = new BufferedOutputStream(fileStream, DOWNLOAD_BUFFER_SIZE);
                    byte[] data = new byte[DOWNLOAD_BUFFER_SIZE];
                    int bytesRead = 0, totalRead = 0;
                    while (!isInterrupted() && (bytesRead = inStream.read(data, 0, data.length)) >= 0) {
                        outStream.write(data, 0, bytesRead);

                        // update progress bar
                        totalRead += bytesRead;
                        Integer totalReadInKB = totalRead / 1024;

                        downloadBundle.setStatus(BundleItem.BUNDLE_UPDATE_PROGRESS_BAR);
                        downloadBundle.setStatusInfo(totalReadInKB);
                        refreshView(parentActivity);

                    }

                    outStream.close();
                    fileStream.close();
                    inStream.close();

                    if (isInterrupted()) {
                        // the download was canceled, so let's delete the partially downloaded file
                        outFile.delete();
                    } else {
                        downloadBundle.setStatus(BundleItem.BUNDLE_DOWNLOAD_COMPLETE);

                    }
                } catch (MalformedURLException e) {
                    String errMsg = parentActivity.getString(R.string.error_message_bad_url);
                    downloadBundle.setStatus(BundleItem.BUNDLE_ENCOUNTERED_ERROR);
                    downloadBundle.setStatusInfo(errMsg);
                } catch (FileNotFoundException e) {
                    String errMsg = parentActivity.getString(R.string.error_message_file_not_found);
                    downloadBundle.setStatus(BundleItem.BUNDLE_ENCOUNTERED_ERROR);
                    downloadBundle.setStatusInfo(errMsg);
                } catch (Exception e) {
                    String errMsg = parentActivity.getString(R.string.error_message_general) + " " + e.getMessage();
                    downloadBundle.setStatus(BundleItem.BUNDLE_ENCOUNTERED_ERROR);
                    downloadBundle.setStatusInfo(errMsg);
                } finally {
                    refreshView(parentActivity);
                }
            }
        }
        else {
            parentActivity.showNoConnectionDialog(parentActivity);
        }
    }
}
