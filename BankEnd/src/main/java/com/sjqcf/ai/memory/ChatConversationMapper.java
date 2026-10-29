package com.sjqcf.ai.memory;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatConversationMapper {

    /**
     * 首次出现时插入会话，已存在则只刷新最后活跃时间（标题保持为首次的值）。
     */
    int upsert(@Param("sessionId") String sessionId,
               @Param("userId") Integer userId,
               @Param("title") String title);

    /**
     * 某个用户的历史会话，按最后活跃时间倒序。
     */
    List<ChatConversation> findByUserId(@Param("userId") Integer userId);

    ChatConversation findBySessionId(@Param("sessionId") String sessionId);

    /**
     * 仅在摘要版本仍匹配时更新，避免并发摘要互相覆盖。
     */
    int updateSummary(@Param("sessionId") String sessionId,
                      @Param("summary") String summary,
                      @Param("summarizedThroughId") Long summarizedThroughId,
                      @Param("expectedThroughId") Long expectedThroughId);

    int deleteBySessionId(@Param("sessionId") String sessionId);
}
