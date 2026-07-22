package com.example.devicetrackerapp.webrtc;

import android.content.Context;

import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.PeerConnectionFactory;

public class WebRtcClient {

    private static WebRtcClient instance;

    private final PeerConnectionFactory factory;

    private final EglBase eglBase;

    private WebRtcClient(Context context) {

        PeerConnectionFactory.initialize(

                PeerConnectionFactory
                        .InitializationOptions
                        .builder(context)
                        .createInitializationOptions()

        );

        eglBase = EglBase.create();

        DefaultVideoEncoderFactory encoderFactory =

                new DefaultVideoEncoderFactory(

                        eglBase.getEglBaseContext(),

                        true,

                        true

                );

        DefaultVideoDecoderFactory decoderFactory =

                new DefaultVideoDecoderFactory(

                        eglBase.getEglBaseContext()

                );

        factory =

                PeerConnectionFactory.builder()

                        .setVideoEncoderFactory(encoderFactory)

                        .setVideoDecoderFactory(decoderFactory)

                        .createPeerConnectionFactory();

    }

    public static synchronized WebRtcClient getInstance(Context context) {

        if (instance == null) {

            instance = new WebRtcClient(context);

        }

        return instance;

    }

    public PeerConnectionFactory getFactory() {

        return factory;

    }

    public EglBase getEglBase() {

        return eglBase;

    }

}