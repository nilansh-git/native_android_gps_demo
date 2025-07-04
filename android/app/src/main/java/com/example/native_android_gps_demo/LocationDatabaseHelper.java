package com.example.native_android_gps_demo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

public class LocationDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "location_data.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "locations";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_LAT = "latitude";
    public static final String COLUMN_LNG = "longitude";
    public static final String COLUMN_TIME = "timestamp";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_LAT + " REAL, " +
                    COLUMN_LNG + " REAL, " +
                    COLUMN_TIME + " INTEGER)";

    public LocationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For now: drop and recreate
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertLocation(double lat, double lng, long timestamp) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAT, lat);
        values.put(COLUMN_LNG, lng);
        values.put(COLUMN_TIME, timestamp);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }
}