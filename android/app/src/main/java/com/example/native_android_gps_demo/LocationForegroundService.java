package com.example.native_android_gps_demo;

import android.os.Build;
import android.util.Log;
import android.Manifest;
import android.os.Looper;
import android.os.IBinder;
import android.app.Service;
import android.content.Intent;
import android.app.Notification;
import android.location.Location;
import android.os.Build.VERSION_CODES;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.*;

public class LocationForegroundService extends Service {

    private static final String CHANNEL_ID = "LocationChannel";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationDatabaseHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        dbHelper = new LocationDatabaseHelper(getApplicationContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location loc = locationResult.getLastLocation();
                if (loc != null) {
                    double lat = loc.getLatitude();
                    double lng = loc.getLongitude();
                    long timestamp = System.currentTimeMillis();
                    Log.d("FGService", "Saving: " + lat + "," + lng);
                    dbHelper.insertLocation(lat, lng, timestamp);
                }
            }
        };

        requestLocationUpdates();
        createNotificationChannel();
        startForeground(1, buildNotification("Tracking location..."));
    }

    private void requestLocationUpdates() {
        LocationRequest request = LocationRequest.create();
        request.setInterval(5000);
        request.setFastestInterval(2000);
        request.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
        }
    }

    private Notification buildNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("GPS Tracking")
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Service keeps running until explicitly stopped
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}