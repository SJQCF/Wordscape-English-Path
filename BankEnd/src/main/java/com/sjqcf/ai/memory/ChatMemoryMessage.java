package com.sjqcf.ai.memory;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话记忆持久化实体，一条记录对应一条对话消息。
 */
@Data
public class ChatMemoryMessage {

    /** 主键，自增，同时用于保证消息先后顺序。 */
    private Long id;

    /** 会话 ID，对应 ChatMemory 的 conversationId（前端 sessionId）。 */
    private String conversationId;

    /** 消息类型：USER / ASSISTANT / SYSTEM / TOOL。 */
    private String messageType;

    /** 消息文本内容。 */
    private String content;

    /** 入库时间。 */
    private LocalDateTime createdAt;
}
