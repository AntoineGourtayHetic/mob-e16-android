package com.antoinegourtay.mob_e16_android.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.antoinegourtay.mob_e16_android.CryptoPlaceApplication;
import com.antoinegourtay.mob_e16_android.R;
import com.antoinegourtay.mob_e16_android.response.CryptoValueResponse;
import com.antoinegourtay.mob_e16_android.response.PlacesResponse;
import com.antoinegourtay.mob_e16_android.response.SinglePlaceResponse;
import com.fasterxml.jackson.core.JsonParser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.neopixl.spitfire.listener.RequestListener;
import com.neopixl.spitfire.request.BaseRequest;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String APP_NAME = "CryptoPlaces";
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 102;

    private GoogleMap mMap;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private static double latitude;
    private static double longitude;
    private LatLng currentPosition;

    private boolean onLaunch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /**
         * Location management
         */

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        //When user location changes
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mMap.clear();
                Log.d(APP_NAME, "location : " + location);

                //Getting the position from the LocationListener
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                Log.d(APP_NAME, "latitude : " + latitude + " - longitude : " + longitude);

                currentPosition = new LatLng(latitude, longitude);

                //To show the blue dot on current location
                mMap.setMyLocationEnabled(true);

                //Animating the camera to the current position
                if (onLaunch) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 15));
                    onLaunch = false;
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                provider.toString();
            }

            @Override
            public void onProviderEnabled(String provider) {
                provider.toString();
            }

            @Override
            public void onProviderDisabled(String provider) {
                provider.toString();
            }
        };

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            }
        } else {
            long minTime = 10;
            float minDistance = 10.0f;

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);

        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Request to get all the places from the API
        BaseRequest<PlacesResponse> request =
                new BaseRequest.Builder<>(Request.Method.GET
                        , "https://api.myjson.com/bins/wfh57"
                        , PlacesResponse.class)
                        .listener(new RequestListener<PlacesResponse>() {
                            @Override
                            public void onSuccess(Request request, NetworkResponse response, PlacesResponse result) {
                                Toast.makeText(MapsActivity.this, "OK", Toast.LENGTH_SHORT).show();

                                for (SinglePlaceResponse singlePlaceResponse : result.getResults()) {
                                    LatLng markerLatLng = new LatLng(
                                            Double.parseDouble(singlePlaceResponse.getPosition().getLatitude()),
                                            Double.parseDouble(singlePlaceResponse.getPosition().getLongitude())
                                    );

                                    mMap.addMarker(
                                            new MarkerOptions()
                                                    .title(singlePlaceResponse.getName())
                                                    .position(markerLatLng)
                                    );
                                }
                            }

                            @Override
                            public void onFailure(Request request, NetworkResponse response, VolleyError error) {
                                Toast.makeText(MapsActivity.this, "FAIL", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .build();


        CryptoPlaceApplication cryptoPlaceApplication =
                (CryptoPlaceApplication) getApplication();
        cryptoPlaceApplication.getRequestQueue().add(request);
    }
}
