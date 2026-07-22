package com.example.devicetrackerapp.activity;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.devicetrackerapp.R;
import com.example.devicetrackerapp.api.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CameraPermissionActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION = 1001;

    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera_permission);

        deviceId = getIntent().getStringExtra("deviceId");

        findViewById(R.id.btnAllow).setOnClickListener(v -> {

                    checkCameraPermission();

                });


        findViewById(R.id.btnDeny)
                .setOnClickListener(v -> {

                    ApiClient.getApiService()
                            .cameraStopped(deviceId)
                            .enqueue(new Callback<String>() {

                                @Override
                                public void onResponse(
                                        Call<String> call,
                                        Response<String> response) {


                                }

                                @Override
                                public void onFailure(
                                        Call<String> call,
                                        Throwable t) {


                                }

                            });


                    finish();

                });


    }

    private void checkCameraPermission(){


        if(ContextCompat.checkSelfPermission(

                this,

                Manifest.permission.CAMERA

        )
                == PackageManager.PERMISSION_GRANTED){


            openCamera();


        }
        else {


            ActivityCompat.requestPermissions(

                    this,

                    new String[]{
                            Manifest.permission.CAMERA
                    },

                    CAMERA_PERMISSION

            );

        }


    }



    @Override
    public void onRequestPermissionsResult(

            int requestCode,

            @NonNull String[] permissions,

            @NonNull int[] grantResults) {


        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );


        if(requestCode == CAMERA_PERMISSION){


            if(grantResults.length > 0
                    &&
                    grantResults[0]
                            ==
                            PackageManager.PERMISSION_GRANTED){


                openCamera();


            }


        }


    }



    private void openCamera(){


        Intent intent =
                new Intent(
                        this,
                        CameraActivity.class
                );


        intent.putExtra(
                "deviceId",
                deviceId
        );


        startActivity(intent);


        finish();


    }


}