package com.dealfaro.luca.clicker;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * This class performs an HTTP call according to the ServerCallSpec specification,
 * and post-processes it according to the PostProcess specifications.
 */
public class ServerCall extends AsyncTask<ServerCallSpec, String, PostProcessPair> {

    private static final int MAX_SETUP_DOWNLOAD_TRIES = 3;
    private static final String LOG_TAG = "ServerCall";

    protected PostProcessPair doInBackground(ServerCallSpec... specs) {
        String downloadedString = null;
        ServerCallSpec spec = specs[0];
        Log.i(LOG_TAG, "Starting the server call to URL " + spec.url);
        URI url = URI.create(spec.url);
        int numTries = 0;
        while (downloadedString == null && numTries < MAX_SETUP_DOWNLOAD_TRIES && !isCancelled()) {
            numTries++;
            HttpPost request = new HttpPost(url);
            // We need to add the parameters.
            request.setEntity(spec.form);

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = null;
            try {
                response = httpClient.execute(request);
            } catch (ClientProtocolException ex) {
                Log.e(LOG_TAG, ex.toString());
            } catch (IOException ex) {
                Log.e(LOG_TAG, ex.toString());
            }
            if (response != null) {
                // Checks the status code.
                int statusCode = response.getStatusLine().getStatusCode();
                Log.d(LOG_TAG, "Status code: " + statusCode);

                if (statusCode == HttpURLConnection.HTTP_OK) {
                    // Correct response. Reads the real result.
                    // Extracts the string content of the response.
                    HttpEntity entity = response.getEntity();
                    InputStream iStream = null;
                    try {
                        iStream = entity.getContent();
                    } catch (IOException ex) {
                        Log.e(LOG_TAG, ex.toString());
                    }
                    if (iStream != null) {
                        downloadedString = ConvertStreamToString(iStream);
                        Log.d(LOG_TAG, "Received string: " + downloadedString);
                        // Postprocess the result.
                        PostProcessPair instr = new PostProcessPair();
                        instr.spec = spec;
                        instr.result = downloadedString;
                        return instr;
                    }
                }
            }
        }
        // Postprocess the result.
        PostProcessPair instr = new PostProcessPair();
        instr.spec = spec;
        instr.result = downloadedString;
        return instr;
    }

    protected void onPostExecute(PostProcessPair instr) {
        // This is executed in the UI thread.
        instr.spec.useResult(instr.spec.context, instr.result);
    }

    private static String ConvertStreamToString(InputStream is) {
        if (is == null) {
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append((line + "\n"));
            }
        } catch (IOException e) {
            Log.d(LOG_TAG, e.toString());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.d(LOG_TAG, e.toString());
            }
        }
        return sb.toString();
    }

}
