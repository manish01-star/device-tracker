package com.manish.device_tracker_api.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class SessionManager {


    private final Map<String, WebSocketSession> devices =
            new ConcurrentHashMap<>();


    private final Map<String, WebSocketSession> browsers =
            new ConcurrentHashMap<>();



    public void addDevice(
            String deviceId,
            WebSocketSession session
    ){

        devices.put(deviceId,session);

    }



    public void addBrowser(
            String deviceId,
            WebSocketSession session
    ){

        browsers.put(deviceId,session);

    }




    public WebSocketSession getDevice(
            String deviceId
    ){

        return devices.get(deviceId);

    }



    public WebSocketSession getBrowser(
            String deviceId
    ){

        return browsers.get(deviceId);

    }




    public void removeDevice(
            String deviceId
    ){

        devices.remove(deviceId);

    }



    public void removeBrowser(
            String deviceId
    ){

        browsers.remove(deviceId);

    }




    public void sendToDevice(
            String deviceId,
            String message
    ){

        try{

            WebSocketSession session =
                    devices.get(deviceId);


            if(session!=null && session.isOpen()){

                session.sendMessage(
                        new TextMessage(message)
                );

            }

        }
        catch(Exception e){

            e.printStackTrace();

        }


    }

    public void sendToBrowser(
        String deviceId,
        String message
){

    try{

        WebSocketSession session =
                browsers.get(deviceId);

        if(session!=null && session.isOpen()){

            session.sendMessage(
                    new TextMessage(message)
            );

        }

    }catch(Exception e){

        e.printStackTrace();

    }

}


}