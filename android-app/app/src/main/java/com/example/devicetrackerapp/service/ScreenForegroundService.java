package com.example.devicetrackerapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.app.Activity;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Display;
import android.view.WindowManager;
import android.util.DisplayMetrics;
import com.example.devicetrackerapp.MainActivity;
import com.example.devicetrackerapp.R;
import com.example.devicetrackerapp.webrtc.PeerConnectionManager;
import com.example.devicetrackerapp.webrtc.SignalMessage;
import com.example.devicetrackerapp.webrtc.SignalingClient;

public class ScreenForegroundService extends Service {

    private static final String CHANNEL_ID = "screen_service_channel";

    private SignalingClient signalingClient;

    private PeerConnectionManager peerConnectionManager;

    private String deviceId;

    private int resultCode;

    private Intent projectionData;

    private OrientationEventListener orientationListener;

    private int lastRotation = -1;

    // Screen Active Flag
    private volatile boolean streaming = false;

    // Prevent duplicate stop
    private volatile boolean stopped = false;

    private Notification createNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Screen Streaming",
                            NotificationManager.IMPORTANCE_LOW
                    );

            channel.setDescription("Live screen streaming");

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Screen Streaming")
                .setContentText("Live screen streaming is active")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public void onCreate() {

        super.onCreate();

    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {
        Log.d("SCREEN","==========================");
        Log.d("SCREEN","onStartCommand");

        if (intent == null) {

            stopSelf();

            return START_NOT_STICKY;

        }

        startForeground(2, createNotification());

        deviceId = intent.getStringExtra("deviceId");

        resultCode =
                intent.getIntExtra(
                        "resultCode",
                        Activity.RESULT_CANCELED
                );

        projectionData =
                intent.getParcelableExtra("data");

        if (deviceId == null || projectionData == null) {

            stopSelf();

            return START_NOT_STICKY;

        }

        streaming = true;

        stopped = false;

        if (RemoteAccessibilityService.instance != null) {

            RemoteAccessibilityService.instance.setStreaming(true);

        }

        signalingClient =
                new SignalingClient(
                        deviceId,
                        "screen",
                        new SignalingClient.Listener() {

                            @Override
                            public void onConnected() {
                                Log.d("SCREEN", "SOCKET CONNECTED");
                                if (peerConnectionManager == null) {
                                    return;
                                }

//                                peerConnectionManager.setSignalingClient(signalingClient);

                                peerConnectionManager.createPeerConnection();

                                peerConnectionManager.startScreenCapture(
                                        projectionData,
                                        resultCode
                                );

                                peerConnectionManager.createOffer();
                                Log.d("SCREEN","Offer Called");
                                startOrientationListener();

                            }

                            @Override
                            public void onDisconnected() {

                                Log.d("SCREEN", "Socket Disconnected");

                            }

                            @Override
                            public void onMessage(SignalMessage message) {

                                if (message == null) {
                                    return;
                                }

                                switch (message.getType()) {

                                    case "answer":

                                        if (peerConnectionManager != null) {

                                            peerConnectionManager.setRemoteAnswer(
                                                    message.getSdp()
                                            );

                                        }

                                        break;

                                    case "candidate":

                                        if (peerConnectionManager != null) {

                                            peerConnectionManager.addIceCandidate(
                                                    message.getSdpMid(),
                                                    message.getSdpMLineIndex(),
                                                    message.getCandidate()
                                            );

                                        }

                                        break;

                                    case "remote_action":

                                        Log.d("SCREEN", "REMOTE ACTION RECEIVED");

                                        Log.d("SCREEN",
                                                "Accessibility = " + RemoteAccessibilityService.instance);

                                        Log.d("SCREEN",
                                                "streaming = " + streaming);

                                        if(RemoteAccessibilityService.instance != null){

                                            Log.d("SCREEN","Calling performRemoteAction");

                                            RemoteAccessibilityService.instance.performRemoteAction(
                                                    message.getAction()
                                            );
                                        }

                                        break;

                                    case "screen_stop":

                                        Log.d("SCREEN","STOP RECEIVED");

                                        if (stopped) {
                                            return;
                                        }

                                        stopped = true;

                                        streaming = false;

                                        if (RemoteAccessibilityService.instance != null) {
                                            RemoteAccessibilityService.instance.stopStreaming();
                                        }

                                        stopSelf();

                                        break;

                                    default:

                                        Log.d(
                                                "SCREEN",
                                                "Unknown Message : " + message.getType()
                                        );

                                        break;
                                }
                            }

                        });

        peerConnectionManager = new PeerConnectionManager(this, signalingClient);
        Log.d("SCREEN", "CONNECTING SOCKET");
        signalingClient.connect();

        return START_NOT_STICKY;

    }

    @Override
    public void onDestroy() {
        Log.d("SCREEN", "SERVICE DESTROY");

        if (RemoteAccessibilityService.instance != null) {
            RemoteAccessibilityService.instance.stopStreaming();
        }

        streaming = false;

        if (peerConnectionManager != null) {

            peerConnectionManager.release();

            peerConnectionManager = null;

        }

        if (signalingClient != null) {

            signalingClient.disconnect();

            signalingClient = null;

        }

        if (orientationListener != null) {

            orientationListener.disable();

            orientationListener = null;

        }

        super.onDestroy();

    }

    private void startOrientationListener() {

        orientationListener = new OrientationEventListener(this) {

            @Override
            public void onOrientationChanged(int orientation) {

                WindowManager wm =
                        (WindowManager) getSystemService(WINDOW_SERVICE);

                if (wm == null) {
                    return;
                }

                Display display = wm.getDefaultDisplay();

                int rotation = display.getRotation();

                if (rotation == lastRotation) {
                    return;
                }

                lastRotation = rotation;

                DisplayMetrics metrics =
                        getResources().getDisplayMetrics();

                SignalMessage message = new SignalMessage();

                message.setType("screen_info");

                message.setScreenWidth(metrics.widthPixels);

                message.setScreenHeight(metrics.heightPixels);

                message.setRotation(rotation);

                signalingClient.send(message);

                Log.d(
                        "SCREEN",
                        "Orientation Changed : " + rotation
                );

            }

        };

        orientationListener.enable();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;

    }



}