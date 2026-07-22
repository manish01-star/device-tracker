package com.example.devicetrackerapp.webrtc;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import android.util.Log;

public class PeerConnectionObserver
        implements PeerConnection.Observer {

    @Override
    public void onSignalingChange(
            PeerConnection.SignalingState signalingState) {

    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState state) {

        Log.d("WEBRTC", "ICE = " + state);

        switch (state){

            case CONNECTED:
                Log.d("WEBRTC","Connected");
                break;

            case DISCONNECTED:
                Log.d("WEBRTC","Disconnected");
                break;

            case FAILED:
                Log.d("WEBRTC","Failed");
                break;

            case CLOSED:
                Log.d("WEBRTC","Closed");
                break;
        }
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {

    }

    @Override
    public void onIceGatheringChange(
            PeerConnection.IceGatheringState iceGatheringState) {

    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {

    }

    @Override
    public void onIceCandidatesRemoved(
            IceCandidate[] iceCandidates) {

    }

    @Override
    public void onAddStream(MediaStream mediaStream) {

    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {

    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {

    }

    @Override
    public void onRenegotiationNeeded() {

    }

    @Override
    public void onAddTrack(
            RtpReceiver receiver,
            MediaStream[] mediaStreams) {

    }

}