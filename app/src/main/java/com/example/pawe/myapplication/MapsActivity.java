package com.example.pawe.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pawe.myapplication.DatabaseContract.DatabasePlace;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,LocationListener{

    private GoogleMap mMap;
    private LatLngBounds lodz;
    private boolean locationPermissionOk = false;
    public GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final static int REQUEST_CHECK_SETTINGS_CODE = 1;
    private final static int REQUEST_LOCATION_PERMISSION_CODE = 1;
    private boolean askForLocalizationPermission;
    private boolean mRequestingLocationUpdates;
    private boolean locationUpdatesOn = false;
    private Location mCurrentLocation;
    private Marker currentLocationMarker;
    SlidingUpPanelLayout slidingLayout;
    public DbHelper mDbHelper;
    private final String SAVED_STATE_BOOLEAN = "saved_boolean";
    private final String SAVED_STATE_MARKER_HASHMAP = "marker_hashmap";
    private final String SAVED_STATE_PANEL_STATE = "panel_state";
    public final static String MY_LOCATION_EXTRA_MESSAGE = "location_extra";
    private boolean restore;
    private HashMap<String,String> clickedMarkerHashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mDbHelper = new DbHelper(MapsActivity.this);
        if(mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        Log.d("camera", String.valueOf(lodz));
        slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        slidingLayout.setDragView(R.id.linear_layout);
        slidingLayout.setClickable(true);
        mRequestingLocationUpdates = false;
        if(savedInstanceState != null) {

           /** bylo -> boolean[] boolarr= new boolean[2];
            boolarr = savedInstanceState.getBooleanArray(SAVED_STATE_BOOLEAN);**/
            boolean[] boolarr = savedInstanceState.getBooleanArray(SAVED_STATE_BOOLEAN);
            if(boolarr[0] && boolarr[1]){
                mRequestingLocationUpdates = boolarr[0];
                locationUpdatesOn = false;
            }
            restore = true;
            if(savedInstanceState.getSerializable(SAVED_STATE_PANEL_STATE) != null) {
               Log.e("onCreate","savedInstanceState panel state");
                clickedMarkerHashMap = (HashMap<String, String>) savedInstanceState.getSerializable(SAVED_STATE_MARKER_HASHMAP);
                if (savedInstanceState.getSerializable(SAVED_STATE_PANEL_STATE) == SlidingUpPanelLayout.PanelState.DRAGGING) {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
                else {
                    slidingLayout.setPanelState((SlidingUpPanelLayout.PanelState) savedInstanceState.getSerializable(SAVED_STATE_PANEL_STATE));
                }
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        if(ContextCompat.checkSelfPermission(this,"android.permission.ACCESS_FINE_LOCATION")
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionOk = true;
        }
        else
        {
            askForLocalizationPermission = true;
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mGoogleApiClient.isConnected())
        {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mGoogleApiClient.isConnected() && mRequestingLocationUpdates && !locationUpdatesOn)
        {
            startLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbHelper.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        boolean[] boolarr= new boolean[2];
        boolarr[0] = mRequestingLocationUpdates;
        boolarr[1] = locationUpdatesOn;
        outState.putBooleanArray(SAVED_STATE_BOOLEAN,boolarr);
        if(slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED ||
                slidingLayout. getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED ||
                slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED ||
                slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.DRAGGING) {
            outState.putSerializable(SAVED_STATE_MARKER_HASHMAP,clickedMarkerHashMap);
            outState.putSerializable(SAVED_STATE_PANEL_STATE,slidingLayout.getPanelState());
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    /**MAP METHODS*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.getTag().toString() == "myLoc") {
                    return false;
                }
                final String d = "drawable";
                clickedMarkerHashMap = (HashMap<String,String>) marker.getTag();
                String name = clickedMarkerHashMap.get(DatabasePlace.COLUMN_NAME_NAME);
                Log.e("markerClick",name);
                String address = clickedMarkerHashMap.get(DatabasePlace.COLUMN_NAME_ADDRESS);
                Log.e("markerClick",address);
                String description = clickedMarkerHashMap.get(DatabasePlace.COLUMN_NAME_DESCRIPTION);
                Log.e("markerClick",description);
                String imgName = clickedMarkerHashMap.get(DatabasePlace.COLUMN_NAME_IMG_NAME);
                Log.e("markerClick",imgName);
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                ImageView iv = (ImageView)findViewById(R.id.sliding_layout_image);
                TextView tvName = (TextView) findViewById(R.id.sliding_layout_name);
                TextView tvAddress = (TextView) findViewById(R.id.sliding_layout_address);
                TextView tvDescription = (TextView) findViewById(R.id.sliding_layout_description);
                tvName.setText(name);
                tvAddress.setText(address);
                tvDescription.setText(description);
                Glide.with(MapsActivity.this).load(getResources().getIdentifier(imgName,d,getPackageName())).into(iv);
                return false;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                clickedMarkerHashMap = null;

            }
        });
        setMapOptions();
        addMarkers();
        if (restore && clickedMarkerHashMap != null) {
            Log.e("pre","setSlidingPanelContentAfrerRestore");
            setSlidingPanelContentAfterRestore();
        }
    }
    private void setSlidingPanelContentAfterRestore() {
        final String d = "drawable";
        String name = clickedMarkerHashMap.get(DatabasePlace.COLUMN_NAME_NAME);
        Log.e("markerClick",name);
        String address = clickedMarkerHashMap.get(DatabasePlace.COLUMN_NAME_ADDRESS);
        Log.e("markerClick",address);
        String description = clickedMarkerHashMap.get(DatabasePlace.COLUMN_NAME_DESCRIPTION);
        Log.e("markerClick",description);
        String imgName = clickedMarkerHashMap.get(DatabasePlace.COLUMN_NAME_IMG_NAME);
        Log.e("markerClick",imgName);
        ImageView iv = (ImageView)findViewById(R.id.sliding_layout_image);
        TextView tvName = (TextView) findViewById(R.id.sliding_layout_name);
        TextView tvAddress = (TextView) findViewById(R.id.sliding_layout_address);
        TextView tvDescription = (TextView) findViewById(R.id.sliding_layout_description);
        tvName.setText(name);
        tvAddress.setText(address);
        tvDescription.setText(description);
        Glide.with(MapsActivity.this).load(getResources().getIdentifier(imgName,d,getPackageName())).into(iv);
    }
    private void setMapOptions(){
        lodz = new LatLngBounds.Builder()
                .include(new LatLng(51.806862, 19.321633))
                .include(new LatLng(51.858473, 19.504835))
                .include(new LatLng(51.754398, 19.637833))
                .include(new LatLng(51.687828, 19.467888))
                .build();
        mMap.setLatLngBoundsForCameraTarget(lodz);
        mMap.setMinZoomPreference(10.644797f);
    }
    private void addMarkers(){
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                DatabasePlace._ID,
                DatabasePlace.COLUMN_NAME_NAME,
                DatabasePlace.COLUMN_NAME_ADDRESS,
                DatabasePlace.COLUMN_NAME_DESCRIPTION,
                DatabasePlace.COLUMN_NAME_IMG_NAME,
                DatabasePlace.COLUMN_NAME_LATITUDE,
                DatabasePlace.COLUMN_NAME_LONGITUDE
        };
        Cursor cursor = db.query(
                DatabasePlace.TABLE_NAME_PLACES,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        while (cursor.moveToNext()) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(Double.parseDouble(cursor.getString(
                    cursor.getColumnIndex(DatabasePlace.COLUMN_NAME_LATITUDE))),
                    Double.parseDouble(cursor.getString(cursor.getColumnIndex(
                            DatabasePlace.COLUMN_NAME_LONGITUDE)))));
            Marker marker = mMap.addMarker(markerOptions);
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put(DatabasePlace.COLUMN_NAME_NAME,
                    cursor.getString(cursor.getColumnIndex(DatabasePlace.COLUMN_NAME_NAME)));
            hashMap.put(DatabasePlace.COLUMN_NAME_DESCRIPTION,
                    cursor.getString(cursor.getColumnIndex(DatabasePlace.COLUMN_NAME_DESCRIPTION)));
            hashMap.put(DatabasePlace.COLUMN_NAME_ADDRESS,
                    cursor.getString(cursor.getColumnIndex(DatabasePlace.COLUMN_NAME_ADDRESS)));
            hashMap.put(DatabasePlace.COLUMN_NAME_IMG_NAME,
                    cursor.getString(cursor.getColumnIndex(DatabasePlace.COLUMN_NAME_IMG_NAME)));
            hashMap.put(DatabasePlace._ID,
                    cursor.getString(cursor.getColumnIndex(DatabasePlace._ID)));
            marker.setTag(hashMap);
        }
        cursor.close();
    }

    /** LOCATION METHODS*/

    @Override
    public void onConnected(@Nullable Bundle  bundle) {
        if(mGoogleApiClient.isConnected() && mRequestingLocationUpdates && !locationUpdatesOn && restore) {
            showMyLocation(findViewById(R.id.myLocationButton));
        }
        else if(mGoogleApiClient.isConnected() && mRequestingLocationUpdates && !locationUpdatesOn)
        {
            startLocationUpdates();
    }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    protected void checkAndSetLocationSettings(){
        createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        final PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS: {
                        Log.e("tag settings", "location not null");
                        startLocationUpdates();
                        break;
                    }
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED: {
                        //show user dialog to fix
                        try {
                            status.startResolutionForResult(MapsActivity.this,
                                    REQUEST_CHECK_SETTINGS_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            //ignore it
                        }
                        break;
                    }
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE: {
                        //cant change settings. localization unavailable
                        break;
                    }
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case(REQUEST_CHECK_SETTINGS_CODE): {
                if (resultCode == RESULT_OK) {
                    Log.d("activityResult","ok");
                }
                else {
                    Log.e("activityResult","not ok");
                }
                break;
            }
            default:
                break;
        }
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        mRequestingLocationUpdates = true;
        locationUpdatesOn = true;
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        //locationUpdatesOn = false;
    }
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        if(mCurrentLocation != null) {
            putMyLocationMarkerOnMap(mCurrentLocation);
        }
    }
    private void putMyLocationMarkerOnMap(Location location){
        String tag = "myLoc";
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        currentLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_pin_circle_blue_18dp)));
        currentLocationMarker.setTag(tag);
        currentLocationMarker.setTitle("Moja lokalizacja");
    }
    public void showMyLocation(View view) {
        if(askForLocalizationPermission)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION_CODE);
        }
        if(mGoogleApiClient.isConnected() && locationPermissionOk)
        {
            checkAndSetLocationSettings();
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mLastLocation != null )
            {
                if(!locationUpdatesOn){
                    startLocationUpdates();
                    putMyLocationMarkerOnMap(mLastLocation);
                }
                Log.e("tag", "location not null");
                if(!restore) {
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                            .zoom(15)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    restore = false;
                }
            }
            else {
                Toast.makeText(this, getResources().getString(R.string.location_unavailable), Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults){
        switch(requestCode) {
            case REQUEST_LOCATION_PERMISSION_CODE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permissions","loc granted");
                    locationPermissionOk = true;
                }
                else {
                    Log.d("Permissions","loc denied");
                    locationPermissionOk = false;
                }
            }
        }
    }

    public void showListOfPlaces(View view) {
        if (mGoogleApiClient != null && LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient) != null)
        {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Intent intent = new Intent(this,ListActivity.class);
            intent.putExtra(MY_LOCATION_EXTRA_MESSAGE,new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude()));
            startActivity(intent);
        }
        else if(mCurrentLocation != null){
            Intent intent = new Intent(this,ListActivity.class);
            intent.putExtra(MY_LOCATION_EXTRA_MESSAGE,new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()));
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "Location unavailable", Toast.LENGTH_SHORT).show();
        }
    }
}
