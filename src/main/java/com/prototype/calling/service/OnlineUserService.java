package com.prototype.calling.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which users are currently online.
 * Uses ConcurrentHashMap for thread safety.
 */
@Service
public class OnlineUserService {

    // Thread-safe set of online usernames
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public void registerUser(String username) {
        onlineUsers.add(username);
    }

    public void removeUser(String username) {
        onlineUsers.remove(username);
    }

    public boolean isOnline(String username) {
        return onlineUsers.contains(username);
    }

    public List<String> getOnlineUsers() {
        return new ArrayList<>(onlineUsers);
    }
}