package com.prototype.calling.controller;

import com.prototype.calling.model.CallRecord;
import com.prototype.calling.service.CallHistoryService;
import com.prototype.calling.service.OnlineUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for call history and user management.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CallHistoryController {

    private final CallHistoryService callHistoryService;
    private final OnlineUserService onlineUserService;

    /**
     * GET /api/history?username={username}
     * Returns call history for a specific user.
     */
    @GetMapping("/history")
    public ResponseEntity<List<CallRecord>> getCallHistory(@RequestParam String username) {
        List<CallRecord> history = callHistoryService.getCallHistory(username);
        return ResponseEntity.ok(history);
    }

    /**
     * GET /api/history/all
     * Returns all call records (admin/debug view).
     */
    @GetMapping("/history/all")
    public ResponseEntity<List<CallRecord>> getAllCallHistory() {
        return ResponseEntity.ok(callHistoryService.getAllCallHistory());
    }

    /**
     * GET /api/users/online
     * Returns currently online users.
     */
    @GetMapping("/users/online")
    public ResponseEntity<Map<String, Object>> getOnlineUsers() {
        List<String> users = onlineUserService.getOnlineUsers();
        return ResponseEntity.ok(Map.of(
                "count", users.size(),
                "users", users
        ));
    }

    /**
     * DELETE /api/users/{username}/offline
     * Manually mark user as offline (for cleanup).
     */
    @DeleteMapping("/users/{username}/offline")
    public ResponseEntity<Void> markUserOffline(@PathVariable String username) {
        onlineUserService.removeUser(username);
        return ResponseEntity.noContent().build();
    }
}