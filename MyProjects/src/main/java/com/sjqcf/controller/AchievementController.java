package com.sjqcf.controller;

import com.sjqcf.pojo.Result;
import com.sjqcf.service.AchievementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/achievement")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    /**
     * 获取所有成就
     */
    @GetMapping("/list")
    public Result getAchievementList(Integer pageNum) {
        log.info("获取所有成就");
        return Result.success(achievementService.getAchievementList(pageNum));
    }

    /**
     * 获取用户已获取成就
     */
    @GetMapping("/getUserAchievement/{userId}")
    public Result getUserAchievement(@PathVariable Integer userId) {
        log.info("获取用户已获取成就");
        return Result.success(achievementService.getUserAchievement(userId));
    }

    /**
     * 获取用户获取成就时间
     */
    @GetMapping("/getUserAchievementTime/{userId}")
    public Result getUserAchievementTime(@PathVariable Integer userId, @RequestParam Integer achievementId) {
        log.info("获取用户已获取成就时间");
        return Result.success(achievementService.getUserAchievementTime(userId, achievementId));
    }

    /**
     * 用户获取成就
     */
    @PostMapping("/getAchievement/{userId}")
    public Result getAchievement(@PathVariable Integer userId, @RequestParam Integer achievementId) {
        log.info("用户获取成就");
        achievementService.getAchievement(userId, achievementId);
        return Result.success();
    }

    /**
     * 检查用户是否获取过此成就
     */
    @GetMapping("/checkAchievement/{userId}")
    public Result checkAchievement(@PathVariable Integer userId, @RequestParam Integer achievementId) {
        log.info("检查用户是否获取过此成就");
        return Result.success(achievementService.checkAchievement(userId, achievementId));
    }
}
