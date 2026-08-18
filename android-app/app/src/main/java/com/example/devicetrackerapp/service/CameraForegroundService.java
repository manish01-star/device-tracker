package com.example.devicetrackerapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.devicetrackerapp.R;
import com.example.devicetrackerapp.MainActivity;
import com.example.devicetrackerapp.webrtc.PeerConnectionManager;
import com.example.devicetrackerapp.webrtc.SignalMessage;
import com.example.devicetrackerapp.webrtc.SignalingClient;

public class CameraForegroundService extends Service {

    private SignalingClient signalingClient;
    private PeerConnectionManager peerConnectionManager;

    private String deviceId;
    private String cameraType;

    private static final String CHANNEL_ID = "camera_service_channel";

    private Notification createNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Camera Service",
                    NotificationManager.IMPORTANCE_LOW
            );

            channel.setDescription("Shows when camera service is running");

            NotificationManager manager = getSystemService(NotificationManager.class);

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
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle("Camera Active")
                .setContentText("Background camera service is running")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public void onCreate(){

        super.onCreate();


    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        startForeground(
                1,
                createNotification()
        );

        deviceId = intent.getStringExtra("deviceId");
        cameraType = intent.getStringExtra("cameraType");
        if (deviceId == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        signalingClient = new SignalingClient(deviceId,"camera", new SignalingClient.Listener() {

            @Override
            public void onConnected() {

                if (peerConnectionManager == null) {
                    return;
                }

                peerConnectionManager.createPeerConnection();

                peerConnectionManager.startLocalCamera(cameraType);

                peerConnectionManager.createOffer();
            }

                    @Override
                    public void onDisconnected() {

                    }

            @Override
            public void onMessage(SignalMessage message) {

                if (message == null || peerConnectionManager == null) {
                    return;
                }

                switch (message.getType()) {

                    case "answer":

                        peerConnectionManager.setRemoteAnswer(message.getSdp());

                        break;

                    case "candidate":

                        peerConnectionManager.addIceCandidate(
                                message.getSdpMid(),
                                message.getSdpMLineIndex(),
                                message.getCandidate()
                        );

                        break;
                }
            }

                });

        peerConnectionManager = new PeerConnectionManager(
                        this,
                        signalingClient
                );

        signalingClient.connect();


        return START_STICKY;

    }


    @Override
    public void onDestroy() {

        if (peerConnectionManager != null) {
            peerConnectionManager.release();
            peerConnectionManager = null;
        }

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