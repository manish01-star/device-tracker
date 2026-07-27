package com.example.devicetrackerapp.webrtc;


import android.util.Log;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;


public class SignalingClient {


    private static final String TAG = "SIGNAL";


    // Local testing
//    private static final String SERVER_URL = "ws://172.16.36.94:8080/signal";

    private static final String SERVER_URL = "ws://192.168.164.252:8080/signal";


    // Production later
    // private static final String SERVER_URL = "wss://your-domain.com/signal";


    private WebSocket webSocket;


    private final Gson gson =
            new Gson();


    private final String deviceId;


    private final Listener listener;



    public interface Listener {


        void onConnected();


        void onDisconnected();


        void onMessage(
                SignalMessage message
        );


    }



    public SignalingClient(
            String deviceId,
            Listener listener
    ){

        this.deviceId = deviceId;

        this.listener = listener;

    }





    public void connect(){


        OkHttpClient client =
                new OkHttpClient();



        String url =
                SERVER_URL
                        +
                        "?type=device&deviceId="
                        +
                        deviceId;



        Request request =
                new Request.Builder()
                        .url(url)
                        .build();




        webSocket =
                client.newWebSocket(
                        request,
                        new WebSocketListener(){



                            @Override
                            public void onOpen(
                                    WebSocket webSocket,
                                    Response response
                            ){

                                Log.d(
                                        TAG,
                                        "Connected"
                                );


                                listener.onConnected();

                            }





                            @Override
                            public void onMessage(
                                    WebSocket webSocket,
                                    String text
                            ){


                                Log.d(
                                        TAG,
                                        text
                                );


                                SignalMessage message =
                                        gson.fromJson(
                                                text,
                                                SignalMessage.class
                                        );


                                listener.onMessage(message);


                            }





                            @Override
                            public void onClosed(
                                    WebSocket webSocket,
                                    int code,
                                    String reason
                            ){

                                listener.onDisconnected();

                            }





                            @Override
                            public void onFailure(
                                    WebSocket webSocket,
                                    Throwable t,
                                    Response response
                            ){


                                Log.e(
                                        TAG,
                                        "Socket Error",
                                        t
                                );


                            }


                        });

    }


    public void send(SignalMessage message){
        if(webSocket!=null){

            webSocket.send(gson.toJson(message));
            Log.d(TAG, "SEND -> " + message);

        }


    }


    public void disconnect(){


        if(webSocket!=null){


            webSocket.close(
                    1000,
                    "closed"
            );


        }


    }



}