package com.sjqcf.mapper;

import com.sjqcf.pojo.Achievement;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AchievementMapper {
    List<Achievement> getAchievementList(Integer pageNum);

    List<Integer> getUserAchievement(Integer userId);

    LocalDateTime getUserAchievementTime(Integer userId, Integer achievementId);

    void insert(Integer userId, Integer achievementId, LocalDateTime time);

    Integer checkAchievement(Integer userId, Integer achievementId);
}
