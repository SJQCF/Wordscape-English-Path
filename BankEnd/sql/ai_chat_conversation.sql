-- AI Agent 会话索引表
-- 一个 session_id 对应一条会话记录，用于按用户列出历史对话。
-- ai_chat_memory 表只按 session_id 存消息，本身不知道用户是谁；
-- 用户归属放在这张表里，由控制器层从 JWT 取 userId 后写入。
CREATE TABLE IF NOT EXISTS ai_chat_conversation
(
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    session_id VARCHAR(128) NOT NULL COMMENT '会话 ID，对应 ChatMemory 的 conversationId',
    user_id    INT          NOT NULL COMMENT '所属用户 ID',
    title      VARCHAR(255) NOT NULL COMMENT '会话标题，取首条用户消息',
    summary    LONGTEXT     NULL COMMENT '较早对话的滚动摘要',
    summarized_through_id BIGINT NOT NULL DEFAULT 0 COMMENT '已写入摘要的最后一条消息 ID',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_session_id (session_id),
    KEY idx_user_updated (user_id, updated_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'AI Agent 会话索引表';
