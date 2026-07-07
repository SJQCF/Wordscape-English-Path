package com.sjqcf.service;

import com.sjqcf.utils.AIVoice.CallManager;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VoiceSessionRegistry {

    private final ConcurrentHashMap<String, CallManager> activeSessions = new ConcurrentHashMap<>();

    public void put(String sessionId, CallManager callManager) {
        activeSessions.put(sessionId, callManager);
    }

    public CallManager get(String sessionId) {
        return activeSessions.get(sessionId);
    }

    public CallManager remove(String sessionId) {
        return activeSessions.remove(sessionId);
    }

    public int size() {
        return activeSessions.size();
    }

    public Set<String> sessionIds() {
        return activeSessions.keySet();
    }

    public Map<String, CallManager> snapshot() {
        return Map.copyOf(activeSessions);
    }
}
