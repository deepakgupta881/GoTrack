
package com.android.gotrack;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;


import com.android.gotrack.myapplication.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocationUtils {

    private static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     *
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     *
     * @param location The {@link Location}.
     */
    public static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    public static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }

    public static void animateMarker(final GoogleMap map, final Marker markerAnimate, final LatLng toPosition) {
        try {
            LatLng startPosition = markerAnimate.getPosition();
            Projection proj = map.getProjection();
            Log.d(LocationUtils.class.getName(),"aa gya animate fn m");
            Point startPoint = proj.toScreenLocation(markerAnimate.getPosition());
            final LatLng startLatLng = proj.fromScreenLocation(startPoint);
            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
            boolean check = checkDistanceBetweenLoc(startLatLng, toPosition);
            if (check) {

                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                valueAnimator.setDuration(3000);
                valueAnimator.setInterpolator(new LinearInterpolator());
                valueAnimator.addUpdateListener(valueAnimator1 -> {
                    float v = valueAnimator1.getAnimatedFraction();
                    LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, toPosition);
                    markerAnimate.setPosition(newPosition);
                    markerAnimate.setAnchor(0.5f, 0.5f);
                    markerAnimate.setRotation(computeRotation(v, markerAnimate.getRotation(), getBearing(startLatLng, toPosition)));
                });
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        Log.d(LocationUtils.class.getName(),"end ani");

                    }
                });
                valueAnimator.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean checkDistanceBetweenLoc(LatLng startLatLng, LatLng toPosition) {
        boolean check;
        Location startLocation = new Location("");
        startLocation.setLatitude(startLatLng.latitude);
        startLocation.setLongitude(startLatLng.longitude);
        Location endLocation = new Location("");
        endLocation.setLatitude(toPosition.latitude);
        endLocation.setLongitude(toPosition.longitude);
        float distance = startLocation.distanceTo(endLocation);
        check = distance > 3;
        return check;
    }


    private static float computeRotation(float fraction, float start, float end) {
        float normalizeEnd = end - start; // rotate start to 0
        float normalizedEndAbs = (normalizeEnd + 360) % 360;
        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
        float rotation;
        if (direction > 0) {
            rotation = normalizedEndAbs;
        } else {
            rotation = normalizedEndAbs - 360;
        }
        float result = fraction * rotation + start;
        return (result + 360) % 360;
    }

    private static float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);
        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }


    public static void openAutocompleteActivityAddress(Activity mActivity) {
        if (mActivity != null)
            try {
                // The autocomplete activity requires Google Play Services to be available. The intent
                // builder checks this and throws an exception if it is not the case.
                AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                        .setTypeFilter(Place.TYPE_COUNTRY).setCountry("IN")
                        .build();
                Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                        .setFilter(autocompleteFilter)
                        .build(mActivity);
                int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
                mActivity.startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
            } catch (GooglePlayServicesRepairableException e) {
                // Indicates that Google Play Services is either not installed or not up to date. Prompt
                // the user to correct the issue.
         /*   GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0 / requestCode /).show();*/
            } catch (GooglePlayServicesNotAvailableException e) {
                // Indicates that Google Play Services is not available and the problem is not easily
                // resolvable.
                String message = mActivity.getString(R.string.google_service_not_avail) +
                        GoogleApiAvailability.getInstance().getErrorString(e.errorCode);
                Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
            }
    }

    public static boolean checkPlayServices(Activity activity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, 12).show();
            } else {
                Log.i("Error", "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    public static String getGeoState(Activity mActivity, LatLng location) {
        Geocoder geocoder;
        List<Address> addresses;
        String stateName = null;
        geocoder = new Geocoder(mActivity, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = null;
            if (addresses.get(0).getAddressLine(0) == null || addresses.get(0).getAddressLine(0).isEmpty()) {
                address = "-NA-";
                //return address not found
            } else {
                stateName = addresses.get(0).getAdminArea();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return stateName;
    }



    /**
     * @param context     pass current activity context
     * @param vectorResId vector image marker on google map
     * @return return bitmap
     */
    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable != null) {
            vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
            Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }
        return null;
    }


    /**
     * @param location location passed in string
     * @return LatLng
     */
    public static LatLng convertStringToLocation(String location) {
        String loc[] = location.split(",");
        return new LatLng(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
    }

    /**
     * @param map     animate google map camera within latlng bounds
     * @param context current activity context
     * @param srcLoc  src latlng
     * @param destLoc dest latlng
     */
    public static void animateGoogleCameraWithinLatlngBounds(Context context, GoogleMap map, LatLng srcLoc, LatLng destLoc) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(srcLoc);
        builder.include(destLoc);
        LatLngBounds bounds = builder.build();
        final int width = context.getResources().getDisplayMetrics().widthPixels;
        final int height = context.getResources().getDisplayMetrics().heightPixels;
        final int minMetric = Math.min(width, height / 2);
        final int padding = (int) (minMetric * 0.25);
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height / 2, padding);
        map.animateCamera(cu);
    }
    public static String getCompleteAddressString(Context context, double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        String address = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null || addresses.size() != 0) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");
                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i));
                }
                strAdd = strReturnedAddress.toString();
             /*    if (strAdd.contains(",")) {
                    String splitState[] = strAdd.split(",");
                    address = splitState[0] + splitState[1] + "," + returnedAddress.getSubLocality();
                } else {
                    address = strAdd.toString() + "," + returnedAddress.getSubLocality();
                }*/
                if (strAdd.contains(",")) {
                    String splitState[] = strAdd.split(",");
                    int size = splitState.length;
                    if (size == 2 || size == 3) {
                        address = address + splitState[0].toString();
                    } else {
                        for (int count = 0; count < size - 3; count++) {
                            address = address + splitState[count] + ",";
                        }
                        if (address.length() != 0) {
                            address = address.substring(0, address.length() - 1);
                        }
                    }
                } else {
                    address = strAdd.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return address;
    }
    /**
     * @param context pass current activity context
     * @param logo    png image marker on google map
     * @return return bitmap
     */
    public static BitmapDescriptor getMapIcon(Context context, int logo) {
        Bitmap smallMarker = null;
        try {
            final float scale = context.getResources().getDisplayMetrics().density;
            int pixels = (int) (30 * scale + 0.5f);
            BitmapDrawable bitmapdraw = (BitmapDrawable) context.getResources().getDrawable(logo);
            smallMarker = Bitmap.createScaledBitmap(bitmapdraw.getBitmap(), pixels, pixels, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BitmapDescriptorFactory.fromBitmap(smallMarker);
    }

    public static String convertLocationToString(Double lat, Double lng) {
        return lat + "," + lng;
    }
}
