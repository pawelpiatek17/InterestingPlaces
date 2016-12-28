package com.example.pawe.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Xml;

import com.example.pawe.myapplication.DatabaseContract.DatabaseImages;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.example.pawe.myapplication.DatabaseContract.DatabasePlace;

/**
 * Created by Paweł on 2016-12-26.
 */

public class DbHelper extends SQLiteOpenHelper {
    Context context;
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Database.db";
    private static final String INT_TYPE = " INT ";
    private static final String TEXT_TYPE = " TEXT ";
    private static final String COMMA_SEP = ", ";
    public static final String SQL_CREATE_PLACE =
            "CREATE TABLE " + DatabasePlace.TABLE_NAME_PLACES + " ( " +
                    DatabasePlace._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DatabasePlace.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    DatabasePlace.COLUMN_NAME_LATITUDE + TEXT_TYPE + COMMA_SEP +
                    DatabasePlace.COLUMN_NAME_LONGITUDE + TEXT_TYPE + COMMA_SEP +
                    DatabasePlace.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
//                    DatabasePlace.COLUMN_NAME_PLACES_IMGS_KEY + INT_TYPE  + COMMA_SEP +
                    DatabasePlace.COLUMN_NAME_IMG_NAME + TEXT_TYPE + COMMA_SEP +
                    DatabasePlace.COLUMN_NAME_ADDRESS + TEXT_TYPE + " );";
    public static final String SQL_CREATE_IMAGES =
            "CREATE TABLE " + DatabaseImages.TABLE_NAME_IMAGES + " ( " +
                    DatabaseImages._ID + "INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseImages.COLUMN_NAME_IMAGES_IMGS_KEY + INT_TYPE + COMMA_SEP +
                    DatabaseImages.COLUMN_NAME_PATH + TEXT_TYPE + " );";
    public static final String SQL_DELETE_PLACE =
            "DROP TABLE IF EXISTS " + DatabasePlace.TABLE_NAME_PLACES;
    public static final String SQL_DELETE_IMAGES =
            "DROP TABLE IF EXISTS " + DatabaseImages.TABLE_NAME_IMAGES;

    public DbHelper(Context c) {
        super(c, DATABASE_NAME, null, DATABASE_VERSION);
        context = c;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //db.execSQL(SQL_CREATE_IMAGES);
        db.execSQL(SQL_CREATE_PLACE);
        populateDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //dopisac upgrade -> skopiowac wszystkie dane, usunac db, stworzyc nowa i wstawic dane
    }

    private void populateDatabase(SQLiteDatabase db) {
        try {
            ArrayList<Place> places = (ArrayList) parseXml();
            for (Place p : places
                 ) {
                ContentValues values = new ContentValues();
                values.put(DatabasePlace.COLUMN_NAME_NAME,p.getName());
                values.put(DatabasePlace.COLUMN_NAME_ADDRESS,p.getAddress());
                values.put(DatabasePlace.COLUMN_NAME_DESCRIPTION,p.getDescription());
                values.put(DatabasePlace.COLUMN_NAME_IMG_NAME,p.getimgName());
                values.put(DatabasePlace.COLUMN_NAME_LATITUDE,p.getLatitude());
                values.put(DatabasePlace.COLUMN_NAME_LONGITUDE,p.getLongitude());
                db.insert(DatabasePlace.TABLE_NAME_PLACES,null,values);
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List parseXml() throws XmlPullParserException, IOException {
        InputStream input = context.getResources().openRawResource(R.raw.places);
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(input, null);
            parser.nextTag();
            return readPlaces(parser);
        } finally {
            input.close();
            context = null;

        }
    }

    private List readPlaces(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Place> places = new ArrayList<Place>();
        parser.require(XmlPullParser.START_TAG, null, "places");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            if (tagName.equals("info")) {
                places.add(readInfo(parser));
            }
        }
        return places;
    }

    private Place readInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "info");
        String name = null;
        String imgName = null;
        String address = null;
        String description = null;
        String latitude = null;
        String longitude = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            if (tagName.equals("name")) {
                name = readName(parser);
            } else if (tagName.equals("imgName")) {
                imgName = readImgName(parser);
            } else if (tagName.equals("address")) {
                address = readAddress(parser);
            } else if (tagName.equals("description")) {
                description = readDescription(parser);
            } else if (tagName.equals("imgName")) {
                imgName = readImgName(parser);
            } else if (tagName.equals("latitude")) {
                latitude = readLatitude(parser);
            } else if (tagName.equals("longitude")) {
                longitude = readLongitude(parser);
            }
        }
        return new Place(name, imgName, address, description, latitude, longitude);
    }

    private String readText(XmlPullParser parser) throws XmlPullParserException, IOException{
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private String readName(XmlPullParser parser) throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG,null,"name");
        String str = readText(parser);
        parser.require(XmlPullParser.END_TAG,null,"name");
        return str;
    }

    private String readImgName(XmlPullParser parser) throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG,null,"imgName");
        String str = readText(parser);
        parser.require(XmlPullParser.END_TAG,null,"imgName");
        return str;
    }

    private String readAddress(XmlPullParser parser) throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG,null,"address");
        String str = readText(parser);
        parser.require(XmlPullParser.END_TAG,null,"address");
        return str;
    }

    private String readDescription(XmlPullParser parser) throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG,null,"description");
        String str = readText(parser);
        parser.require(XmlPullParser.END_TAG,null,"description");
        return str;
    }

    private String readLatitude(XmlPullParser parser) throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG,null,"latitude");
        String str = readText(parser);
        parser.require(XmlPullParser.END_TAG,null,"latitude");
        return str;
    }

    private String readLongitude(XmlPullParser parser) throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG,null,"longitude");
        String str = readText(parser);
        parser.require(XmlPullParser.END_TAG,null,"longitude");
        return str;
    }
}

