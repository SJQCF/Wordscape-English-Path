package com.sjqcf.utils.AIVoice;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 将火山下行 PCM 转发到浏览器 WebSocket。
 */
public final class VoiceRelay {

    private static final ConcurrentHashMap<String, WebSocketSession> DOWNSTREAM = new ConcurrentHashMap<>();

    private VoiceRelay() {
    }

    public static void registerDownstream(String restSessionId, WebSocketSession session) {
        if (restSessionId != null && session != null) {
            DOWNSTREAM.put(restSessionId, session);
        }
    }

    public static void unregisterDownstream(String restSessionId) {
        if (restSessionId != null) {
            DOWNSTREAM.remove(restSessionId);
        }
    }

    public static void sendDownstream(String restSessionId, byte[] pcmS16le) {
        if (restSessionId == null || pcmS16le == null || pcmS16le.length == 0) {
            return;
        }
        WebSocketSession session = DOWNSTREAM.get(restSessionId);
        if (session == null || !session.isOpen()) {
            return;
        }
        synchronized (session) {
            try {
                session.sendMessage(new BinaryMessage(ByteBuffer.wrap(pcmS16le)));
            } catch (Exception e) {
                DOWNSTREAM.remove(restSessionId, session);
            }
        }
    }
}
