package com.example.devicetrackerapp.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.devicetrackerapp.R;
import com.example.devicetrackerapp.api.ApiClient;
import com.example.devicetrackerapp.dto.ApiResponse;
import com.example.devicetrackerapp.dto.TrackingConfigResponse;
import com.example.devicetrackerapp.dto.UpdateLocationRequest;
import com.example.devicetrackerapp.utils.DeviceUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceTrackingService extends Service {

    private static final String TAG = "TrackingService";

    private Handler handler;

    private Runnable runnable;

    // Default 60 sec
    private long interval = 60000;

    private FusedLocationProviderClient locationClient;

    @Override
    public void onCreate() {

        super.onCreate();

        createNotification();

        locationClient =
                LocationServices.getFusedLocationProviderClient(this);

        handler = new Handler();

        startTracking();

    }

    private void startTracking() {

        runnable = new Runnable() {

            @Override
            public void run() {

                fetchTrackingConfig();

            }

        };

        handler.post(runnable);

    }

    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.e(TAG, "Location Permission Missing");

            return;
        }

        locationClient
                .getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null
                )
                .addOnSuccessListener(location -> {

                    if (location == null) {

                        Log.e(TAG, "Location NULL");

                        return;
                    }

                    double latitude = location.getLatitude();

                    double longitude = location.getLongitude();


                    Log.d(TAG, "LAT : " + latitude);

                    Log.d(TAG, "LNG : " + longitude);

                    updateServer(
                            latitude,
                            longitude
                    );

                });

    }

    private void updateServer(
            double latitude,
            double longitude) {

        UpdateLocationRequest request =
                new UpdateLocationRequest(
                        DeviceUtils.getDeviceId(this),
                        latitude,
                        longitude,
                        getBatteryLevel(),
                        null
                );

        ApiClient.getApiService()
                .updateDevice(request)
                .enqueue(new Callback<ApiResponse<String>>() {

                    @Override
                    public void onResponse(
                            Call<ApiResponse<String>> call,
                            Response<ApiResponse<String>> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            Log.d(TAG, "Location Updated Successfully");

                        } else {

                            Log.e(TAG, "Update Failed");

                        }

                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<String>> call,
                            Throwable t) {

                        Log.e(TAG, "API Error", t);

                    }

                });

    }

    private int getBatteryLevel() {

        Intent batteryStatus =
                registerReceiver(
                        null,
                        new IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                );

        if (batteryStatus == null) {

            return 0;

        }

        int level =
                batteryStatus.getIntExtra(
                        BatteryManager.EXTRA_LEVEL,
                        -1
                );

        int scale =
                batteryStatus.getIntExtra(
                        BatteryManager.EXTRA_SCALE,
                        -1
                );

        if (level == -1 || scale == -1) {

            return 0;

        }

        return (level * 100) / scale;

    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId) {

        return START_STICKY;

    }

    private void createNotification() {

        String channelId = "tracking_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            channelId,
                            "Device Tracking",
                            NotificationManager.IMPORTANCE_LOW
                    );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);

        }

        Notification notification =
                new NotificationCompat.Builder(
                        this,
                        channelId
                )
                        .setContentTitle("Device Tracking Active")
                        .setContentText("Tracking location in background")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .build();

        startForeground(
                1,
                notification
        );

    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        if (handler != null && runnable != null) {

            handler.removeCallbacks(runnable);

        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;

    }

    private void fetchTrackingConfig() {

        ApiClient.getApiService()
                .getTrackingConfig(DeviceUtils.getDeviceId(this))
                .enqueue(new Callback<ApiResponse<TrackingConfigResponse>>() {

                    @Override
                    public void onResponse(Call<ApiResponse<TrackingConfigResponse>> call,
                                           Response<ApiResponse<TrackingConfigResponse>> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            TrackingConfigResponse config =
                                    response.body().getData();

                            if (config != null) {

                                if (config.getTrackingInterval() != null
                                        && config.getTrackingInterval() > 0) {

                                    interval = config.getTrackingInterval() * 1000L;

                                } else {

                                    interval = 60000;

                                }

                                Log.d(TAG, "Tracking : " + config.getTrackingEnabled());
                                Log.d(TAG, "Interval : " + interval);

                                if (Boolean.TRUE.equals(config.getTrackingEnabled())) {

                                    getLocation();

                                } else {

                                    Log.d(TAG, "Tracking Disabled");

                                }
                            }
                        }

                        handler.postDelayed(runnable, interval);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<TrackingConfigResponse>> call,
                                          Throwable t) {

                        Log.e(TAG, "Config Error", t);

                        handler.postDelayed(runnable, interval);

                    }

                });

    }

}