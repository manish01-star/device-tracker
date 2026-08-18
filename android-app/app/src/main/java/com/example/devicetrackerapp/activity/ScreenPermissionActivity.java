package com.example.devicetrackerapp.activity;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.devicetrackerapp.service.ScreenForegroundService;

public class ScreenPermissionActivity extends AppCompatActivity {

    private static final String TAG = "ScreenPermission";

    private MediaProjectionManager projectionManager;

    private String deviceId;

    private final ActivityResultLauncher<Intent> projectionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {

                            Log.d(TAG, "Screen permission granted");

                            Intent serviceIntent =
                                    new Intent(
                                            ScreenPermissionActivity.this,
                                            ScreenForegroundService.class
                                    );

                            serviceIntent.putExtra(
                                    "deviceId",
                                    deviceId
                            );

                            serviceIntent.putExtra(
                                    "resultCode",
                                    result.getResultCode()
                            );

                            serviceIntent.putExtra(
                                    "data",
                                    result.getData()
                            );

                            ContextCompat.startForegroundService(
                                    ScreenPermissionActivity.this,
                                    serviceIntent
                            );

                        } else {

                            Log.e(TAG, "Screen permission denied");

                        }

                        finish();

                    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        deviceId = getIntent().getStringExtra("deviceId");

        if (deviceId == null) {

            Log.e(TAG, "DeviceId Missing");

            finish();

            return;

        }

        projectionManager =
                (MediaProjectionManager)
                        getSystemService(MEDIA_PROJECTION_SERVICE);

        if (projectionManager == null) {

            Log.e(TAG, "MediaProjectionManager NULL");

            finish();

            return;

        }

        Intent captureIntent =
                projectionManager.createScreenCaptureIntent();

        projectionLauncher.launch(captureIntent);

    }

}