package com.sjqcf.ai;

import com.sjqcf.ai.memory.ChatConversation;
import com.sjqcf.ai.memory.ChatConversationMapper;
import com.sjqcf.ai.memory.ChatMemoryMapper;
import com.sjqcf.ai.memory.ChatMemoryMessage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ConversationSummaryServiceTest {

    @Test
    void doesNotSummarizeBelowMessageThreshold() {
        ChatConversationMapper conversationMapper = mock(ChatConversationMapper.class);
        ChatMemoryMapper memoryMapper = mock(ChatMemoryMapper.class);
        ChatClientRegistry registry = mock(ChatClientRegistry.class);

        AiAgentProperties properties = new AiAgentProperties();
        properties.setSummaryTriggerMessages(40);
        properties.setSummaryKeepMessages(10);
        properties.setSummaryBatchSize(100);

        ChatConversation conversation = new ChatConversation();
        conversation.setSessionId("session-1");
        conversation.setSummarizedThroughId(0L);

        List<ChatMemoryMessage> rows = new ArrayList<>();
        for (int i = 1; i < 40; i++) {
            ChatMemoryMessage message = new ChatMemoryMessage();
            message.setId((long) i);
            message.setConversationId("session-1");
            message.setMessageType(i % 2 == 0 ? "ASSISTANT" : "USER");
            message.setContent("message-" + i);
            rows.add(message);
        }
        when(memoryMapper.findMessagesAfterId("session-1", 0L, 100)).thenReturn(rows);

        ConversationSummaryService service = new ConversationSummaryService(
                conversationMapper, memoryMapper, registry, properties);
        service.compactIfNeeded(conversation);

        verifyNoInteractions(registry);
        verifyNoInteractions(conversationMapper);
    }
}
