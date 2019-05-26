package com.android.gotrack;

import android.app.Activity;
import android.app.Application;

public class AppController extends Application {
    public static Activity mActivity;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
