package com.example.demo.model;

import java.util.EnumMap;
import java.util.Map;

public class UserSessionIndex {

    private final Map<LoginType, String> sessions = new EnumMap<>(LoginType.class);

    public void bind(LoginType type, String sessionId) {
        sessions.put(type, sessionId);
    }

    public String get(LoginType type) {
        return sessions.get(type);
    }

    public void remove(LoginType type) {
        sessions.remove(type);
    }

    public Map<LoginType, String> all() {
        return sessions;
    }
}
