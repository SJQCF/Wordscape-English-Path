package com.sjqcf.service.impl;

import com.github.pagehelper.PageHelper;
import com.sjqcf.mapper.AchievementMapper;
import com.sjqcf.pojo.Achievement;
import com.sjqcf.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.sjqcf.utils.SystemConstants.PAGE_SIZE;

@Service
public class AchievementServiceImpl implements AchievementService {

    @Autowired
    private AchievementMapper achievementMapper;

    @Override
    public List<Achievement> getAchievementList(Integer pageNum) {
        PageHelper.startPage(pageNum,PAGE_SIZE);
        return achievementMapper.getAchievementList(pageNum);
    }

    @Override
    public List<Integer> getUserAchievement(Integer userId) {
        return achievementMapper.getUserAchievement(userId);
    }

    @Override
    public LocalDateTime getUserAchievementTime(Integer userId, Integer achievementId) {
        return achievementMapper.getUserAchievementTime(userId, achievementId);
    }

    @Override
    public void getAchievement(Integer userId, Integer achievementId) {
        LocalDateTime time = LocalDateTime.now();
        achievementMapper.insert(userId, achievementId, time);
    }

    @Override
    public Boolean checkAchievement(Integer userId, Integer achievementId) {
        return achievementMapper.checkAchievement(userId, achievementId) > 0;
    }
}
