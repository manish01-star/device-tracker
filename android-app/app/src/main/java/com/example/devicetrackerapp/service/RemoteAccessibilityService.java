package com.example.devicetrackerapp.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.example.devicetrackerapp.model.RemoteAction;

import java.util.concurrent.atomic.AtomicBoolean;

public class RemoteAccessibilityService extends AccessibilityService {

    private static final String TAG = "ACCESSIBILITY";

    public static RemoteAccessibilityService instance;

    // Gesture Queue
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final AtomicBoolean gestureRunning = new AtomicBoolean(false);

    // Screen Streaming Status
    private volatile boolean streaming = false;


    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();

        instance = this;

        Log.d(TAG, "Accessibility Connected");
    }

    /**
     * Called from ScreenForegroundService
     */
    public void setStreaming(boolean enabled) {

        streaming = enabled;

        Log.d(TAG, "Streaming = " + enabled);

    }

    public boolean isStreaming() {

        return streaming;

    }

    public void stopStreaming() {

        streaming = false;

        handler.removeCallbacksAndMessages(null);

        gestureRunning.set(false);

        Log.d(TAG, "Streaming stopped");

    }

    public void performRemoteAction(RemoteAction action) {

        Log.d(TAG,"performRemoteAction called");
        Log.d(TAG,"streaming="+streaming);
        Log.d(TAG,"action="+action.getType());

        if (!streaming) {

            Log.d(TAG, "Ignore Action. Streaming OFF");

            return;

        }

        if (action == null) {
            return;
        }

        handler.post(() -> {

            if (!streaming) {
                return;
            }

            if (gestureRunning.get()) {
                return;
            }

            gestureRunning.set(true);

            switch (action.getType()) {

                case "CLICK":

                    click(
                            action.getX(),
                            action.getY()
                    );

                    break;

                case "SCROLL_UP":

                    scrollUp();

                    break;

                case "SCROLL_DOWN":

                    scrollDown();

                    break;

                case "SWIPE":

                    swipe(
                            action.getX(),
                            action.getY(),
                            action.getEndX(),
                            action.getEndY(),
                            action.getDuration()
                    );

                    break;

                case "LONG_PRESS":

                    longPress(
                            action.getX(),
                            action.getY(),
                            action.getDuration()
                    );

                    break;

                default:

                    gestureRunning.set(false);

                    break;
            }

        });

    }

    private void click(float x, float y) {

        DisplayMetrics metrics = getResources().getDisplayMetrics();

        Log.d("CLICK",
                "Phone = " +
                        metrics.widthPixels + " x " +
                        metrics.heightPixels);

        Log.d("CLICK",
                "Touch = " + x + "," + y);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return;
        }

        gestureRunning.set(true);

        Path path = new Path();
        path.moveTo(x, y);

        GestureDescription gesture =
                new GestureDescription.Builder()
                        .addStroke(
                                new GestureDescription.StrokeDescription(
                                        path,
                                        0,
                                        60
                                )
                        )
                        .build();

        dispatchGesture(
                gesture,
                gestureCallback,
                handler
        );

    }

    private void scrollDown() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return;
        }

        gestureRunning.set(true);

        DisplayMetrics metrics = getResources().getDisplayMetrics();

        float centerX = metrics.widthPixels / 2f;

        float startY = metrics.heightPixels * 0.82f;

        float endY = metrics.heightPixels * 0.18f;

        Path path = new Path();

        path.moveTo(centerX, startY);

        path.lineTo(centerX, endY);

        GestureDescription gesture =
                new GestureDescription.Builder()
                        .addStroke(
                                new GestureDescription.StrokeDescription(
                                        path,
                                        0,
                                        350
                                )
                        )
                        .build();

        dispatchGesture(
                gesture,
                gestureCallback,
                handler
        );

    }

    private void scrollUp() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return;
        }

        gestureRunning.set(true);

        DisplayMetrics metrics = getResources().getDisplayMetrics();

        float centerX = metrics.widthPixels / 2f;

        float startY = metrics.heightPixels * 0.18f;

        float endY = metrics.heightPixels * 0.82f;

        Path path = new Path();

        path.moveTo(centerX, startY);

        path.lineTo(centerX, endY);

        GestureDescription gesture =
                new GestureDescription.Builder()
                        .addStroke(
                                new GestureDescription.StrokeDescription(
                                        path,
                                        0,
                                        350
                                )
                        )
                        .build();

        dispatchGesture(
                gesture,
                gestureCallback,
                handler
        );

    }

    private void swipe(
            float startX,
            float startY,
            float endX,
            float endY,
            long duration
    ) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return;
        }

        gestureRunning.set(true);

        Path path = new Path();

        path.moveTo(startX, startY);

        path.lineTo(endX, endY);

        if (duration < 200) {
            duration = 200;
        }

        GestureDescription gesture =
                new GestureDescription.Builder()
                        .addStroke(
                                new GestureDescription.StrokeDescription(
                                        path,
                                        0,
                                        duration
                                )
                        )
                        .build();

        dispatchGesture(
                gesture,
                gestureCallback,
                handler
        );

    }

    private void longPress(
            float x,
            float y,
            long duration
    ) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return;
        }

        gestureRunning.set(true);

        Path path = new Path();

        path.moveTo(x, y);

        if (duration < 700) {
            duration = 700;
        }

        GestureDescription gesture =
                new GestureDescription.Builder()
                        .addStroke(
                                new GestureDescription.StrokeDescription(
                                        path,
                                        0,
                                        duration
                                )
                        )
                        .build();

        dispatchGesture(
                gesture,
                gestureCallback,
                handler
        );

    }

    // =======================================================
// Gesture Callback
// =======================================================

    private final GestureResultCallback gestureCallback =
            new GestureResultCallback() {

                @Override
                public void onCompleted(GestureDescription gestureDescription) {

                    super.onCompleted(gestureDescription);

                    gestureRunning.set(false);

                    Log.d(TAG, "Gesture Completed");

                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {

                    super.onCancelled(gestureDescription);

                    gestureRunning.set(false);

                    Log.d(TAG, "Gesture Cancelled");

                }

            };


// =======================================================
// Accessibility Callbacks
// =======================================================

    @Override
    public void onAccessibilityEvent(
            AccessibilityEvent event
    ) {

        // No implementation required

    }

    @Override
    public void onInterrupt() {

        gestureRunning.set(false);

        Log.d(TAG, "Accessibility Interrupted");

    }


// =======================================================
// Lifecycle
// =======================================================

    @Override
    public boolean onUnbind(Intent intent) {

        streaming = false;

        gestureRunning.set(false);

        instance = null;

        Log.d(TAG, "Accessibility Unbound");

        return super.onUnbind(intent);

    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        streaming = false;

        gestureRunning.set(false);

        instance = null;

        Log.d(TAG, "Accessibility Destroyed");

    }

}

