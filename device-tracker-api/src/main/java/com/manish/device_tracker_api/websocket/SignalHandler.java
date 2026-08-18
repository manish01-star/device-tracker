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
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String role = getQuery(session, "role");
        String deviceId = getQuery(session, "deviceId");
        String stream = getQuery(session, "stream");

        if (role.isEmpty() || deviceId.isEmpty() || stream.isEmpty()) {

            log.error("Invalid websocket params");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        if (!role.equalsIgnoreCase("device")
                && !role.equalsIgnoreCase("browser")) {

            log.error("Invalid role {}", role);
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        sessionManager.addSession(
                deviceId,
                role,
                stream,
                session
        );

        log.info(
                "Connected device={} role={} stream={}",
                deviceId,
                role,
                stream
        );
    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message
    ) throws Exception {

        String role = getQuery(session, "role");
        String deviceId = getQuery(session, "deviceId");
        String stream = getQuery(session, "stream");

        if (message.getPayload() == null
                || message.getPayload().isEmpty()) {
            return;
        }

        String targetRole =
                role.equalsIgnoreCase("device")
                        ? "browser"
                        : "device";

        boolean sent =
                sessionManager.sendMessage(
                        deviceId,
                        targetRole,
                        stream,
                        message.getPayload()
                );

        if (!sent) {

            log.warn(
                    "Forward failed device={} stream={}",
                    deviceId,
                    stream
            );
        }
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status
    ) {

        notifyPeerClosed(session);
        removeSession(session);

    }

    @Override
    public void handleTransportError(
            WebSocketSession session,
            Throwable exception
    ) {

        log.error("Websocket transport error", exception);

        notifyPeerClosed(session);
        removeSession(session);

    }

    /**
     * Notify browser/device when SCREEN stream ends
     */
    private void notifyPeerClosed(WebSocketSession session) {

        String role = getQuery(session, "role");
        String deviceId = getQuery(session, "deviceId");
        String stream = getQuery(session, "stream");

        // Only notify for SCREEN stream
        if (!"screen".equalsIgnoreCase(stream)) {
            return;
        }

        String targetRole =
                role.equalsIgnoreCase("device")
                        ? "browser"
                        : "device";

        boolean sent = sessionManager.sendMessage(
                deviceId,
                targetRole,
                stream,
                "{\"type\":\"screen_stop\"}"
        );

        log.info(
                "screen_stop sent={} device={} target={} stream={}",
                sent,
                deviceId,
                targetRole,
                stream
        );
    }

    private void removeSession(WebSocketSession session) {

        String deviceId = getQuery(session, "deviceId");
        String role = getQuery(session, "role");
        String stream = getQuery(session, "stream");

        sessionManager.removeSession(
                deviceId,
                role,
                stream
        );

        log.info(
                "Disconnected device={} role={} stream={}",
                deviceId,
                role,
                stream
        );
    }

    private String getQuery(
            WebSocketSession session,
            String key
    ) {

        if (session.getUri() == null) {
            return "";
        }

        String query = session.getUri().getQuery();

        if (query == null) {
            return "";
        }

        for (String param : query.split("&")) {

            String[] data = param.split("=");

            if (data.length == 2 && key.equals(data[0])) {
                return data[1];
            }
        }

        return "";
    }
}