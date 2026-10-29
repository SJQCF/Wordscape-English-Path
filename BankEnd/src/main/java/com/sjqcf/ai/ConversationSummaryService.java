package com.sjqcf.ai;

import com.sjqcf.ai.memory.ChatConversation;
import com.sjqcf.ai.memory.ChatConversationMapper;
import com.sjqcf.ai.memory.ChatMemoryMapper;
import com.sjqcf.ai.memory.ChatMemoryMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 将较早的对话压缩成滚动摘要。完整消息仍保留在数据库中。
 */
@Slf4j
@Service
public class ConversationSummaryService {

    private final ChatConversationMapper conversationMapper;
    private final ChatMemoryMapper chatMemoryMapper;
    private final ChatClientRegistry chatClientRegistry;
    private final AiAgentProperties properties;

    public ConversationSummaryService(ChatConversationMapper conversationMapper,
                                      ChatMemoryMapper chatMemoryMapper,
                                      ChatClientRegistry chatClientRegistry,
                                      AiAgentProperties properties) {
        this.conversationMapper = conversationMapper;
        this.chatMemoryMapper = chatMemoryMapper;
        this.chatClientRegistry = chatClientRegistry;
        this.properties = properties;
    }

    /**
     * 未摘要消息达到阈值时，压缩除最近若干条之外的旧消息。
     */
    public void compactIfNeeded(ChatConversation conversation) {
        long summarizedThroughId = conversation.getSummarizedThroughId() == null
                ? 0L : conversation.getSummarizedThroughId();
        int triggerMessages = Math.max(properties.getSummaryTriggerMessages(), 1);
        int keepMessages = Math.max(properties.getSummaryKeepMessages(), 2);
        int batchSize = Math.max(properties.getSummaryBatchSize(), 1);
        List<ChatMemoryMessage> rows = chatMemoryMapper.findMessagesAfterId(
                conversation.getSessionId(),
                summarizedThroughId,
                Math.max(batchSize, triggerMessages + keepMessages));

        if (rows.size() <= keepMessages) {
            return;
        }

        if (rows.size() < triggerMessages) {
            return;
        }

        int cutoff = Math.min(rows.size() - keepMessages, batchSize);
        List<ChatMemoryMessage> messagesToSummarize = rows.subList(0, cutoff);
        String newSummary = generateSummary(conversation.getSummary(), messagesToSummarize);
        Long throughId = messagesToSummarize.get(messagesToSummarize.size() - 1).getId();

        int updated = conversationMapper.updateSummary(
                conversation.getSessionId(),
                newSummary,
                throughId,
                summarizedThroughId);
        if (updated == 0) {
            log.info("会话摘要已被其他请求更新，跳过本次写入, sessionId: {}",
                    conversation.getSessionId());
        }
    }

    private String generateSummary(String existingSummary, List<ChatMemoryMessage> messages) {
        StringBuilder prompt = new StringBuilder();
        if (existingSummary != null && !existingSummary.isBlank()) {
            prompt.append("已有摘要：\n")
                    .append(existingSummary)
                    .append("\n\n");
        }
        prompt.append("新增对话：\n");
        for (ChatMemoryMessage message : messages) {
            if (!isTextConversationMessage(message)) {
                continue;
            }
            prompt.append(message.getMessageType())
                    .append(": ")
                    .append(message.getContent() == null ? "" : message.getContent())
                    .append('\n');
        }

        String summary = chatClientRegistry.chat().prompt()
                .system("""
                        你负责压缩英语学习对话。请用中文生成简洁、连续的摘要，
                        保留用户的学习目标、当前水平、偏好、已学内容、未完成任务和重要约束。
                        不要回答问题，不要编造未出现的信息，只输出更新后的摘要。
                        """)
                .user(prompt.toString())
                .call()
                .content();

        if (summary == null || summary.isBlank()) {
            throw new IllegalStateException("对话摘要生成结果为空");
        }
        return limitLength(summary.strip(), Math.max(properties.getMaxSummaryChars(), 500));
    }

    private boolean isTextConversationMessage(ChatMemoryMessage message) {
        return "USER".equals(message.getMessageType())
                || "ASSISTANT".equals(message.getMessageType());
    }

    private String limitLength(String text, int maxChars) {
        return text.length() <= maxChars ? text : text.substring(0, maxChars);
    }
}
