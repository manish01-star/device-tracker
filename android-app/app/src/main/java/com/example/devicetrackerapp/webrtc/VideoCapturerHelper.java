package com.example.devicetrackerapp.webrtc;

import android.content.Context;

import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.VideoCapturer;

public class VideoCapturerHelper {

    public static VideoCapturer createCamera(Context context) {

        CameraEnumerator enumerator = new Camera2Enumerator(context);

        String[] devices = enumerator.getDeviceNames();

        // Front Camera
        for (String device : devices) {

            if (enumerator.isFrontFacing(device)) {

                CameraVideoCapturer capturer =
                        enumerator.createCapturer(device, null);

                if (capturer != null) {

                    return capturer;

                }

            }

        }

        // Back Camera
        for (String device : devices) {

            if (!enumerator.isFrontFacing(device)) {

                CameraVideoCapturer capturer =
                        enumerator.createCapturer(device, null);

                if (capturer != null) {

                    return capturer;

                }

            }

        }

        return null;

    }

}