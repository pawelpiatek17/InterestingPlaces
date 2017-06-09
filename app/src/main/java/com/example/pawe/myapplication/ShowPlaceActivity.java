package com.example.pawe.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.pawe.myapplication.DatabaseContract.DatabasePlace;

import java.util.HashMap;

public class ShowPlaceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_place);
        Intent intent = getIntent();
        String name = intent.getStringExtra(ListActivity.LOCATION_NAME_EXTRA);
        if (name == null) {
            setViewsData(intent.getIntExtra(HomeActivity.IMAGE_KEY_EXTRA,-1));
        } else {
            setViewsData(name);
        }
        TextView tvAddress = (TextView) findViewById(R.id.activity_show_place_address);
        tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(),MapsActivity.class);
                TextView tv = (TextView) findViewById(R.id.activity_show_place_name);
                i.putExtra(DatabasePlace.COLUMN_NAME_NAME,tv.getText());
                startActivity(i);
            }
        });
    }
    private void setViewsData(String name) {
        DbHelper mDbHelper = new DbHelper(ShowPlaceActivity.this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                DatabasePlace.COLUMN_NAME_NAME,
                DatabasePlace.COLUMN_NAME_ADDRESS,
                DatabasePlace.COLUMN_NAME_DESCRIPTION,
                DatabasePlace.COLUMN_NAME_MAIN_IMG_NAME
        };
        String selection = DatabasePlace.COLUMN_NAME_NAME + " = ?";
        String[] selectionArgs ={name};
        Cursor cursor = db.query(
                DatabasePlace.TABLE_NAME_PLACES,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        ImageView iv = (ImageView)findViewById(R.id.activity_show_place_image);
        TextView tvName = (TextView) findViewById(R.id.activity_show_place_name);
        TextView tvAddress = (TextView) findViewById(R.id.activity_show_place_address);
        TextView tvDescription = (TextView) findViewById(R.id.activity_show_place_description);
        tvName.setText(cursor.getString(cursor.getColumnIndex(DatabasePlace.COLUMN_NAME_NAME)));
        tvAddress.setText(cursor.getString(cursor.getColumnIndex(DatabasePlace.COLUMN_NAME_ADDRESS)));
        tvDescription.setText(cursor.getString(cursor.getColumnIndex(DatabasePlace.COLUMN_NAME_DESCRIPTION)));
        Glide.with(this).load(getResources().getIdentifier(cursor.getString(cursor.getColumnIndex(DatabasePlace.COLUMN_NAME_MAIN_IMG_NAME)),"drawable",getPackageName())).into(iv);
        cursor.close();
        mDbHelper.close();
    }
    private void setViewsData(int id) {
        DbHelper mDbHelper = new DbHelper(ShowPlaceActivity.this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                DatabasePlace.COLUMN_NAME_NAME,
                DatabasePlace.COLUMN_NAME_ADDRESS,
                DatabasePlace.COLUMN_NAME_DESCRIPTION,
                DatabasePlace.COLUMN_NAME_MAIN_IMG_NAME
        };
        String selection = DatabasePlace._ID + " = ?";
        String[] selectionArgs ={String.valueOf(id)};
        Cursor cursor = db.query(
                DatabasePlace.TABLE_NAME_PLACES,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        ImageView iv = (ImageView)findViewById(R.id.activity_show_place_image);
        TextView tvName = (TextView) findViewById(R.id.activity_show_place_name);
        TextView tvAddress = (TextView) findViewById(R.id.activity_show_place_address);
        TextView tvDescription = (TextView) findViewById(R.id.activity_show_place_description);
        tvName.setText(cursor.getString(cursor.getColumnIndex(DatabasePlace.COLUMN_NAME_NAME)));
        tvAddress.setText(cursor.getString(cursor.getColumnIndex(DatabasePlace.COLUMN_NAME_ADDRESS)));
        tvDescription.setText(cursor.getString(cursor.getColumnIndex(DatabasePlace.COLUMN_NAME_DESCRIPTION)));
        Glide.with(this).load(getResources().getIdentifier(cursor.getString(cursor.getColumnIndex(DatabasePlace.COLUMN_NAME_MAIN_IMG_NAME)),"drawable",getPackageName())).into(iv);
        cursor.close();
        mDbHelper.close();
    }
}
