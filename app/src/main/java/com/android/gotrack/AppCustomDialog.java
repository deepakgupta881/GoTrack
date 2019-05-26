package com.android.gotrack;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import com.android.gotrack.myapplication.R;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;


class AppCustomDialog {

    private Context mContext;
    public static AlertDialog.Builder sBuilder = null;
    static boolean mIsLocationPermissionEnable;


    public AppCustomDialog(Context context) {
        mContext = context;
    }


    @SuppressLint("CheckResult")
    public void requestPermissions(String permissions[]) {
        new RxPermissions((MainActivity) mContext)
                .requestEachCombined(permissions)
                .subscribe((Permission permission) -> { // will emit  Permission objects
                    if (permission.granted) {
                        //set User Current Location
                        Session.getGpsPermissionListener();
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        // Denied permission without ask never again
                        mIsLocationPermissionEnable = true;
                        requestPermissions(permissions);
                    } else {
                        mIsLocationPermissionEnable = true;
                        showAlertDialog();
                        // Denied permission with ask never again
                        // Need to go to the settings
                    }
                });
    }


    private void showAlertDialog() {
        if (sBuilder == null) {
            sBuilder = new AlertDialog.Builder(mContext).setMessage(mContext.getString(R.string.rationale_storage));
            sBuilder.setCancelable(false);
            sBuilder.setPositiveButton(mContext.getString(R.string.settings), (dialog, id) -> {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts(mContext.getString(R.string.txt_package), mContext.getPackageName(), null);
                intent.setData(uri);
                mContext.startActivity(intent);
            }).show();
        }
    }

}
