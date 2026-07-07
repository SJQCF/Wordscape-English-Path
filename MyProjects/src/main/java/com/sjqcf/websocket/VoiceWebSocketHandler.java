package com.sjqcf.websocket;

import com.sjqcf.service.VoiceSessionRegistry;
import com.sjqcf.utils.AIVoice.CallManager;
import com.sjqcf.utils.AIVoice.VoiceRelay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class VoiceWebSocketHandler extends BinaryWebSocketHandler {

    private static final String ATTR_FIRST_LOG = "VOICE_WS_FIRST_BINARY_LOGGED";

    private final VoiceSessionRegistry voiceSessionRegistry;

    public VoiceWebSocketHandler(VoiceSessionRegistry voiceSessionRegistry) {
        this.voiceSessionRegistry = voiceSessionRegistry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sid = (String) session.getAttributes().get("voiceSessionId");
        VoiceRelay.registerDownstream(sid, session);
        session.getAttributes().put(ATTR_FIRST_LOG, new AtomicBoolean(false));
        log.info("语音浏览器 WebSocket 已连接, sessionId={}, wsId={}", sid, session.getId());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        String sid = (String) session.getAttributes().get("voiceSessionId");
        CallManager cm = voiceSessionRegistry.get(sid);
        if (cm == null) {
            log.warn("收到浏览器 PCM 但会话已不存在: sessionId={}", sid);
            return;
        }
        ByteBuffer buf = message.getPayload();
        byte[] arr = new byte[buf.remaining()];
        buf.get(arr);
        AtomicBoolean first = (AtomicBoolean) session.getAttributes().get(ATTR_FIRST_LOG);
        if (first != null && first.compareAndSet(false, true)) {
            log.info("收到浏览器首帧 PCM, sessionId={}, 字节={}", sid, arr.length);
        }
        cm.enqueueBrowserAudio(arr);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sid = (String) session.getAttributes().get("voiceSessionId");
        log.info("语音浏览器 WebSocket 关闭, sessionId={}, code={}, reason={}", sid, status.getCode(), status.getReason());
        VoiceRelay.unregisterDownstream(sid);
    }
}
