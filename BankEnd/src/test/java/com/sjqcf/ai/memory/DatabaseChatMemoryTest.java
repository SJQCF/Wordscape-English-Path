package com.sjqcf.ai.memory;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseChatMemoryTest {

    @Test
    void includesSummaryAndRecentMessagesAfterSummarizedId() {
        ChatMemoryMapper memoryMapper = mock(ChatMemoryMapper.class);
        ChatConversationMapper conversationMapper = mock(ChatConversationMapper.class);
        ChatConversation conversation = conversation("session-1", "用户正在练习过去时", 42L);
        when(conversationMapper.findBySessionId("session-1")).thenReturn(conversation);
        when(memoryMapper.findRecentMessages("session-1", 42L, 19))
                .thenReturn(List.of(
                        row(43L, MessageType.USER, "先练什么？"),
                        row(44L, MessageType.ASSISTANT, "先练一般过去时。")));

        DatabaseChatMemory memory = new DatabaseChatMemory(memoryMapper, conversationMapper, 20);
        List<Message> messages = memory.get("session-1");

        assertEquals(3, messages.size());
        assertEquals(MessageType.SYSTEM, messages.get(0).getMessageType());
        assertTrue(messages.get(0).getText().contains("用户正在练习过去时"));
        assertEquals(MessageType.USER, messages.get(1).getMessageType());
        assertEquals(MessageType.ASSISTANT, messages.get(2).getMessageType());
        verify(memoryMapper).findRecentMessages("session-1", 42L, 19);
    }

    @Test
    void limitsRecentMessagesByMessageCountIncludingSummary() {
        ChatMemoryMapper memoryMapper = mock(ChatMemoryMapper.class);
        ChatConversationMapper conversationMapper = mock(ChatConversationMapper.class);
        when(conversationMapper.findBySessionId("session-1"))
                .thenReturn(conversation("session-1", "旧对话摘要", 0L));
        when(memoryMapper.findRecentMessages("session-1", 0L, 2))
                .thenReturn(List.of(
                        row(1L, MessageType.USER, "第一条"),
                        row(2L, MessageType.ASSISTANT, "第二条"),
                        row(3L, MessageType.USER, "第三条"),
                        row(4L, MessageType.ASSISTANT, "第四条")));

        DatabaseChatMemory memory = new DatabaseChatMemory(memoryMapper, conversationMapper, 3);
        List<Message> messages = memory.get("session-1");

        assertEquals(3, messages.size());
        assertEquals(MessageType.SYSTEM, messages.get(0).getMessageType());
        assertEquals("第三条", messages.get(1).getText());
        assertEquals("第四条", messages.get(2).getText());
    }

    private ChatConversation conversation(String sessionId, String summary, long summarizedThroughId) {
        ChatConversation conversation = new ChatConversation();
        conversation.setSessionId(sessionId);
        conversation.setSummary(summary);
        conversation.setSummarizedThroughId(summarizedThroughId);
        return conversation;
    }

    private ChatMemoryMessage row(long id, MessageType type, String content) {
        ChatMemoryMessage message = new ChatMemoryMessage();
        message.setId(id);
        message.setConversationId("session-1");
        message.setMessageType(type.name());
        message.setContent(content);
        return message;
    }
}
