package com.example.devicetrackerapp.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.devicetrackerapp.R;
import com.example.devicetrackerapp.api.ApiClient;
import com.example.devicetrackerapp.webrtc.PeerConnectionManager;
import com.example.devicetrackerapp.webrtc.SignalMessage;
import com.example.devicetrackerapp.webrtc.SignalingClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG="Camera";

    private String deviceId;

    private SignalingClient signalingClient;

    private PeerConnectionManager peerConnectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);


        deviceId =
                getIntent()
                        .getStringExtra("deviceId");


        if(ContextCompat.checkSelfPermission(

                this,

                Manifest.permission.CAMERA

        ) != PackageManager.PERMISSION_GRANTED){


            finish();

            return;

        }



        initWebRTC();


        cameraStarted();


    }

    private void initWebRTC() {

        signalingClient = new SignalingClient(

                deviceId,

                new SignalingClient.Listener() {

                    @Override
                    public void onConnected() {

                        Log.d(TAG, "Socket Connected");

                        // Socket connect hone ke baad hi WebRTC start karo
                        peerConnectionManager.createPeerConnection();

                        peerConnectionManager.startLocalCamera();

                        peerConnectionManager.createOffer();
                    }

                    @Override
                    public void onDisconnected() {

                        Log.d(TAG, "Socket Disconnected");

                    }

                    @Override
                    public void onMessage(SignalMessage message) {

                        runOnUiThread(() -> {

                            switch (message.getType()) {

                                case "answer":

                                    peerConnectionManager.setRemoteAnswer(
                                            message.getSdp()
                                    );

                                    break;

                                case "candidate":

                                    peerConnectionManager.addIceCandidate(

                                            message.getSdpMid(),

                                            message.getSdpMLineIndex(),

                                            message.getCandidate()

                                    );

                                    break;

                            }

                        });

                    }

                }

        );

        peerConnectionManager = new PeerConnectionManager(this, signalingClient);

        // Sirf socket connect karo
        signalingClient.connect();

    }
    private void cameraStarted(){

        ApiClient.getApiService().cameraStarted(deviceId)

                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(
                            Call<String> call,
                            Response<String> response) {

                        Log.d(TAG,
                                "Camera Active");
                    }

                    @Override
                    public void onFailure(
                            Call<String> call,
                            Throwable t) {

                        Log.e(TAG,
                                "Camera Status Error",
                                t);
                    }

                });

    }


    private void cameraStopped(){

        ApiClient
                .getApiService()
                .cameraStopped(deviceId)

                .enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(
                            Call<String> call,
                            Response<String> response) {

                        Log.d(TAG, "Camera Stopped");

                    }

                    @Override
                    public void onFailure(
                            Call<String> call,
                            Throwable t) {
                    }

                });

    }

    @Override
    protected void onDestroy() {

        cameraStopped();

        if(peerConnectionManager!=null){

            peerConnectionManager.release();

        }

        if(signalingClient!=null){

            signalingClient.disconnect();

        }

        super.onDestroy();

    }

}