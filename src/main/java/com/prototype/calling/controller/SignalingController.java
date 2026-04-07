package com.prototype.calling.controller;

import com.prototype.calling.model.SignalMessage;
import com.prototype.calling.service.CallHistoryService;
import com.prototype.calling.service.OnlineUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Handles all WebSocket/STOMP signaling messages.
 *
 * Flow:
 *  1. User A  → /app/signal (CALL_REQUEST)  → Server
 *  2. Server  → /topic/signal/{UserB}       → User B
 *  3. User B  → /app/signal (CALL_ACCEPT)   → Server
 *  4. Server  → /topic/signal/{UserA}       → User A
 *  5. User A  → /app/signal (OFFER)         → Server → User B
 *  6. User B  → /app/signal (ANSWER)        → Server → User A
 *  7. Both    → /app/signal (ICE_CANDIDATE) → Server → Other peer
 *  8. Either  → /app/signal (CALL_END)      → Server → Other peer
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class SignalingController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CallHistoryService callHistoryService;
    private final OnlineUserService onlineUserService;

    /**
     * Central signal handler — forwards messages to the intended recipient.
     */
    @MessageMapping("/signal")
    public void handleSignal(@Payload SignalMessage signal) {
        log.info("Signal received: type={}, from={}, to={}", signal.getType(), signal.getFrom(), signal.getTo());

        // Update call history for relevant event types
        callHistoryService.handleSignal(signal);

        // If the target is SERVER (e.g., REGISTER), handle internally
        if ("SERVER".equals(signal.getTo())) {
            handleServerSignal(signal);
            return;
        }

        // Forward to the recipient's personal topic channel
        messagingTemplate.convertAndSend("/topic/signal/" + signal.getTo(), signal);
    }

    /**
     * Handle server-bound signals (REGISTER, etc.)
     */
    private void handleServerSignal(SignalMessage signal) {
        if ("REGISTER".equals(signal.getType())) {
            onlineUserService.registerUser(signal.getFrom());
            log.info("User registered: {}", signal.getFrom());

            // Broadcast updated user list to everyone
            broadcastUserList();
        }
    }

    /**
     * Handle user going online/offline and broadcast user list.
     */
    @MessageMapping("/register")
    public void registerUser(@Payload SignalMessage signal) {
        onlineUserService.registerUser(signal.getFrom());
        log.info("User registered via /register: {}", signal.getFrom());
        broadcastUserList();
    }

    /**
     * Handle user disconnecting.
     */
    @MessageMapping("/unregister")
    public void unregisterUser(@Payload SignalMessage signal) {
        onlineUserService.removeUser(signal.getFrom());
        log.info("User unregistered: {}", signal.getFrom());
        broadcastUserList();
    }

    /**
     * Broadcast the current online user list to all connected clients.
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