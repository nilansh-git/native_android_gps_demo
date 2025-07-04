package com.example.native_android_gps_demo;

import android.os.Build;
import android.Manifest;
import android.util.Log;
import android.os.Bundle;
import android.os.Looper;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.content.pm.PackageManager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.Priority;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.android.FlutterActivity;

public class MainActivity extends FlutterActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private static final String LOCATION_STREAM_CHANNEL = "native_location_stream";
    private static final String SQLITE_CHANNEL = "native_sqlite_channel";
    private EventChannel.EventSink eventSink;
    private LocationDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        dbHelper = new LocationDatabaseHelper(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        },
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                startForegroundLocationService();
                startLocationUpdates();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                startForegroundLocationService();
                startLocationUpdates();
            }
        }
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//        dbHelper = new LocationDatabaseHelper(getApplicationContext());
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    LOCATION_PERMISSION_REQUEST_CODE);
//        } else {
//            startForegroundLocationService();
//            startLocationUpdates();
//        }
//    }

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);

        new EventChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), LOCATION_STREAM_CHANNEL)
                .setStreamHandler(new EventChannel.StreamHandler() {
                    @Override
                    public void onListen(Object arguments, EventChannel.EventSink events) {
                        eventSink = events;
                    }

                    @Override
                    public void onCancel(Object arguments) {
                        eventSink = null;
                    }
                });

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), SQLITE_CHANNEL)
                .setMethodCallHandler((call, result) -> {
                    if (call.method.equals("getStoredLocations")) {
                        try {
                            List<Map<String, Object>> data = getStoredLocationsFromDB();
                            result.success(data);
                        } catch (Exception e) {
                            result.error("SQLITE_ERROR", "Failed to fetch data", null);
                        }
                    } else if (call.method.equals("clearStoredLocations")) {
                        try {
                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            db.delete(LocationDatabaseHelper.TABLE_NAME, null, null);
                            db.close();
                            result.success("cleared");
                        } catch (Exception e) {
                            result.error("SQLITE_CLEAR_ERROR", "Failed to clear data", null);
                        }
                    } else {
                        result.notImplemented();
                    }
                });

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), "foreground_service_channel")
                .setMethodCallHandler((call, result) -> {
                    if (call.method.equals("startService")) {
                        startForegroundLocationService();
                        result.success("started");
                    } else if (call.method.equals("stopService")) {
                        stopService(new Intent(this, LocationForegroundService.class));
                        result.success("stopped");
                    } else {
                        result.notImplemented();
                    }
                });
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // 5 seconds
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;

                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                long timestamp = System.currentTimeMillis();

                Log.d("Location", "Lat: " + latitude + ", Lng: " + longitude + ", Time: " + timestamp);

                // Later: send to Flutter or store in SQLite
                if (eventSink != null) {
                    HashMap<String, Object> locationData = new HashMap<>();
                    locationData.put("lat", latitude);
                    locationData.put("lng", longitude);
                    locationData.put("timestamp", timestamp);

                    eventSink.success(locationData);
                }

                if (dbHelper != null) {
                    dbHelper.insertLocation(latitude, longitude, timestamp);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            boolean fineGranted = false;
            boolean backgroundGranted = false;

            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    fineGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
                if (permissions[i].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    backgroundGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (fineGranted && backgroundGranted) {
                    startForegroundLocationService();
                    startLocationUpdates();
                } else {
                    Log.e("Permission", "Both foreground and background location permissions are required");
                }
            } else {
                if (fineGranted) {
                    startForegroundLocationService();
                    startLocationUpdates();
                } else {
                    Log.e("Permission", "Location permission denied");
                }
            }
        }
    }

    private List<Map<String, Object>> getStoredLocationsFromDB() {
        List<Map<String, Object>> locationList = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                LocationDatabaseHelper.TABLE_NAME,
                null, null, null, null, null,
                LocationDatabaseHelper.COLUMN_ID + " DESC"
        );

        while (cursor.moveToNext()) {
            double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(LocationDatabaseHelper.COLUMN_LAT));
            double lng = cursor.getDouble(cursor.getColumnIndexOrThrow(LocationDatabaseHelper.COLUMN_LNG));
            long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(LocationDatabaseHelper.COLUMN_TIME));

            Map<String, Object> location = new HashMap<>();
            location.put("lat", lat);
            location.put("lng", lng);
            location.put("timestamp", timestamp);
            locationList.add(location);
        }

        cursor.close();
        db.close();

        return locationList;
    }

    private void startForegroundLocationService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, LocationForegroundService.class));
        } else {
            startService(new Intent(this, LocationForegroundService.class));
        }
    }
}