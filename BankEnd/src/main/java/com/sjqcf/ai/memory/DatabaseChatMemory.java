package com.sjqcf.ai.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于数据库的 {@link ChatMemory} 实现。
 *
 * <p>写入策略：只追加、不覆盖，因此数据库保留完整对话历史；
 * 读取策略：每次最多读取最近若干条消息，并把较早对话的滚动摘要放在消息之前。</p>
 * 由于消息落库，服务重启后仍能按 sessionId 恢复上下文。</p>
 */
public class DatabaseChatMemory implements ChatMemory {

    private static final Logger log = LoggerFactory.getLogger(DatabaseChatMemory.class);

    private final ChatMemoryMapper chatMemoryMapper;
    private final ChatConversationMapper conversationMapper;
    private final int maxMessages;

    public DatabaseChatMemory(ChatMemoryMapper chatMemoryMapper,
                              ChatConversationMapper conversationMapper,
                              int maxMessages) {
        this.chatMemoryMapper = chatMemoryMapper;
        this.conversationMapper = conversationMapper;
        this.maxMessages = Math.max(maxMessages, 1);
    }

    @Override
    @Transactional
    public void add(String conversationId, List<Message> messages) {
        if (conversationId == null || conversationId.isBlank() || messages == null || messages.isEmpty()) {
            return;
        }

        List<ChatMemoryMessage> rows = new ArrayList<>(messages.size());
        for (Message message : messages) {
            ChatMemoryMessage row = toRow(conversationId, message);
            if (row != null) {
                rows.add(row);
            }
        }

        if (!rows.isEmpty()) {
            chatMemoryMapper.insertMessages(rows);
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return List.of();
        }

        ChatConversation conversation = conversationMapper.findBySessionId(conversationId);
        if (conversation == null) {
            return List.of();
        }

        String summary = conversation.getSummary();
        long summarizedThroughId = conversation.getSummarizedThroughId() == null
                ? 0L : conversation.getSummarizedThroughId();
        boolean hasSummary = summary != null && !summary.isBlank();
        int recentMessageLimit = hasSummary ? Math.max(maxMessages - 1, 0) : maxMessages;

        List<ChatMemoryMessage> rows = chatMemoryMapper.findRecentMessages(
                conversationId, summarizedThroughId, recentMessageLimit);

        List<Message> messages = new ArrayList<>();
        if (hasSummary) {
            String summaryPrompt = "以下是更早对话的摘要，仅用于理解上下文：\n" + summary;
            messages.add(new SystemMessage(summaryPrompt));
        }

        if (rows.isEmpty()) {
            return messages;
        }

        List<ChatMemoryMessage> selected = new ArrayList<>(rows);
        if (selected.size() > recentMessageLimit) {
            selected = new ArrayList<>(selected.subList(
                    selected.size() - recentMessageLimit, selected.size()));
        }
        // 条数边界刚好切到助手回复时，不把缺少对应提问的孤立的助手消息放进上下文。
        while (!selected.isEmpty()
                && MessageType.ASSISTANT.name().equals(selected.get(0).getMessageType())) {
            selected.remove(0);
        }

        for (ChatMemoryMessage row : selected) {
            Message message = toMessage(row);
            if (message != null) {
                messages.add(message);
            }
        }
        return messages;
    }

    @Override
    @Transactional
    public void clear(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return;
        }
        chatMemoryMapper.deleteByConversationId(conversationId);
    }

    private ChatMemoryMessage toRow(String conversationId, Message message) {
        if (message == null || message.getMessageType() == null) {
            return null;
        }
        ChatMemoryMessage row = new ChatMemoryMessage();
        row.setConversationId(conversationId);
        row.setMessageType(message.getMessageType().name());
        row.setContent(message.getText() == null ? "" : message.getText());
        return row;
    }

    private Message toMessage(ChatMemoryMessage row) {
        MessageType type;
        try {
            type = MessageType.valueOf(row.getMessageType());
        } catch (IllegalArgumentException | NullPointerException e) {
            log.warn("跳过未知的对话记忆消息类型: {}, conversationId: {}",
                    row.getMessageType(), row.getConversationId());
            return null;
        }

        String text = row.getContent() == null ? "" : row.getContent();
        return switch (type) {
            case USER -> new UserMessage(text);
            case ASSISTANT -> new AssistantMessage(text);
            case SYSTEM -> new SystemMessage(text);
            case TOOL -> {
                // 工具调用消息带有结构化的响应体，纯文本无法无损还原，这里跳过。
                // 本项目的 Agent 记忆只保存 USER / ASSISTANT 文本消息，不涉及该分支。
                log.warn("对话记忆中暂不支持还原 TOOL 消息，已跳过, conversationId: {}", row.getConversationId());
                yield null;
            }
        };
    }
}
