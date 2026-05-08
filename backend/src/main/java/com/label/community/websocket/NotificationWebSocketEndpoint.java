package com.label.community.websocket;

import com.label.community.common.JsonUtil;
import com.label.community.security.AuthUser;
import com.label.community.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/notifications")
public class NotificationWebSocketEndpoint {
    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketEndpoint.class);
    private static final ConcurrentHashMap<Long, Set<Session>> USER_SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) throws IOException {
        String token = null;
        if (session.getRequestParameterMap().containsKey("token")) {
            token = session.getRequestParameterMap().get("token").get(0);
        }
        if (token == null || token.isBlank()) {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "missing token"));
            return;
        }

        AuthUser user = JwtUtil.parse(token);
        session.getUserProperties().put("userId", user.getUserId());
        USER_SESSIONS.computeIfAbsent(user.getUserId(), key -> ConcurrentHashMap.newKeySet()).add(session);

        send(session, Map.of(
            "event", "connected",
            "message", "通知通道已建立",
            "time", LocalDateTime.now().toString()
        ));
    }

    @OnClose
    public void onClose(Session session) {
        removeSession(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.warn("websocket error: {}", throwable.getMessage());
        removeSession(session);
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        if ("ping".equalsIgnoreCase(message)) {
            send(session, Map.of(
                "event", "pong",
                "time", LocalDateTime.now().toString()
            ));
        }
    }

    public static void pushToUser(Long userId, Map<String, Object> payload) {
        Set<Session> sessions = USER_SESSIONS.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        sessions.removeIf(session -> !session.isOpen());
        for (Session session : sessions) {
            try {
                send(session, payload);
            } catch (Exception ex) {
                log.debug("skip invalid websocket session: {}", ex.getMessage());
            }
        }
    }

    private static void send(Session session, Map<String, ?> payload) throws IOException {
        if (!session.isOpen()) {
            return;
        }
        synchronized (session) {
            session.getBasicRemote().sendText(JsonUtil.write(payload));
        }
    }

    private void removeSession(Session session) {
        Object userIdObj = session.getUserProperties().get("userId");
        if (!(userIdObj instanceof Long)) {
            return;
        }
        Long userId = (Long) userIdObj;
        Set<Session> sessions = USER_SESSIONS.get(userId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            USER_SESSIONS.remove(userId);
        }
    }
}
