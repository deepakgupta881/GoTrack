package com.android.gotrack;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.gotrack.myapplication.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends BaseActivity implements OnMapReadyCallback, GpsPermissionListener {
    private AppCustomDialog mAppCustomDialog;
    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;
    // Tracks the bound state of the service.
    private boolean mBound = false;
    private GoogleMap mMap;
    private BroadcastReceiver myReceiver;
    //dest temp
    LatLng destlatLng, sourceLatLng;
    private Marker mCarMarker;

    private boolean mIsMarkerVisible;
    boolean isButtonClick;
    TextView tvSrc, tvDest;
    private boolean mIsDropLocationScreenOpenCheck;
private boolean mGetFirstLocation;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        myReceiver = new MyReceiver();
//      destlatLng = new LatLng(30.741482, 76.768066);
        Session.setsGpsPermissionListener(this);
        mAppCustomDialog = new AppCustomDialog(MainActivity.this);
        tvSrc = findViewById(R.id.et_src);
        tvDest = findViewById(R.id.et_dest);

        try {
            SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            if (supportMapFragment != null) {
                supportMapFragment.getMapAsync(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
        if (!AppCustomDialog.mIsLocationPermissionEnable) {
            mAppCustomDialog.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(false);
    }

    @Override
    public void onGranted() {
        new Handler().postDelayed(this::startLocation, 1000);

    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
    }

    public void startLocation() {
        mService.requestLocationUpdates();
    }

    public void stopLocation() {
        mService.removeLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mIsMarkerVisible = false;
        stopLocation();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        if (AppCustomDialog.sBuilder != null) AppCustomDialog.sBuilder = null;
        AppCustomDialog.mIsLocationPermissionEnable = false;
    }

    public void go(View view) {
/*
        if (destlatLng != null) {
            if (!isButtonClick) {
                startAnimation();
                isButtonClick = true;
                if (destlatLng != null)
                    mDestMarker = mMap.addMarker(new MarkerOptions()
                            .position(destlatLng)
                            .icon(LocationUtils.bitmapDescriptorFromVector(this, R.drawable.ic_flag)));
            }
        } else
            Toast.makeText(this, "please select your destination", Toast.LENGTH_SHORT).show();
*/

    }


    private void startAnimation() {
        if (!mIsMarkerVisible) {
//              animateMapCamera();
                mIsMarkerVisible = true;
        } else {
            LocationUtils.animateGoogleCameraWithinLatlngBounds(this, mMap, sourceLatLng, destlatLng);
            runOnUiThread(() -> new Handler().postDelayed(() -> LocationUtils.animateMarker(mMap, mCarMarker, sourceLatLng), 500));
        }
    }

    private void animateMapCamera() {
        float markerZoom = 15.0f;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sourceLatLng, markerZoom));
    }

    public void autocompleteSearch(View view) {
        if (!mIsDropLocationScreenOpenCheck) {
            mIsDropLocationScreenOpenCheck = true;
            LocationUtils.openAutocompleteActivityAddress(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check that the result was from the autocomplete widget.
        int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);
                setData(place);

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e(MainActivity.class.getName(), "Autocomplete place filter error" + status.getStatusCode());
            } else if (resultCode == RESULT_CANCELED) {
                // Indicates that the activity closed before a selection was made. For example if
                // the user pressed the back button.
                Log.e(MainActivity.class.getName(), "back pressed");

            }
            mIsDropLocationScreenOpenCheck = false;
        }
    }

    @SuppressLint("MissingPermission")
    private void setSourceMarker() {
        if (sourceLatLng != null) {
            mMap.setMyLocationEnabled(false);
            mCarMarker = mMap.addMarker(new MarkerOptions()
                    .position(sourceLatLng)
                    .icon(LocationUtils.bitmapDescriptorFromVector(this, R.drawable.ic_airplane)));
        } else mMap.setMyLocationEnabled(true);
    }

    private void setDestinationMarker() {
        if (destlatLng != null)
            mMap.addMarker(new MarkerOptions()
                    .position(destlatLng)
                    .icon(LocationUtils.bitmapDescriptorFromVector(this, R.drawable.ic_flag)));
    }

    private void setData(Place place) {
        destlatLng = place.getLatLng();
        tvDest.setText(place.getAddress());
        mMap.clear();
        isButtonClick = true;
        setSourceMarker();
        setDestinationMarker();
        new Handler().postDelayed(this::startAnimation, 1000);
    }

    public void centerLocation(View view) {
        if (sourceLatLng != null && destlatLng != null) {
            LocationUtils.animateGoogleCameraWithinLatlngBounds(this, mMap, sourceLatLng, destlatLng);
        } else {
            if (sourceLatLng != null) {
                animateMapCamera();
            }
        }
    }

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION) != null) {
                Location mLocation = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
                sourceLatLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                tvSrc.setText(LocationUtils.getCompleteAddressString(MainActivity.this, mLocation.getLatitude(), mLocation.getLongitude()));


                if (!mGetFirstLocation){
                    setSourceMarker();
                    mGetFirstLocation=true;
                }

                if (isButtonClick)
                    startAnimation();
            }
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };
}

