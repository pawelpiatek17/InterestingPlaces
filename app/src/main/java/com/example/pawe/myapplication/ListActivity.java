package com.example.pawe.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.pawe.myapplication.DatabaseContract.DatabasePlace;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class ListActivity extends android.app.ListActivity {
    private final float maxDistanceInMeters = 10000;
    private ListView listView;
    public final static String LOCATION_NAME_EXTRA = "name_extra";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Intent intent = getIntent();
        LatLng latLng = intent.getParcelableExtra(MapsActivity.MY_LOCATION_EXTRA_MESSAGE);
        listView = getListView();
        String[] from = {"name", "distance"};
        int[] to = {R.id.places_listview_item_layout_name,R.id.places_listview_item_layout_distance};
        ArrayList<HashMap<String,String>> arrayList = getDataFromDatabase(latLng,from);
        SimpleAdapter adapter = new SimpleAdapter(this,
                arrayList, R.layout.places_listview_item_layout, from, to);
        Log.e("ListActivity","onCreate po adapter construktor");
        listView.setAdapter(adapter);
        Log.e("ListActivity","onCreate po setAdapter");
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = ((TextView)view.findViewById(R.id.places_listview_item_layout_name))
                        .getText().toString();
                Intent intent1 = new Intent(ListActivity.this,ShowPlaceActivity.class);
                intent1.putExtra(LOCATION_NAME_EXTRA,name);
                startActivity(intent1);
            }
        });
    }
    private ArrayList<HashMap<String,String>> getDataFromDatabase(LatLng myLocation, String[] s) {
        String nameString = s[0];
        String distanceString = s[1];
        ArrayList<HashMap<String,String>> list = new ArrayList<>();
        DbHelper mDbHelper = new DbHelper(ListActivity.this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                DatabasePlace.COLUMN_NAME_NAME,
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
            LatLng latLng = new LatLng(Double.parseDouble(cursor.getString(
                    cursor.getColumnIndex(DatabasePlace.COLUMN_NAME_LATITUDE))),
                    Double.parseDouble(cursor.getString(cursor.getColumnIndex(
                            DatabasePlace.COLUMN_NAME_LONGITUDE))));
            float[] distanceInMeters = new float[1];
            Location.distanceBetween(myLocation.latitude,myLocation.longitude,latLng.latitude,latLng.longitude,distanceInMeters);
            if(distanceInMeters[0] > maxDistanceInMeters)
            {
                continue;
            }
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put(nameString,cursor.getString(
                    cursor.getColumnIndex(DatabasePlace.COLUMN_NAME_NAME)));
            hashMap.put(distanceString, String.valueOf(Math.round(distanceInMeters[0]))+" m");
            list.add(hashMap);
        }
        Collections.sort(list,new CustomHashMapComparator(distanceString));
        Log.e("ListActivity","getDataFromDatabase przed return");
        cursor.close();
        mDbHelper.close();
        return list;
    }
}

class CustomHashMapComparator implements Comparator<HashMap<String,String>> {
    private final String key;

    public CustomHashMapComparator(String key) {
        this.key = key;
    }

    @Override
    public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {
        Float firstValue = Float.parseFloat((o1.get(key)).replace(" m",""));
        Float secondValue = Float.parseFloat(o2.get(key).replace(" m",""));
        return firstValue.compareTo(secondValue);
    }
}
