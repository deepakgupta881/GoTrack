package com.android.gotrack;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.MenuItem;


public class Session {
    private SharedPreferences mPreferences;
    private Context mContext;
    private static GpsPermissionListener sGpsPermissionListener;

    public Session(Context context) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }
    public static void setsGpsPermissionListener(GpsPermissionListener listener) {
        if (listener != null)
            sGpsPermissionListener = listener;
    }

    static void getGpsPermissionListener() {
        if (sGpsPermissionListener != null) {
            sGpsPermissionListener.onGranted();
        }
    }
}
