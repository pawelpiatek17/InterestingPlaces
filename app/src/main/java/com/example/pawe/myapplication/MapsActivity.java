package com.example.pawe.myapplication;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks{

    private GoogleMap mMap;
    private LatLngBounds lodz;
    public GoogleApiClient mGoogleApiClient;
    SlidingUpPanelLayout slidingLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if(mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        lodz = new LatLngBounds.Builder()
                .include(new LatLng(51.806862, 19.321633))
                .include(new LatLng(51.858473, 19.504835))
                .include(new LatLng(51.754398, 19.637833))
                .include(new LatLng(51.687828, 19.467888))
                .build();
        Log.d("camera", String.valueOf(lodz));
        slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        slidingLayout.setDragView(R.id.linear_layout);
        slidingLayout.setClickable(true);
    }
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
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
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.e("markerClick","abb");
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                return false;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
        });
        mMap.setLatLngBoundsForCameraTarget(lodz);
        mMap.setMinZoomPreference(10.644797f);
        //mMap.setMinZoomPreference(11);
        LatLng manufaktura = new LatLng(51.779521, 19.446634);
        mMap.addMarker(new MarkerOptions().position(manufaktura).title("Manufaktura"));
}
    @Override
    public void onConnected(@Nullable Bundle  bundle) {
        int permissionCheck = ContextCompat.checkSelfPermission(this,"android.permission.ACCESS_FINE_LOCATION");
        Log.d("tag", String.valueOf(permissionCheck));
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.e("tag", "onConnected: " + String.valueOf(mLastLocation.getLatitude()) + ":" + String.valueOf(mLastLocation.getLongitude()));
        } else {
            Log.e("tag", "onConasdad");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void showMyLocation(View view) {
        if(mGoogleApiClient.isConnected())
        {
            Log.d("con","connectedAPI");
            int permissionCheck = ContextCompat.checkSelfPermission(this,"android.permission.ACCESS_FINE_LOCATION");
            Log.d("con", String.valueOf(permissionCheck));
            if(permissionCheck == PackageManager.PERMISSION_DENIED){
                Log.d("con","request");
                ActivityCompat.requestPermissions(this,
                        new String[]{"android.permission.ACCESS_FINE_LOCATION"},1
                        );
            }
            else {
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    Log.e("tag", "location not null");
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()))
                            .zoom(15)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } else {
                    Toast.makeText(this,"Aby uzyskać aktualną lokalizacje włącz na urządzeniu usługę lokalizacji",Toast.LENGTH_LONG).show();
                    Log.e("tag", "location null");
                }
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults){
        switch(requestCode) {
            case 1:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permissions","loc granted");
                    //noinspection MissingPermission
                    showMyLocation(findViewById(R.id.myLocationButton));
                }
                else {
                    Log.d("Permissions","loc denied");
                }
            }
        }
    }

    public void imageViewInSlidingLayoutClick(View view) {
        Toast.makeText(this, "sdfjahksdffadfa", Toast.LENGTH_SHORT).show();
    }
}
