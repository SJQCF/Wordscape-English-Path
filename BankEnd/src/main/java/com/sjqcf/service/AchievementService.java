package com.sjqcf.service;

import com.sjqcf.pojo.Achievement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AchievementService {
    List<Achievement> getAchievementList(Integer pageNum);

    List<Integer> getUserAchievement(Integer userId);

    LocalDateTime getUserAchievementTime(Integer userId, Integer achievementId);

    void getAchievement(Integer userId, Integer achievementId);

    Boolean checkAchievement(Integer userId, Integer achievementId);
}
