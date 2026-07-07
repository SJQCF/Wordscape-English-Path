package com.sjqcf.config;

import com.sjqcf.websocket.VoiceHandshakeInterceptor;
import com.sjqcf.websocket.VoiceWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private VoiceWebSocketHandler voiceWebSocketHandler;

    @Autowired
    private VoiceHandshakeInterceptor voiceHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(voiceWebSocketHandler, "/ws/voice")
                .addInterceptors(voiceHandshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
