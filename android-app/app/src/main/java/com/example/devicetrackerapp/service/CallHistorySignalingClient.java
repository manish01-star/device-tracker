package com.example.devicetrackerapp.service;

import android.util.Log;

import com.example.devicetrackerapp.webrtc.SignalMessage;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class CallHistorySignalingClient {

    private static final String TAG =
            "CALL_HISTORY_SOCKET";

    private static final String SERVER_URL =
            "ws://192.168.164.252:8080/signal";

    private final OkHttpClient client =
            new OkHttpClient();

    private final Gson gson =
            new Gson();

    private final String deviceId;

    private final Listener listener;

    private WebSocket webSocket;

    private volatile boolean connected = false;

    public interface Listener {

        void onConnected();

        void onDisconnected();

        void onMessage(
                SignalMessage message
        );
    }

    public CallHistorySignalingClient(
            String deviceId,
            Listener listener
    ) {

        this.deviceId = deviceId;
        this.listener = listener;
    }

    public synchronized void connect() {

        disconnect();

        String url =
                SERVER_URL
                        + "?deviceId="
                        + deviceId
                        + "&role=device"
                        + "&stream=call_history";

        Log.d(
                TAG,
                "Connecting = " + url
        );

        Request request =
                new Request.Builder()
                        .url(url)
                        .build();

        webSocket =
                client.newWebSocket(
                        request,
                        new WebSocketListener() {

                            @Override
                            public void onOpen(
                                    WebSocket webSocket,
                                    Response response
                            ) {

                                connected = true;

                                Log.d(
                                        TAG,
                                        "Socket Connected"
                                );

                                if (listener != null) {

                                    listener.onConnected();
                                }
                            }

                            @Override
                            public void onMessage(
                                    WebSocket webSocket,
                                    String text
                            ) {

                                Log.d(
                                        TAG,
                                        "RECEIVE -> "
                                                + text
                                );

                                try {

                                    SignalMessage message =
                                            gson.fromJson(
                                                    text,
                                                    SignalMessage.class
                                            );

                                    if (listener != null
                                            && message != null) {

                                        listener.onMessage(
                                                message
                                        );
                                    }

                                } catch (Exception e) {

                                    Log.e(
                                            TAG,
                                            "Invalid message",
                                            e
                                    );
                                }
                            }

                            @Override
                            public void onClosing(
                                    WebSocket webSocket,
                                    int code,
                                    String reason
                            ) {

                                connected = false;

                                webSocket.close(
                                        code,
                                        reason
                                );
                            }

                            @Override
                            public void onClosed(
                                    WebSocket webSocket,
                                    int code,
                                    String reason
                            ) {

                                connected = false;

                                Log.d(
                                        TAG,
                                        "Socket Closed: "
                                                + reason
                                );

                                if (listener != null) {

                                    listener.onDisconnected();
                                }
                            }

                            @Override
                            public void onFailure(
                                    WebSocket webSocket,
                                    Throwable t,
                                    Response response
                            ) {

                                connected = false;

                                Log.e(
                                        TAG,
                                        "Socket Failure",
                                        t
                                );

                                if (listener != null) {

                                    listener.onDisconnected();
                                }
                            }
                        }
                );
    }

    public synchronized void disconnect() {

        connected = false;

        if (webSocket != null) {

            try {

                webSocket.close(
                        1000,
                        "closed"
                );

            } catch (Exception ignored) {
            }

            webSocket = null;
        }
    }

    public boolean isConnected() {

        return connected
                && webSocket != null;
    }
}