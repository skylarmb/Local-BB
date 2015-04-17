package com.dealfaro.luca.clicker;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity {

    Location lastLocation;
    private double lastAccuracy = (double) 1e10;
    private long lastAccuracyTime = 0;

    private static final String LOG_TAG = "lclicker";

    private static final float GOOD_ACCURACY_METERS = 100;

    // This is an id for my app, to keep the key space separate from other apps.
    private static final String MY_APP_ID = "luca_bboard";

    private static final String SERVER_URL_PREFIX = "https://luca-teaching.appspot.com/store/default/";

    // To remember the favorite account.
    public static final String PREF_ACCOUNT = "pref_account";

    // Uploader.
    private ServerCall uploader;

    // Remember whether we have already successfully checked in.
    private boolean checkinSuccessful = false;

    private ArrayList<String> accountList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }



    @Override
    protected void onPause() {
        // Stops the upload if any.
        if (uploader != null) {
            uploader.cancel(true);
            uploader = null;
        }
        super.onPause();
    }


    public void clickButton(View v) {

        // Get the text we want to send.
        EditText et = (EditText) findViewById(R.id.editText);
        String msg = et.getText().toString();

        // Then, we start the call.
        PostMessageSpec myCallSpec = new PostMessageSpec();


        myCallSpec.url = SERVER_URL_PREFIX + "post_msg.json";
        myCallSpec.context = MainActivity.this;
        // Let's add the parameters.
        HashMap<String,String> m = new HashMap<String,String>();
        m.put("app_id", MY_APP_ID);
        m.put("msg", msg);
        myCallSpec.setParams(m);
        // Actual server call.
        if (uploader != null) {
            // There was already an upload in progress.
            uploader.cancel(true);
        }
        uploader = new ServerCall();
        uploader.execute(myCallSpec);
    }


    private String reallyComputeHash(String s) {
        // Computes the crypto hash of string s, in a web-safe format.
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(s.getBytes());
            digest.update("My secret key".getBytes());
            byte[] md = digest.digest();
            // Now we need to make it web safe.
            String safeDigest = Base64.encodeToString(md, Base64.URL_SAFE);
            return safeDigest;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * This class is used to do the HTTP call, and it specifies how to use the result.
     */
    class PostMessageSpec extends ServerCallSpec {
        @Override
        public void useResult(Context context, String result) {
            // Translates the string result, decoding the Json.
            Log.i(LOG_TAG, "Received string: " + result);
            Gson gson = new Gson();
            MessageList ml = gson.fromJson(result, MessageList.class);
            String s = "";
            for (int i = 0; i < ml.messages.length; i++) {
                s = s + ml.messages[i] + ",";
            }
            TextView tv = (TextView) findViewById(R.id.textView);
            tv.setText(s);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
