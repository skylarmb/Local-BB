package com.dealfaro.luca.clicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by root on 5/6/15.
 */
public class AppInfo {

    public static final String PREF_USERID = "skylarmb";

    private static AppInfo instance = null;

    protected AppInfo() {
        // Exists only to defeat instantiation.
    }

    // Here are some values we want to keep global.
    public String userid;

    public static AppInfo getInstance(Context context) {
        if (instance == null) {
            instance = new AppInfo();
            // Creates a userid, if I don't have one.
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            instance.userid = settings.getString(PREF_USERID, null);
            if (instance.userid == null) {
                // We need to create a userid.
                SecureRandom random = new SecureRandom();
                instance.userid = new BigInteger(130, random).toString(32);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(PREF_USERID, instance.userid);
                editor.commit();
            }
        }
        return instance;
    }
}

