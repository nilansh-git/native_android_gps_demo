package com.example.native_android_gps_demo;

import android.util.Log;
import android.os.Build;
import android.content.Intent;
import android.content.Context;
import android.content.BroadcastReceiver;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BootReceiver", "BOOT COMPLETED received");

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootReceiver", "Starting foreground service after boot");

            Intent serviceIntent = new Intent(context, LocationForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }

}