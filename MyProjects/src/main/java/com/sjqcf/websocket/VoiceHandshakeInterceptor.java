package com.sjqcf.websocket;

import com.sjqcf.service.VoiceSessionRegistry;
import com.sjqcf.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class VoiceHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private VoiceSessionRegistry voiceSessionRegistry;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        URI uri = request.getURI();
        Map<String, String> query = parseQuery(uri.getRawQuery());
        String token = query.get("token");
        String sessionId = query.get("sessionId");
        if (token == null || sessionId == null) {
            log.warn("语音 WebSocket 握手缺少 token 或 sessionId");
            return false;
        }
        try {
            JwtUtils.parseJwt(token);
        } catch (Exception e) {
            log.warn("语音 WebSocket token 无效");
            return false;
        }
        if (voiceSessionRegistry.get(sessionId) == null) {
            log.warn("语音 WebSocket 会话不存在或未就绪: {}", sessionId);
            return false;
        }
        attributes.put("voiceSessionId", sessionId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> map = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return map;
        }
        for (String pair : rawQuery.split("&")) {
            int eq = pair.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = pair.substring(0, eq);
            String val = pair.substring(eq + 1);
            map.put(key, URLDecoder.decode(val, StandardCharsets.UTF_8));
        }
        return map;
    }
}
