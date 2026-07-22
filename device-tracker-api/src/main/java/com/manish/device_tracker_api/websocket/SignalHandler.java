package com.manish.device_tracker_api.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignalHandler extends TextWebSocketHandler {

    private final SessionManager sessionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session)
            throws Exception {

        String type = getQuery(session, "type");
        String deviceId = getQuery(session, "deviceId");

        if (deviceId == null || deviceId.isEmpty()) {
            session.close();
            return;
        }

        if ("device".equalsIgnoreCase(type)) {

            sessionManager.addDevice(deviceId, session);

            log.info("Android Connected : {}", deviceId);

        } else if ("browser".equalsIgnoreCase(type)) {

            sessionManager.addBrowser(deviceId, session);

            log.info("Browser Connected : {}", deviceId);

        } else {

            session.close();

        }

    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message) throws Exception {

        String type = getQuery(session, "type");
        String deviceId = getQuery(session, "deviceId");

        WebSocketSession target;

        if ("device".equalsIgnoreCase(type)) {

            target = sessionManager.getBrowser(deviceId);

        } else {

            target = sessionManager.getDevice(deviceId);

        }

        if (target != null && target.isOpen()) {

            target.sendMessage(message);

        }

    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status) throws Exception {

        String type = getQuery(session, "type");
        String deviceId = getQuery(session, "deviceId");

        if ("device".equalsIgnoreCase(type)) {

            sessionManager.removeDevice(deviceId);

            log.info("Android Disconnected : {}", deviceId);

        } else {

            sessionManager.removeBrowser(deviceId);

            log.info("Browser Disconnected : {}", deviceId);

        }

    }

    @Override
    public void handleTransportError(
            WebSocketSession session,
            Throwable exception) throws Exception {

        // session.close();
        log.error("Transport Error", exception);

    }

    private String getQuery(
            WebSocketSession session,
            String key) {

        if (session.getUri() == null)
            return "";

        String query = session.getUri().getQuery();

        if (query == null)
            return "";

        for (String p : query.split("&")) {

            String[] kv = p.split("=");

            if (kv.length == 2 && key.equals(kv[0])) {

                return kv[1];

            }

        }

        return "";

    }

}