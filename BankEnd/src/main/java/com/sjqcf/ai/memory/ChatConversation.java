package com.sjqcf.ai.memory;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI Agent 会话索引，一条记录对应一个 session_id，并记录它属于哪个用户。
 */
@Data
public class ChatConversation {

    private Long id;

    /** 会话 ID，对应 ChatMemory 的 conversationId。 */
    private String sessionId;

    /** 所属用户 ID。 */
    private Integer userId;

    /** 会话标题，取首条用户消息。 */
    private String title;

    /** 较早对话的滚动摘要。 */
    private String summary;

    /** 已经写入摘要的最后一条消息 ID。 */
    private Long summarizedThroughId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
