package com.example.devicetrackerapp.webrtc;

import android.content.Context;
import android.util.Log;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraVideoCapturer;
import android.view.Display;
import android.view.WindowManager;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import org.webrtc.ScreenCapturerAndroid;
import android.app.Activity;

public class PeerConnectionManager {

    private final Context context;

    private final SignalingClient signalingClient;

    private final PeerConnectionFactory factory;

    private final EglBase eglBase;

    private PeerConnection peerConnection;

    private VideoCapturer videoCapturer;

    private SurfaceTextureHelper surfaceTextureHelper;

    private VideoSource videoSource;

    private VideoTrack localVideoTrack;

    private AudioSource audioSource;

    private AudioTrack localAudioTrack;

    private ScreenCapturerAndroid screenCapturer;

    private VideoTrack screenVideoTrack;

    private VideoSource screenVideoSource;

    private MediaProjectionManager projectionManager;

    public PeerConnectionManager(Context context, SignalingClient signalingClient) {

        this.context = context;

        this.signalingClient = signalingClient;

        WebRtcClient webRtcClient =
                WebRtcClient.getInstance(context);

        factory =
                webRtcClient.getFactory();

        eglBase =
                webRtcClient.getEglBase();

    }

    public void createPeerConnection() {

        List<PeerConnection.IceServer> servers =
                new ArrayList<>();

        servers.add(

                PeerConnection.IceServer

                        .builder("stun:stun.l.google.com:19302")

                        .createIceServer()

        );

        PeerConnection.RTCConfiguration configuration =

                new PeerConnection.RTCConfiguration(
                        servers
                );

        peerConnection =

                factory.createPeerConnection(

                        configuration,

                        new PeerConnectionObserver() {

                            @Override
                            public void onIceCandidate(
                                    IceCandidate candidate) {

                                super.onIceCandidate(candidate);

                                SignalMessage message =
                                        new SignalMessage();

                                message.setType("candidate");

                                message.setSdpMid(
                                        candidate.sdpMid
                                );

                                message.setSdpMLineIndex(
                                        candidate.sdpMLineIndex
                                );

                                message.setCandidate(
                                        candidate.sdp
                                );

                                signalingClient.send(message);

                            }

                            @Override
                            public void onIceConnectionChange(
                                    PeerConnection.IceConnectionState state){

                                Log.d("WEBRTC",state.name());

                            }

                        }

                );

    }

    public PeerConnection getPeerConnection() {

        return peerConnection;

    }

    public EglBase getEglBase() {

        return eglBase;

    }

    public void startLocalCamera(String cameraType) {

        if (videoCapturer != null) {
            stopCapture();
        }

        videoCapturer = VideoCapturerHelper.createCamera(context, cameraType);

        if (videoCapturer == null) {

            throw new RuntimeException("Camera Not Found");

        }

        surfaceTextureHelper =

                SurfaceTextureHelper.create(

                        "CaptureThread",

                        eglBase.getEglBaseContext()

                );

        videoSource = factory.createVideoSource(false);

        videoCapturer.initialize(

                surfaceTextureHelper,

                context,

                videoSource.getCapturerObserver()

        );

        videoCapturer.startCapture(640, 480, 30);

        localVideoTrack = factory.createVideoTrack("VIDEO_TRACK", videoSource);

        audioSource = factory.createAudioSource(new MediaConstraints());

        localAudioTrack = factory.createAudioTrack("AUDIO_TRACK", audioSource);

        if (peerConnection != null) {

            peerConnection.addTrack(
                    localVideoTrack,
                    Collections.singletonList("stream")
            );

            peerConnection.addTrack(
                    localAudioTrack,
                    Collections.singletonList("stream")
            );

        }

    }

    public void startScreenCapture(Intent data, int resultCode) {

        stopCapture();

        surfaceTextureHelper = SurfaceTextureHelper.create(
                        "ScreenCaptureThread",
                        eglBase.getEglBaseContext()
                );

        screenCapturer = new ScreenCapturerAndroid(
                        data,
                        new MediaProjection.Callback() {
                        }
                );

        screenVideoSource = factory.createVideoSource(false);

        screenCapturer.initialize(
                surfaceTextureHelper,
                context,
                screenVideoSource.getCapturerObserver()
        );

        try {
            screenCapturer.startCapture(720, 1280, 30);

        } catch (Exception e) {

            e.printStackTrace();

        }

        screenVideoTrack = factory.createVideoTrack("SCREEN_TRACK", screenVideoSource);

        audioSource = factory.createAudioSource(new MediaConstraints());

        localAudioTrack = factory.createAudioTrack("AUDIO_TRACK", audioSource);

        if (peerConnection != null) {

            peerConnection.addTrack(
                    screenVideoTrack,
                    Collections.singletonList("stream")
            );

            peerConnection.addTrack(
                    localAudioTrack,
                    Collections.singletonList("stream")
            );

        }

    }

    public VideoTrack getLocalVideoTrack() {

        return localVideoTrack;

    }

    public void stopCapture() {

        try {

            if (videoCapturer != null) {
                videoCapturer.stopCapture();
                videoCapturer.dispose();
                videoCapturer = null;
            }

            if (screenCapturer != null) {

                screenCapturer.stopCapture();
                screenCapturer.dispose();
                screenCapturer = null;

            }

            if (screenVideoSource != null) {

                screenVideoSource.dispose();
                screenVideoSource = null;

            }

            screenVideoTrack = null;

            if (surfaceTextureHelper != null) {
                surfaceTextureHelper.dispose();
                surfaceTextureHelper = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void release() {

        stopCapture();

        if (screenVideoSource != null) {
            screenVideoSource.dispose();
            screenVideoSource = null;

        }

        if (peerConnection != null) {
            peerConnection.close();
            peerConnection.dispose();
            peerConnection = null;
        }

        if (videoSource != null) {
            videoSource.dispose();
            videoSource = null;
        }

        if (audioSource != null) {
            audioSource.dispose();
            audioSource = null;
        }

        localVideoTrack = null;
        localAudioTrack = null;
    }
    public void createOffer() {

        MediaConstraints constraints =
                new MediaConstraints();

        constraints.mandatory.add(

                new MediaConstraints.KeyValuePair(
                        "OfferToReceiveVideo",
                        "true"
                )

        );

        constraints.mandatory.add(

                new MediaConstraints.KeyValuePair(
                        "OfferToReceiveAudio",
                        "true"
                )

        );

        if (peerConnection == null) {
            return;
        }

        peerConnection.createOffer(

                new SdpObserver() {

                    @Override
                    public void onCreateSuccess(
                            SessionDescription sessionDescription) {

                        peerConnection.setLocalDescription(

                                new SDPObserver(),

                                sessionDescription

                        );

                        SignalMessage message = new SignalMessage();

                        message.setType("offer");

                        message.setSdp(
                                sessionDescription.description
                        );

                        message.setScreenWidth(
                                context.getResources()
                                        .getDisplayMetrics()
                                        .widthPixels
                        );

                        message.setScreenHeight(
                                context.getResources()
                                        .getDisplayMetrics()
                                        .heightPixels
                        );

                        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

                        int rotation = 0;

                        if (windowManager != null) {
                            Display display = windowManager.getDefaultDisplay();
                            rotation = display.getRotation();
                        }

                        message.setRotation(rotation);

                        signalingClient.send(message);

                    }

                    @Override
                    public void onSetSuccess() {

                    }

                    @Override
                    public void onCreateFailure(
                            String s) {

                    }

                    @Override
                    public void onSetFailure(
                            String s) {

                    }

                },

                constraints

        );

    }

    public void setRemoteAnswer(String answer) {

        SessionDescription sessionDescription =

                new SessionDescription(

                        SessionDescription.Type.ANSWER,

                        answer

                );

        if (peerConnection == null) {
            return;
        }

        peerConnection.setRemoteDescription(

                new SDPObserver(),

                sessionDescription

        );

    }

    public void addIceCandidate(String sdpMid, int sdpIndex, String candidate) {

        IceCandidate iceCandidate =

                new IceCandidate(

                        sdpMid,

                        sdpIndex,

                        candidate

                );

        if(peerConnection!=null){

            peerConnection.addIceCandidate(iceCandidate);

        }

    }

    public VideoTrack getScreenVideoTrack() {

        return screenVideoTrack;

    }
}