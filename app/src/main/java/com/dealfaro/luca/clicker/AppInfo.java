package com.dealfaro.luca.clicker;

import android.content.Context;

/**
 * Created by luca on 23/4/2015.
 */
public class AppInfo {
    private static AppInfo instance = null;

    protected AppInfo() {
        // Exists only   to defeat instantiation.
    }
    // Here are some values we want to keep global.
    public String[] messages;

    public static AppInfo getInstance(Context context) {
        if(instance == null) {
            instance = new AppInfo();
            instance.messages = null;
        }
        return instance;
    }

}
