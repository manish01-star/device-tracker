package com.example.devicetrackerapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.devicetrackerapp.api.ApiClient;
import com.example.devicetrackerapp.dto.ApiResponse;
import com.example.devicetrackerapp.dto.RegisterDeviceRequest;
import com.example.devicetrackerapp.dto.RegisterDeviceResponse;
import com.example.devicetrackerapp.service.DeviceTrackingService;
import com.example.devicetrackerapp.utils.DeviceUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        checkPermission();
    }

    private void checkPermission() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION
            );

            return;
        }

        registerDevice();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults);

        if (requestCode == LOCATION_PERMISSION) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                registerDevice();

            } else {

                Toast.makeText(
                        this,
                        "Location Permission Required",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private void registerDevice() {

        RegisterDeviceRequest request =
                new RegisterDeviceRequest(
                        "Manish",
                        DeviceUtils.getDeviceId(this),
                        DeviceUtils.getDeviceModel(),
                        DeviceUtils.getAppVersion(this)
                );

        ApiClient.getApiService()
                .registerDevice(request)
                .enqueue(new Callback<ApiResponse<RegisterDeviceResponse>>() {

                    @Override
                    public void onResponse(
                            Call<ApiResponse<RegisterDeviceResponse>> call,
                            Response<ApiResponse<RegisterDeviceResponse>> response) {

                        startTrackingService();

                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<RegisterDeviceResponse>> call,
                            Throwable t) {

                        Log.e("REGISTER", t.getMessage());

                    }
                });

    }

    private void startTrackingService() {

        Intent intent =
                new Intent(
                        this,
                        DeviceTrackingService.class
                );

        startForegroundService(intent);

    }

}