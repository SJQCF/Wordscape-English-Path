-- 已有数据库升级脚本：执行一次。
-- 新建数据库直接执行 ai_chat_conversation.sql 和 ai_chat_memory.sql 即可，无需重复执行本文件。

ALTER TABLE ai_chat_conversation
    ADD COLUMN summary LONGTEXT NULL COMMENT '较早对话的滚动摘要' AFTER title,
    ADD COLUMN summarized_through_id BIGINT NOT NULL DEFAULT 0
        COMMENT '已写入摘要的最后一条消息 ID' AFTER summary;
