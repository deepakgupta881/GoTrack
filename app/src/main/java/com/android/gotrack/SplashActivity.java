package com.android.gotrack;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.gotrack.myapplication.R;


public class SplashActivity extends BaseActivity {
    private ImageView mIvSplash;
    private TextView mTxtAppName;
    private RelativeLayout mRelativeLayout;

    private String[] mAskPermissionParams = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"};
    private static final int FINE_LOCATION_REQUEST_CODE = 123;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_splash;
    }


    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        mIvSplash = findViewById(R.id.imageViewSplash);
        mTxtAppName = findViewById(R.id.txtAppName);
        mRelativeLayout = findViewById(R.id.relative);
        startAnimations();

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void startAnimations() {
        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.anim_down);
        Animation translate = AnimationUtils.loadAnimation(this, R.anim.translate);
        rotate.reset();
        translate.reset();
        mRelativeLayout.clearAnimation();
        mIvSplash.startAnimation(rotate);
        mTxtAppName.startAnimation(translate);
        Thread mSplashThread = new Thread() {
            @Override
            public void run() {
                super.run();
                int waited = 0;
                while (waited < 4500) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    waited += 100;
                }

                if (LocationUtils.checkPlayServices(SplashActivity.this)) {
                    finish();
                    openActivity(MainActivity.class);
                }

            }
        };
        mSplashThread.start();
    }


}
