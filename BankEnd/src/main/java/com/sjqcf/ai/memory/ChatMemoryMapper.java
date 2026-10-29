package com.sjqcf.ai.memory;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMemoryMapper {

    /**
     * 追加写入一批消息（只新增，不覆盖历史）。
     */
    int insertMessages(@Param("messages") List<ChatMemoryMessage> messages);

    /**
     * 查询指定消息 ID 之后最近的 limit 条消息，返回结果按时间正序排列。
     */
    List<ChatMemoryMessage> findRecentMessages(@Param("conversationId") String conversationId,
                                               @Param("afterId") long afterId,
                                               @Param("limit") int limit);

    /**
     * 按时间正序查询指定消息 ID 之后的一批消息，用于生成滚动摘要。
     */
    List<ChatMemoryMessage> findMessagesAfterId(@Param("conversationId") String conversationId,
                                                @Param("afterId") long afterId,
                                                @Param("limit") int limit);

    /**
     * 查询某个会话的全部消息（按时间正序），用于历史对话回看。
     */
    List<ChatMemoryMessage> findAllMessages(@Param("conversationId") String conversationId);

    /**
     * 清空指定会话的全部消息。
     */
    int deleteByConversationId(@Param("conversationId") String conversationId);

    /**
     * 查询所有会话 ID。
     */
    List<String> findConversationIds();
}
