package com.sjqcf.ai;

import com.sjqcf.ai.memory.ChatConversation;
import com.sjqcf.ai.memory.ChatConversationMapper;
import com.sjqcf.ai.memory.ChatMemoryMapper;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentConversationServiceTest {

    @Test
    void rejectsSessionOwnedByAnotherUserBeforeChatStarts() {
        ChatConversationMapper conversationMapper = mock(ChatConversationMapper.class);
        ChatConversation existing = new ChatConversation();
        existing.setSessionId("session-1");
        existing.setUserId(10);
        when(conversationMapper.findBySessionId("session-1")).thenReturn(existing);

        AgentConversationService service = service(conversationMapper);

        assertThrows(IllegalArgumentException.class,
                () -> service.prepareChat("session-1", 20, "hello"));
    }

    @Test
    void allowsSessionOwnerToContinueChat() {
        ChatConversationMapper conversationMapper = mock(ChatConversationMapper.class);
        ChatConversation existing = new ChatConversation();
        existing.setSessionId("session-1");
        existing.setUserId(10);
        when(conversationMapper.findBySessionId("session-1")).thenReturn(existing);

        AgentConversationService service = service(conversationMapper);

        assertDoesNotThrow(() -> service.prepareChat("session-1", 10, "hello"));
    }

    private AgentConversationService service(ChatConversationMapper conversationMapper) {
        return new AgentConversationService(
                conversationMapper,
                mock(ChatMemoryMapper.class),
                mock(ChatMemory.class),
                mock(ConversationSummaryService.class));
    }
}
