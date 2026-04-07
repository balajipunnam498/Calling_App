package com.prototype.calling.config;

import com.prototype.calling.model.SignalMessage;
import com.prototype.calling.service.OnlineUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens for WebSocket connect/disconnect events.
 *
 * When a user closes the browser tab or loses connection,
 * Spring Boot fires SessionDisconnectEvent automatically —
 * we use that to mark the user as offline instantly.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final OnlineUserService onlineUserService;
    private final SimpMessagingTemplate messagingTemplate;

    // Maps WebSocket sessionId → username so we know WHO disconnected
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();

    /**
     * Fired when a user connects via WebSocket.
     * We read the username from the STOMP connect headers.
     */
    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String username = accessor.getFirstNativeHeader("username");

        if (username != null && sessionId != null) {
            sessionUserMap.put(sessionId, username);
            onlineUserService.registerUser(username);
            log.info("User connected: {} (session: {})", username, sessionId);
            broadcastUserList();
        }
    }

    /**
     * Fired automatically when:
     *  - User closes browser tab
     *  - User closes browser
     *  - Network drops
     *  - User navigates away
     *
     * Spring Boot detects the WebSocket drop and fires this event.
     */
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        // Look up which user owned this session
        String username = sessionUserMap.remove(sessionId);

        if (username != null) {
            onlineUserService.removeUser(username);
            log.info("User disconnected: {} (session: {})", username, sessionId);
            broadcastUserList();
        }
    }

    /**
     * Broadcast updated online users list to all connected clients.
     */
    private void broadcastUserList() {
        List<String> users = onlineUserService.getOnlineUsers();
        SignalMessage userListMsg = new SignalMessage();
        userListMsg.setType("USER_LIST");
        userListMsg.setFrom("SERVER");
        userListMsg.setTo("ALL");
        userListMsg.setData(users);
        messagingTemplate.convertAndSend("/topic/users", userListMsg);
    }
}