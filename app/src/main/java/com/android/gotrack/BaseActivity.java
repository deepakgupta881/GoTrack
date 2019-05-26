package com.android.gotrack;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.android.gotrack.myapplication.R;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    private static final int CODE_REQUEST_PERMISSION = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        AppController.mActivity=this;
        setStatusBarColor();
        ButterKnife.bind(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        //Called when the activity is first created.
        afterCreate(savedInstanceState);
    }

    protected abstract int getLayoutId();

    protected abstract void afterCreate(Bundle savedInstanceState);

    @SuppressLint("InflateParams")
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        View view = getLayoutInflater().inflate(R.layout.activity_base, null);
        super.setContentView(view);
        initDefaultView(layoutResID);
    }

    /**
     * Initialize the default layout View
     *
     * @param layoutResId The layout of the child view id
     */
    private void initDefaultView(int layoutResId) {
        FrameLayout container = findViewById(R.id.fl_activity_child_container);
        View childView = LayoutInflater.from(this).inflate(layoutResId, null);
        container.addView(childView, 0);
    }


    /**
     * @param calledActivity Target Activity
     */
    protected void openActivity(Class<?> calledActivity) {
        startActivity(new Intent(this, calledActivity));
        overridePendingTransitionEnter();
    }

    /**
     * Jump interface
     *
     * @param calledActivity Target Activity
     * @param bundle         data
     */
    protected void readyGo(Class<?> calledActivity, Bundle bundle) {
        Intent intent = new Intent(this, calledActivity);
        if (null != bundle)
            intent.putExtras(bundle);
        startActivity(intent);
        overridePendingTransitionEnter();
    }


    protected void openActivityWithClearStack(Class<?> calledActivity) {
        startActivity(new
                Intent(this, calledActivity).
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
        overridePendingTransitionEnter();
    }

    /**
     * startActivityForResult
     *
     * @param cls         Target Activity
     * @param requestCode Send a judgment value
     */
    protected void readyGoForResult(Class<?> cls, int requestCode) {
        Intent intent = new Intent(this, cls);
        startActivityForResult(intent, requestCode);
    }

    public void setStatusBarColor() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.parseColor("#09203f"));
        }
    }

    /**
     * startActivityForResult with bundle
     *
     * @param cls         Target Activity
     * @param requestCode Send a judgment value
     * @param bundle      data
     */
    protected void readyGoForResult(Class<?> cls, int requestCode, Bundle bundle) {
        Intent intent = new Intent(this, cls);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
    }

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    /**
     * finish and performing the "Exit" animation.
     */
    protected void finishActivity() {
        this.finish();
        overridePendingTransitionExit();
    }


}
