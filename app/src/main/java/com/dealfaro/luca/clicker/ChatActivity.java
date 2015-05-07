package com.dealfaro.luca.clicker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;


public class ChatActivity extends ActionBarActivity {
    private AppInfo appInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appInfo = AppInfo.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        aList = new ArrayList<ListElement>();
        aa = new MyAdapter(this, R.layout.list_element, aList);
        ListView myListView = (ListView) findViewById(R.id.listView);
        myListView.setAdapter(aa);
        aa.notifyDataSetChanged();
        TextView tv = (TextView) findViewById(R.id.locTextView);
        tv.setText(getIntentMessage());
        showLoadingDialog();
    }

    private void refreshOnChange() {
        clickRefresh(findViewById(R.id.button2));
        dismissLoadingDialog();
    }

    private String getIntentMessage() {
        Intent intent = getIntent();
        String dest = intent.getStringExtra("com.dealfaro.luca.clicker.DESTINATION");
        return dest;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_chat, menu);
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
    Location lastLocation;
    private double lastAccuracy = (double) 1e10;
    private long lastAccuracyTime = 0;
    private boolean hasRefreshed = false;
    private static final String LOG_TAG = "lclicker";

    private static final float GOOD_ACCURACY_METERS = 100;

    // This is an id for my app, to keep the key space separate from other apps.
    private static final String MY_APP_ID = "luca_bboard";

    private static final String SERVER_URL_PREFIX = "https://hw3n-dot-luca-teaching.appspot.com/store/default/";

    // To remember the favorite account.
    public static final String PREF_ACCOUNT = "pref_account";

    // To remember the post we received.
    public static final String PREF_POSTS = "pref_posts";

    // Uploader.
    private ServerCall uploader;

    // Remember whether we have already successfully checked in.
    private boolean checkinSuccessful = false;

    private ArrayList<String> accountList;
    private ProgressDialog progress;


    //display infinite loading widget
    public void showLoadingDialog() {

        if (progress == null) {
            progress = new ProgressDialog(this);
            progress.setMessage("Loading");
        }
        progress.show();
    }
    //remove loading widget
    public void dismissLoadingDialog() {

        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
    }
    //the class for each message in the list. text and time are displayed up front,
    //timestamp and id are displayed in a toast when you click on a message
    private class ListElement {
        ListElement() {};
        public String textLabel;
        public String timeText;
        public String timeStamp;
        public String messageID;
    }

    private ArrayList<ListElement> aList;

    private class MyAdapter extends ArrayAdapter<ListElement> {

        int resource;
        Context context;

        public MyAdapter(Context _context, int _resource, List<ListElement> items) {
            super(_context, _resource, items);
            resource = _resource;
            context = _context;
            this.context = _context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout newView;

            ListElement w = getItem(position);

            // Inflate a new view if necessary.
            if (convertView == null) {
                newView = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource,  newView, true);
            } else {
                newView = (LinearLayout) convertView;
            }

            // Fills in the message.
            TextView mtv = (TextView) newView.findViewById(R.id.itemText);
            mtv.setText(w.textLabel);

            //Fills in the timestamp
            TextView ttv = (TextView) newView.findViewById(R.id.timeText);
            ttv.setText(w.timeText);

            // Set a listener for the whole list item.
            newView.setTag("Posted: " + w.timeStamp + "\nID: " + w.messageID);
            newView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String s = v.getTag().toString();
                    showToast(s);
                    //changeActivity();
                }
            });

            return newView;
        }
    }
    private MyAdapter aa;


    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // Do something with the location you receive.
            lastLocation = location;
            if(!hasRefreshed) {
                refreshOnChange();
                hasRefreshed = true;
            }
            //TextView tv = (TextView) findViewById(R.id.locTextView);
            //tv.setText(location.getLatitude() + ", " + location.getLongitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };


    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        //dismissLoadingDialog();
        super.onResume();
        // First super, then do stuff.
        // Let us display the previous posts, if any.
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String result = settings.getString(PREF_POSTS, null);
        if (result != null) {
            displayResult(result);
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        refreshOnChange();
    }

    @Override
    protected void onPause() {
        //makes sure a proper refresh happens next time you open a private message
        hasRefreshed = false;
        // Stops the upload if any.
        if (uploader != null) {
            uploader.cancel(true);
            uploader = null;
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
        super.onPause();
    }


    public void clickButton(View v) {

        // Get the text we want to send.
        EditText et = (EditText) findViewById(R.id.editText);
        String msg = et.getText().toString();
        //Make sure message isn't empty or blank
        if(msg != null && !msg.trim().isEmpty()) {
            showLoadingDialog();
            String lat;
            String lng;
            //catch error in case of no location
            try {
                lat = Double.toString(lastLocation.getLatitude());
                lng = Double.toString(lastLocation.getLongitude());
            }catch(IllegalStateException e){
                showToast("Unable to get location");
                return;
            }
            String msgid = randomString(8);
            // Then, we start the call.
            PostMessageSpec myCallSpec = new PostMessageSpec();


            myCallSpec.url = SERVER_URL_PREFIX + "put_local";
            myCallSpec.context = ChatActivity.this;
            // Let's add the parameters.
            HashMap<String, String> m = new HashMap<String, String>();
            m.put("lat", lat);
            m.put("lng", lng);
            m.put("msgid", msgid);
            m.put("msg", msg);
            m.put("dest",getIntentMessage());
            m.put("userid",appInfo.userid);

            myCallSpec.setParams(m);
            // Actual server call.
            if (uploader != null) {
                // There was already an upload in progress.
                uploader.cancel(true);
            }
            uploader = new ServerCall();
            uploader.execute(myCallSpec);
        }
        else{
            showToast("Please write something");
        }
    }

    public void clickRefresh(View v) {
        showLoadingDialog();
        //get location
        String lat;
        String lng;
        //in case of no location, just toast!
        try {
            lat = Double.toString(lastLocation.getLatitude());
            lng = Double.toString(lastLocation.getLongitude());
        }catch(Exception e){
            if(hasRefreshed) {
                dismissLoadingDialog();
                showToast("Unable to get location");
            }
            return;
        }
        // Then, we start the call.
        PostMessageSpec myCallSpec = new PostMessageSpec();
        myCallSpec.url = SERVER_URL_PREFIX + "get_local";
        myCallSpec.context = ChatActivity.this;
        // Let's add the parameters.
        HashMap<String,String> m = new HashMap<String,String>();
        m.put("lat", lat);
        m.put("lng", lng);
        m.put("userid",appInfo.userid);
        m.put("dest",getIntentMessage());
        myCallSpec.setParams(m);
        // Actual server call.
        if (uploader != null) {
            // There was already an upload in progress.
            uploader.cancel(true);
        }
        uploader = new ServerCall();
        uploader.execute(myCallSpec);
    }

    //displays a short toast
    private void showToast(String s) {
        Context ct = getApplicationContext();
        CharSequence text = s;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(ct, text, duration);
        toast.show();
    }


    String randomString(final int length) {
        char[] chars = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }


    /**
     * This class is used to do the HTTP call, and it specifies how to use the result.
     */
    class PostMessageSpec extends ServerCallSpec {
        @Override
        public void useResult(Context context, String result) {
            if (result == null) {
                // Do something here, e.g. tell the user that the server cannot be contacted.
                Log.i(LOG_TAG, "The server call failed.");
                showToast("The server could not be reached");
            } else {
                // Translates the string result, decoding the Json.
                EditText et = (EditText) findViewById(R.id.editText);
                et.setText("");
                Log.i(LOG_TAG, "Received string: " + result);
                displayResult(result);
                // Stores in the settings the last messages received.
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(PREF_POSTS, result);
                editor.commit();

            }
            if(hasRefreshed)
                dismissLoadingDialog();
        }
    }


    private void displayResult(String result) {
        Gson gson = new Gson();
        MessageList ml = gson.fromJson(result, MessageList.class);
        // Fills aList, so we can fill the listView.
        aList.clear();
        for (int i = 0; i < ml.messages.length; i++) {
            Message message = ml.messages[i];
            ListElement ael = new ListElement();
            String formattedDate = getRelevantTimeDiff(ml.messages[i].ts);
            ael.textLabel = message.msg;
            ael.timeText = formattedDate;
            ael.messageID = message.msgid;
            ael.timeStamp = message.ts;
            aList.add(ael);
        }
        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(aa);
        aa.notifyDataSetChanged();
    }
    //parse timestamp string and return the relevant time difference (1d, 1h, 30 min, etc)
    private String getRelevantTimeDiff(String ts){
        Date timestampDate;
        DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
        targetFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formattedDate = "error";
        try { //attempt to parse date
            timestampDate = targetFormat.parse(ts);
            //formattedDate = targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return formattedDate;
        }
        // The server seems to be sending us localized timestamps instead of UTC, so I add 7h
        Date now = new Date();
        //The server gives us a localized timestamp instead of UTC, so I subtract 7h
        Date msg =  timestampDate;//new Date(timestampDate.getTime() - 7*3600*1000); // date of message


        long diffInSeconds = (now.getTime() - msg.getTime()) / 1000;
        //return String.format("%d",diffInSeconds);
        long diff[] = new long[] { 0, 0, 0, 0 };
        // sec
        diff[3] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        // min
        diff[2] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        // hours
        diff[1] = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
        // days
        diff[0] = (diffInSeconds = (diffInSeconds / 24));
        if(diff[0] > 0)         //only return days if its been over 24h
            return diff[0]+"d";
        else if(diff[1] > 0)    //only return hours if its been over 1h
            return diff[1]+"h";
        else if(diff[2] > 0)    //only return minutes if its been over 1m
            return diff[2]+"m";
        else if(diff[3] > 0)    //only return seconds if its been under 1m
            return diff[3]+"s";
        else
            return "now";
    }

}
