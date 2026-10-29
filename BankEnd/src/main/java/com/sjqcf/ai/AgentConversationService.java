package com.sjqcf.ai;

import com.sjqcf.ai.memory.ChatConversation;
import com.sjqcf.ai.memory.ChatConversationMapper;
import com.sjqcf.ai.memory.ChatMemoryMapper;
import com.sjqcf.ai.memory.ChatMemoryMessage;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 历史会话管理。
 *
 * <p>{@link ChatMemory} 只认识 sessionId，不知道消息属于谁，所以用户归属单独记在
 * {@code ai_chat_conversation} 表里：控制器从 JWT 拿到 userId，在这里建立
 * sessionId 与用户的关联，并按用户维度提供列表、读取和删除。</p>
 */
@Service
public class AgentConversationService {

    private static final int MAX_TITLE_LENGTH = 30;
    private static final int MAX_SESSION_ID_LENGTH = 128;

    private final ChatConversationMapper conversationMapper;
    private final ChatMemoryMapper chatMemoryMapper;
    private final ChatMemory chatMemory;
    private final ConversationSummaryService summaryService;

    public AgentConversationService(ChatConversationMapper conversationMapper,
                                    ChatMemoryMapper chatMemoryMapper,
                                    ChatMemory chatMemory,
                                    ConversationSummaryService summaryService) {
        this.conversationMapper = conversationMapper;
        this.chatMemoryMapper = chatMemoryMapper;
        this.chatMemory = chatMemory;
        this.summaryService = summaryService;
    }

    /**
     * 在调用模型前建立或验证会话归属。
     *
     * <p>如果 sessionId 已属于其他用户，这里会直接拒绝，消息不会写入该会话。</p>
     */
    public void prepareChat(String sessionId, Integer userId, String firstMessage) {
        if (sessionId == null || sessionId.isBlank() || userId == null) {
            throw new IllegalArgumentException("会话或用户信息不能为空");
        }
        if (sessionId.length() > MAX_SESSION_ID_LENGTH) {
            throw new IllegalArgumentException("sessionId 长度不能超过 " + MAX_SESSION_ID_LENGTH);
        }

        conversationMapper.upsert(sessionId, userId, buildTitle(firstMessage));
        requireOwned(sessionId, userId);
    }

    /**
     * 某个用户的历史会话列表，最近活跃的在前。
     */
    public List<ChatConversation> list(Integer userId) {
        return userId == null ? List.of() : conversationMapper.findByUserId(userId);
    }

    /**
     * 读取某个会话的全部消息，并校验该会话确实属于当前用户。
     */
    public List<ChatMemoryMessage> messages(String sessionId, Integer userId) {
        requireOwned(sessionId, userId);
        return chatMemoryMapper.findAllMessages(sessionId);
    }

    /**
     * 一轮对话完成后尝试压缩较早历史。失败不会影响已经生成的回答。
     */
    public void compactIfNeeded(String sessionId, Integer userId) {
        ChatConversation conversation = requireOwned(sessionId, userId);
        summaryService.compactIfNeeded(conversation);
    }

    /**
     * 删除会话：同时清掉消息记录和会话索引。
     */
    @Transactional
    public void delete(String sessionId, Integer userId) {
        requireOwned(sessionId, userId);
        chatMemory.clear(sessionId);
        conversationMapper.deleteBySessionId(sessionId);
    }

    /**
     * 会话必须存在且属于当前用户，否则不允许读取或删除。
     */
    private ChatConversation requireOwned(String sessionId, Integer userId) {
        if (sessionId == null || sessionId.isBlank() || userId == null) {
            throw new IllegalArgumentException("会话不存在或无权访问");
        }
        ChatConversation conversation = conversationMapper.findBySessionId(sessionId);
        if (conversation == null || !userId.equals(conversation.getUserId())) {
            throw new IllegalArgumentException("会话不存在或无权访问");
        }
        return conversation;
    }

    private String buildTitle(String message) {
        if (message == null || message.isBlank()) {
            return "新对话";
        }
        String title = message.strip().replaceAll("\\s+", " ");
        if (title.isEmpty()) {
            return "新对话";
        }
        return title.length() <= MAX_TITLE_LENGTH
                ? title
                : title.substring(0, MAX_TITLE_LENGTH) + "…";
    }
}
