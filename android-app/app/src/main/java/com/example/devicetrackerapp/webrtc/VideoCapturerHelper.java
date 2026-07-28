package com.example.devicetrackerapp.webrtc;

import android.content.Context;

import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.VideoCapturer;

public class VideoCapturerHelper {

    public static VideoCapturer createCamera(Context context, String cameraType) {

        CameraEnumerator enumerator = new Camera2Enumerator(context);

        String[] devices = enumerator.getDeviceNames();

        // Default
        if (cameraType == null || cameraType.isEmpty()) {
            cameraType = "BACK";
        }

        // ================= FRONT =================
        if ("FRONT".equalsIgnoreCase(cameraType)) {

            for (String device : devices) {

                if (enumerator.isFrontFacing(device)) {

                    CameraVideoCapturer capturer =
                            enumerator.createCapturer(device, null);

                    if (capturer != null) {
                        return capturer;
                    }
                }
            }
        }

        // ================= BACK =================
        if ("BACK".equalsIgnoreCase(cameraType)) {

            for (String device : devices) {

                if (!enumerator.isFrontFacing(device)) {

                    CameraVideoCapturer capturer =
                            enumerator.createCapturer(device, null);

                    if (capturer != null) {
                        return capturer;
                    }
                }
            }
        }

        /*
         * BOTH currently defaults to FRONT.
         * We'll implement true dual camera later.
         */
        if ("BOTH".equalsIgnoreCase(cameraType)) {

            for (String device : devices) {

                if (enumerator.isFrontFacing(device)) {

                    CameraVideoCapturer capturer =
                            enumerator.createCapturer(device, null);

                    if (capturer != null) {
                        return capturer;
                    }
                }
            }
        }

        return null;
    }
}