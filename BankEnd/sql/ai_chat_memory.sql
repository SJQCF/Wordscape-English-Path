-- AI Agent 对话记忆持久化表
-- 一条记录对应一条对话消息，conversation_id 对应 ChatMemory 的会话 ID（前端 sessionId）。
-- 数据库保留完整对话历史，Agent 每次只读取最近 N 条（app.ai.agent.memory-size）喂给模型。
CREATE TABLE IF NOT EXISTS ai_chat_memory
(
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键，自增，同时用于保证消息先后顺序',
    conversation_id VARCHAR(128) NOT NULL COMMENT '会话 ID，对应 ChatMemory 的 conversationId',
    message_type    VARCHAR(32)  NOT NULL COMMENT '消息类型：USER / ASSISTANT / SYSTEM / TOOL',
    content         LONGTEXT COMMENT '消息文本内容',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
    PRIMARY KEY (id),
    KEY idx_conversation_id (conversation_id, id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'AI Agent 对话记忆表';
