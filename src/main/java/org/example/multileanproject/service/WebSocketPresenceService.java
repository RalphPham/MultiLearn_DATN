package org.example.multileanproject.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketPresenceService {

    private final ConcurrentHashMap<String, Boolean> activeUsers = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        Principal user = event.getUser();
        if (user != null) {
            activeUsers.put(user.getName(), true);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        Principal user = event.getUser();
        if (user != null) {
            activeUsers.remove(user.getName());
        }
    }

    public boolean isUserOnline(String userId) {
        return activeUsers.containsKey(userId);
    }
}