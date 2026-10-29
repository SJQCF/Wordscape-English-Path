package com.sjqcf.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LearningAgentService {

    private static final Logger log = LoggerFactory.getLogger(LearningAgentService.class);

    private final ChatClientRegistry chatClientRegistry;
    private final AiAgentProperties properties;
    private final EnglishLearningTools learningTools;
    private final ChatMemory chatMemory;
    private final AgentConversationService conversationService;
    private final Map<String, ChatClient> agentClients = new ConcurrentHashMap<>();

    public LearningAgentService(ChatClientRegistry chatClientRegistry,
                                AiAgentProperties properties,
                                EnglishLearningTools learningTools,
                                ChatMemory chatMemory,
                                AgentConversationService conversationService) {
        this.chatClientRegistry = chatClientRegistry;
        this.properties = properties;
        this.learningTools = learningTools;
        this.chatMemory = chatMemory;
        this.conversationService = conversationService;
    }

    public String chat(String sessionId, Integer userId, String message) {
        return chat(sessionId, userId, message, null, null);
    }

    public String chat(String sessionId, Integer userId, String message, String providerId) {
        return chat(sessionId, userId, message, providerId, null);
    }

    public String chat(String sessionId, Integer userId, String message,
                       String providerId, Boolean deepThinking) {
        conversationService.prepareChat(sessionId, userId, message);

        ChatClientRegistry.ProviderRuntime runtime = chatClientRegistry.requireRuntime(providerId);
        if (!runtime.capabilities().contains(AiProperties.Capability.TOOLS)) {
            throw new IllegalArgumentException(
                    "AI [" + runtime.displayName() + "] 不支持工具调用，无法用于学习规划 Agent");
        }

        var options = chatClientRegistry.chatOptionsBuilder(runtime.id(), new AiRequestOptions(deepThinking));
        String content = agentClient(runtime.id()).prompt()
                .options(options)
                .user(message)
                .tools(learningTools)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();

        if (content == null || content.isBlank()) {
            throw new IllegalStateException("AI Agent 未返回有效内容");
        }
        try {
            conversationService.compactIfNeeded(sessionId, userId);
        } catch (Exception e) {
            // 摘要属于后处理，失败不能覆盖已经成功生成的回答。
            log.warn("Agent 对话摘要更新失败，sessionId: {}, reason: {}", sessionId, e.getMessage());
        }
        return content;
    }

    private ChatClient agentClient(String providerId) {
        return agentClients.computeIfAbsent(providerId, id -> chatClientRegistry.chat(id)
                .mutate()
                .defaultSystem(properties.getSystemPrompt())
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build());
    }
}
