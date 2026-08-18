package com.manish.device_tracker_api.websocket;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
public class SessionManager {


    private final Map<String, WebSocketSession> sessions =
            new ConcurrentHashMap<>();



    private String generateKey(
            String deviceId,
            String role,
            String stream
    ){

        return deviceId 
                + "_"
                + role
                + "_"
                + stream;

    }





    /**
     * Add websocket session
     */
    public void addSession(
            String deviceId,
            String role,
            String stream,
            WebSocketSession session
    ){


        String key =
                generateKey(
                        deviceId,
                        role,
                        stream
                );



        WebSocketSession oldSession =
                sessions.get(key);



        // close old connection
        if(oldSession != null
                && oldSession.isOpen()){

            try {

                oldSession.close();

            }
            catch(Exception e){

                log.error(
                    "Old session close error",
                    e
                );

            }

        }



        sessions.put(
                key,
                session
        );



        log.info(
                "Session Added : {}",
                key
        );


    }







    /**
     * Get session
     */
    public WebSocketSession getSession(
            String deviceId,
            String role,
            String stream
    ){

        return sessions.get(
                generateKey(
                        deviceId,
                        role,
                        stream
                )
        );

    }







    /**
     * Remove session
     */
    public void removeSession(
            String deviceId,
            String role,
            String stream
    ){


        String key =
                generateKey(
                        deviceId,
                        role,
                        stream
                );


        sessions.remove(key);


        log.info(
                "Session Removed : {}",
                key
        );


    }







    /**
     * Send message
     */
    public boolean sendMessage(
            String deviceId,
            String targetRole,
            String stream,
            String message
    ){


        try{


            WebSocketSession session =
                    getSession(
                            deviceId,
                            targetRole,
                            stream
                    );



            if(session == null
                    ||
               !session.isOpen()){


                log.warn(
                    "Target session unavailable {} {} {}",
                    deviceId,
                    targetRole,
                    stream
                );


                return false;

            }





            synchronized(session){


                session.sendMessage(
                        new TextMessage(message)
                );


            }



            log.info(
                "Message sent {} {}",
                stream,
                deviceId
            );


            return true;



        }
        catch(Exception e){


            log.error(
                "Message send failed",
                e
            );


            return false;

        }


    }







    /**
     * Remove all device sessions
     */
    public void removeDeviceSessions(
            String deviceId
    ){


        sessions.entrySet()
                .removeIf(entry -> {

                    boolean remove =
                            entry.getKey()
                            .startsWith(deviceId+"_");


                    if(remove){

                        try{

                            entry.getValue()
                            .close();

                        }
                        catch(Exception ignored){}


                    }


                    return remove;


                });



        log.info(
                "All sessions removed : {}",
                deviceId
        );


    }







    /**
     * Connection check
     */
    public boolean isConnected(
            String deviceId,
            String role,
            String stream
    ){


        WebSocketSession session =
                getSession(
                        deviceId,
                        role,
                        stream
                );


        return session != null
                &&
                session.isOpen();


    }



}