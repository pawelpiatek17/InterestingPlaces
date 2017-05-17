package com.example.pawe.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.support.v4.util.Pair;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private GestureDetectorCompat gestureDetector;
    private DbHelper dbHelper;
    private int imgIndex;
    private ArrayList<Pair<Integer,String>> listOfImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_home);
        ImageView imageViewSlideshow = (ImageView)findViewById(R.id.activity_home_iv_slideshow);
        ImageView imageViewMap = (ImageView) findViewById(R.id.activity_home_iv_map);
        gestureDetector = new GestureDetectorCompat(this,new CustomGestureListener());
        imageViewSlideshow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("asa","asda" + gestureDetector.onTouchEvent(event));
                return true;
            }
        });
        listOfImg = new ArrayList<>();
        dbHelper = new DbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                DatabaseContract.DatabaseImages.COLUMN_NAME_IMAGES_IMG_KEY,
                DatabaseContract.DatabaseImages.COLUMN_NAME_IMG_NAME};
        Cursor cursor = db.query(
                DatabaseContract.DatabaseImages.TABLE_NAME_IMAGES,
                projection,
                null,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();
        while (cursor.moveToNext()){
            listOfImg.add(new Pair<Integer,String>(
                    cursor.getInt(cursor.getColumnIndex(DatabaseContract.DatabaseImages.COLUMN_NAME_IMAGES_IMG_KEY)),
                    cursor.getString(cursor.getColumnIndex(DatabaseContract.DatabaseImages.COLUMN_NAME_IMG_NAME))));
        }
        Glide.with(this).load(getResources().getIdentifier(listOfImg.get(0).second,"drawable",getPackageName())).into(imageViewSlideshow);
        Glide.with(this).load(R.drawable.map).into(imageViewMap);
        imgIndex = 0;
    }

    public void activity_home_iv_list_on_click(View view) {
        Intent intent = new Intent(this,ListActivity.class);
        startActivity(intent);
    }

    public void activity_home_iv_map_on_click(View view) {
        Intent intent = new Intent(this,MapsActivity.class);
        startActivity(intent);
    }

    public void activity_home_iv_slideshow(View view) {

    }
    private class CustomGestureListener extends GestureDetector.SimpleOnGestureListener {

        private final String TAG = "CustomGestureListener";

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG,"onDown: " + e.toString());
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "onFling: " + e1.toString()+e2.toString());
            float firstX = e1.getX();
            float secondX = e1.getY();
            if (firstX > secondX) {
                Log.d(TAG, "onFling: LEFT");
            } else if (firstX < secondX){
                Log.d(TAG, "onFling: RIGHT");
            }
            return true;
        }
    }

}
