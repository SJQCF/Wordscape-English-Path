package com.sjqcf.config;

import com.sjqcf.ai.AiAgentProperties;
import com.sjqcf.ai.memory.ChatConversationMapper;
import com.sjqcf.ai.memory.ChatMemoryMapper;
import com.sjqcf.ai.memory.DatabaseChatMemory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    /**
     * 对话记忆改为数据库持久化：消息写入 ai_chat_memory 表，服务重启后可按 sessionId 恢复上下文。
     * 数据库保留完整历史，每次按消息条数读取最近消息，并附带较早对话的滚动摘要。
     */
    @Bean
    public ChatMemory learningChatMemory(AiAgentProperties properties,
                                         ChatMemoryMapper chatMemoryMapper,
                                         ChatConversationMapper conversationMapper) {
        return new DatabaseChatMemory(
                chatMemoryMapper,
                conversationMapper,
                properties.getMemorySize());
    }
}
