package com.example.pawe.myapplication;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
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

    static final String IMAGE_KEY_EXTRA = "image_key_extra";
    private GestureDetectorCompat gestureDetector;
    private int imgListIndex;
    private ArrayList<Pair<Integer,String>> listOfImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_home);
        ImageView imageViewSlideshow = (ImageView)findViewById(R.id.activity_home_iv_slideshow);
        ImageView imageViewMap = (ImageView) findViewById(R.id.activity_home_iv_map);
        ImageView imageViewLogo = (ImageView) findViewById(R.id.activity_home_iv_logo) ;
        ImageView imageViewList = (ImageView) findViewById(R.id.activity_home_iv_list) ;
        gestureDetector = new GestureDetectorCompat(this,new CustomGestureListener());
        imageViewSlideshow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("asa","asda" + gestureDetector.onTouchEvent(event));
                return true;
            }
        });
        listOfImg = new ArrayList<>();
        Glide.with(HomeActivity.this).load(R.drawable.map).centerCrop().into(imageViewMap);
        Glide.with(HomeActivity.this).load(R.drawable.list).into(imageViewList);
        new RetrieveImagesFromDatabaseTask().execute();
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
            super.onDown(e);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //Log.d(TAG, "onFling: " + e1.toString()+e2.toString());
            float firstX = e1.getX();
            float secondX = e2.getX();
            ImageView imageViewSlideshow = (ImageView)findViewById(R.id.activity_home_iv_slideshow);
            if (firstX > secondX) {
                Log.d(TAG, "onFling: RIGHT");
                changeListIndex(false);
            } else if (firstX < secondX){
                Log.d(TAG, "onFling: LEFT");
                changeListIndex(true);
            }
            Glide.with(HomeActivity.this)
                    .load(getResources().getIdentifier(listOfImg.get(imgListIndex).second,"drawable",getPackageName()))
                    .into(imageViewSlideshow);
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Intent intent = new Intent(HomeActivity.this,ShowPlaceActivity.class);
            intent.putExtra(IMAGE_KEY_EXTRA,listOfImg.get(imgListIndex).first);
            startActivity(intent);
            return true;
        }

        private void changeListIndex(boolean b) {
            if (b) {
                imgListIndex--;
            } else {
                imgListIndex++;
            }
            if (imgListIndex == -1) {
                imgListIndex = listOfImg.size()-1;
            }
            else if (imgListIndex == listOfImg.size()) {
                imgListIndex = 0;
            }
        }
    }

    private class RetrieveImagesFromDatabaseTask extends AsyncTask<Void,Void,ArrayList<Pair<Integer,String>>> {
        @Override
        protected ArrayList<Pair<Integer, String>> doInBackground(Void... params) {
            DbHelper dbHelper;
            dbHelper = new DbHelper(getBaseContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String[] projection = {
                    DatabaseContract.DatabasePlace._ID,
                    DatabaseContract.DatabasePlace.COLUMN_NAME_MAIN_IMG_NAME};
            Cursor cursor = db.query(
                    DatabaseContract.DatabasePlace.TABLE_NAME_PLACES,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    null);
            ArrayList<Pair<Integer,String>> list = new ArrayList<>(cursor.getCount()+1);
            cursor.moveToFirst();
            while (cursor.moveToNext()){
                list.add(new Pair<Integer,String>(
                        cursor.getInt(cursor.getColumnIndex(DatabaseContract.DatabasePlace._ID)),
                        cursor.getString(cursor.getColumnIndex(DatabaseContract.DatabasePlace.COLUMN_NAME_MAIN_IMG_NAME))));
            }
            cursor.close();
            db = dbHelper.getReadableDatabase();
            projection = new String[]{
                    DatabaseContract.DatabaseImages.COLUMN_NAME_IMAGES_IMG_KEY,
                    DatabaseContract.DatabaseImages.COLUMN_NAME_IMG_NAME};
            cursor = db.query(
                    DatabaseContract.DatabaseImages.TABLE_NAME_IMAGES,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    null);
            while (cursor.moveToNext()){
                list.add(new Pair<Integer,String>(
                        cursor.getInt(cursor.getColumnIndex(DatabaseContract.DatabaseImages.COLUMN_NAME_IMAGES_IMG_KEY)),
                        cursor.getString(cursor.getColumnIndex(DatabaseContract.DatabaseImages.COLUMN_NAME_IMG_NAME))));
            }
            cursor.close();
            db.close();
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<Pair<Integer, String>> pairs) {
            ImageView imageViewSlideshow = (ImageView)findViewById(R.id.activity_home_iv_slideshow);
            listOfImg = pairs;
            Glide.with(HomeActivity.this).load(getResources().getIdentifier(listOfImg.get(0).second,"drawable",getPackageName())).into(imageViewSlideshow);
            imgListIndex = 0;
        }
    }
}
