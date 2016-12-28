package com.example.pawe.myapplication;

import android.provider.BaseColumns;

/**
 * Created by Paweł on 2016-12-26.
 */

public final class DatabaseContract {
    private DatabaseContract() {}
    public static class DatabasePlace implements BaseColumns {
        public static final String TABLE_NAME_PLACES = "place";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_PLACES_IMGS_KEY = "images_key_place";
        public static final String COLUMN_NAME_IMG_NAME = "image_name";
        public static final String COLUMN_NAME_ADDRESS = "address";
    }
    public static class DatabaseImages implements BaseColumns {
        public static final String TABLE_NAME_IMAGES = "images";
        public static final String COLUMN_NAME_IMAGES_IMGS_KEY = "images_key_images";
        public static final String COLUMN_NAME_PATH = "path";
    }
}
