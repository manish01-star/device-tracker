package com.example.devicetrackerapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.devicetrackerapp.R;
import com.example.devicetrackerapp.api.ApiClient;
import com.example.devicetrackerapp.dto.CallHistoryRequest;
import com.example.devicetrackerapp.dto.CallHistorySyncItemDTO;
import com.example.devicetrackerapp.dto.CallHistorySyncRequest;
import com.example.devicetrackerapp.dto.ApiResponse;
import com.example.devicetrackerapp.utils.CallHistoryUtils;
import com.example.devicetrackerapp.webrtc.SignalMessage;

import java.time.LocalDate;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallHistoryForegroundService extends Service {

    private static final String TAG = "CALL_HISTORY_SERVICE";

    private static final String CHANNEL_ID =
            "call_history_service_channel";

    private static final int NOTIFICATION_ID = 3;

    private String deviceId;

    private CallHistorySignalingClient signalingClient;

    @Override
    public void onCreate() {

        super.onCreate();

        Log.d(TAG, "Call History Service Created");

        createNotificationChannel();
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {

        Log.d(TAG, "==============================");
        Log.d(TAG, "Call History Service Started");

        if (intent == null) {

            Log.e(TAG, "Intent is null");

            stopSelf();

            return START_NOT_STICKY;
        }

        startForeground(
                NOTIFICATION_ID,
                createNotification()
        );

        deviceId =
                intent.getStringExtra("deviceId");

        if (deviceId == null || deviceId.isBlank()) {

            Log.e(TAG, "Device ID missing");

            stopSelf();

            return START_NOT_STICKY;
        }

        Log.d(
                TAG,
                "Starting Call History Socket for device = "
                        + deviceId
        );

        connectSocket();

        return START_STICKY;
    }

    private void connectSocket() {

        if (signalingClient != null) {

            signalingClient.disconnect();

            signalingClient = null;
        }

        signalingClient =
                new CallHistorySignalingClient(
                        deviceId,
                        new CallHistorySignalingClient.Listener() {

                            @Override
                            public void onConnected() {

                                Log.d(
                                        TAG,
                                        "Call History Socket Connected"
                                );
                            }

                            @Override
                            public void onDisconnected() {

                                Log.d(
                                        TAG,
                                        "Call History Socket Disconnected"
                                );
                            }

                            @Override
                            public void onMessage(
                                    SignalMessage message
                            ) {

                                if (message == null) {
                                    return;
                                }

                                handleMessage(message);
                            }
                        }
                );

        signalingClient.connect();
    }

    private void handleMessage(
            SignalMessage message
    ) {

        String type = message.getType();

        if (type == null) {
            return;
        }

        Log.d(
                TAG,
                "Received message type = " + type
        );

        if ("call_history_request"
                .equalsIgnoreCase(type)) {

            CallHistoryRequest request =
                    message.getCallHistoryRequest();

            if (request == null) {

                Log.e(
                        TAG,
                        "Call history request payload is null"
                );

                return;
            }

            LocalDate fromDate =
                    request.getFromDate();

            LocalDate toDate =
                    request.getToDate();

            Log.d(
                    TAG,
                    "Call History Request: "
                            + fromDate
                            + " -> "
                            + toDate
            );

            syncCallHistory(
                    deviceId,
                    fromDate,
                    toDate
            );
        }
    }

    private void syncCallHistory(
            String deviceId,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        Log.d(
                TAG,
                "Starting Call History Sync: "
                        + fromDate
                        + " -> "
                        + toDate
        );

        if (fromDate == null || toDate == null) {

            Log.e(
                    TAG,
                    "FromDate or ToDate is null"
            );

            return;
        }

        CallHistoryUtils utils =
                new CallHistoryUtils(this);

        List<CallHistorySyncItemDTO> calls =
                utils.getCallHistory(
                        fromDate,
                        toDate
                );

        Log.d(
                TAG,
                "Call history records found = "
                        + calls.size()
        );

        CallHistorySyncRequest request =
                new CallHistorySyncRequest();

        request.setDeviceId(deviceId);
        request.setFromDate(fromDate);
        request.setToDate(toDate);
        request.setCalls(calls);

        ApiClient.getApiService()
                .syncCallHistory(request)
                .enqueue(
                        new Callback<ApiResponse<String>>() {

                            @Override
                            public void onResponse(
                                    Call<ApiResponse<String>> call,
                                    Response<ApiResponse<String>> response
                            ) {

                                if (response.isSuccessful()
                                        && response.body() != null
                                        && response.body().isSuccess()) {

                                    Log.d(
                                            TAG,
                                            "Call history synced successfully"
                                    );

                                } else {

                                    Log.e(
                                            TAG,
                                            "Call history sync failed. HTTP="
                                                    + response.code()
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<ApiResponse<String>> call,
                                    Throwable t
                            ) {

                                Log.e(
                                        TAG,
                                        "Call history sync API failed",
                                        t
                                );
                            }
                        }
                );
    }

    private Notification createNotification() {

        return new NotificationCompat.Builder(
                this,
                CHANNEL_ID
        )
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Call History")
                .setContentText(
                        "Call history sync service is running"
                )
                .setOngoing(true)
                .setPriority(
                        NotificationCompat.PRIORITY_LOW
                )
                .build();
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Call History Sync",
                            NotificationManager.IMPORTANCE_LOW
                    );

            channel.setDescription(
                    "Call history synchronization service"
            );

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class
                    );

            if (manager != null) {

                manager.createNotificationChannel(
                        channel
                );
            }
        }
    }

    @Override
    public void onDestroy() {

        Log.d(
                TAG,
                "Call History Service Destroyed"
        );

        if (signalingClient != null) {

            signalingClient.disconnect();

            signalingClient = null;
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}