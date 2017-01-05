package com.example.pawe.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pawe.myapplication.DatabaseContract.DatabasePlace;

public class ShowPlaceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_place);
        Intent intent = getIntent();
        String name = intent.getStringExtra(ListActivity.LOCATION_NAME_EXTRA);
        setViewsData(name);
    }
    private void setViewsData(String name) {
        DbHelper mDbHelper = new DbHelper(ShowPlaceActivity.this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                DatabasePlace.COLUMN_NAME_NAME,
                DatabasePlace.COLUMN_NAME_ADDRESS,
                DatabasePlace.COLUMN_NAME_DESCRIPTION,
                DatabasePlace.COLUMN_NAME_IMG_NAME
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
        iv.setImageDrawable(getDrawable(getResources().getIdentifier(cursor.getString(cursor.getColumnIndex(DatabasePlace.COLUMN_NAME_IMG_NAME)),"drawable",getPackageName())));
        cursor.close();
        mDbHelper.close();
    }
}
