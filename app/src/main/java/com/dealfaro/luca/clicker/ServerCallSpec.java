package com.dealfaro.luca.clicker;

import android.content.Context;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ServerCallSpec {

    private static final String LOG_TAG = "ServerCallSpec";

    // Specification for accessing the server.
    ServerCallSpec() {};

    public String url;
    public Context context;
    public UrlEncodedFormEntity form;
    public void useResult(Context context, String r) {}

    public boolean setParams(HashMap<String, String> params) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pair = it.next();
            nameValuePairs.add(new BasicNameValuePair(pair.getKey(), pair.getValue()));
        }
        try {
            form = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "Encoding exception: " + e.toString());
            return false;
        }
        return true;
    }

}