package com.sjqcf.controller;

import com.sjqcf.pojo.Result;
import com.sjqcf.pojo.Word;
import com.sjqcf.pojo.WordCheck;
import com.sjqcf.pojo.WordCheckReturn;
import com.sjqcf.service.AchievementService;
import com.sjqcf.service.StudyService;
import com.sjqcf.service.UserService;
import com.sjqcf.service.WordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/study")
public class StudyController {

    @Autowired
    private WordService wordService;

    @Autowired
    private StudyService studyService;

    @Autowired
    private UserService userService;

    @Autowired
    private AchievementService achievementService;

    /**
     * 检查单词或中文是否输入正确
     */
    @PostMapping("/checkWord/{userId}")
    public Result checkWord(@PathVariable Integer userId, @RequestParam String userInput, @RequestParam String correctAnswer, @RequestParam Integer wordId) {
        log.info("检查单词: 用户输入={}, 正确答案={}", userInput, correctAnswer);
        WordCheckReturn wordCheckReturn = wordService.checkWordAnswer(new WordCheck(userInput, correctAnswer, wordId));
        return Result.success(wordCheckReturn);
    }

    /**
     * 获取单词选择题（一个单词，四个选项）
     */
    @PostMapping("/wordChoice")
    public Result wordChoice(@RequestBody WordCheck wordCheck) {
        log.info("获取单词选择题: userId={}", wordCheck.getWordId());
        WordCheckReturn wordCheckReturn = wordService.checkWordAnswer(wordCheck);
        return Result.success(wordCheckReturn);
    }

    /**
     * 获取用户学习单词
     */
    @GetMapping("/studyWord/{userId}")
    public Result studyWord(@PathVariable Integer userId, @RequestParam Integer num) {//num是学习单词的个数
        log.info("用户{}学习{}个单词", userId, num);
        List<Word> list = studyService.studyWord(userId, num);
        if (list == null) {
            return Result.error("没有可学习单词");
        }
        return Result.success(list);
    }

    /**
     * 完成单词学习
     */
    @PostMapping("/completeWord/{userId}")
    public Result completeWord(@PathVariable Integer userId, @RequestParam Integer wordId) {
        log.info("用户{}完成单词学习{}", userId, wordId);
        studyService.completeWord(userId, wordId);
        return Result.success();
    }

    /**
     * 获取用户当天新学习多少
     */
    @GetMapping("/todayNewWordsNum/{userId}")
    public Result getTodayNewWords(@PathVariable Integer userId) {
        log.info("获取用户{}当天新学习单词数量", userId);
        Integer count = studyService.getTodayNewWordsCount(userId);
        if (count == null) {
            count = 0;
        }
        return Result.success(count);
    }

    /**
     * 获取用户当天复习多少
     */
    @GetMapping("/todayReviewWordsNum/{userId}")
    public Result getTodayReviewWords(@PathVariable Integer userId) {
        log.info("获取用户{}当天复习单词数量", userId);
        Integer count = studyService.getTodayReviewWordsCount(userId);
        if (count == null) {
            count = 0;
        }
        return Result.success(count);
    }

    /**
     * 获取用户学习过的单词数量
     */
    @GetMapping("/studiedWordsNum/{userId}")
    public Result getStudiedWordsNum(@PathVariable Integer userId) {
        log.info("获取用户{}学习过的单词数量", userId);
        Integer count = wordService.findStudiedNumByUserId(userId);
        if (count == null) {
            count = 0;
        }
        if(count >= 10){
            if(!achievementService.checkAchievement(userId, 11)){
                achievementService.getAchievement(userId, 11);
            }
        }
        if(count >= 50){
            if(!achievementService.checkAchievement(userId, 12)){
                achievementService.getAchievement(userId, 12);
            }
        }
        if(count >= 100){
            if(!achievementService.checkAchievement(userId, 13)){
                achievementService.getAchievement(userId, 13);
            }
        }
        if(count >= 300){
            if(!achievementService.checkAchievement(userId, 14)){
                achievementService.getAchievement(userId, 14);
            }
        }
        if(count >= 500){
            if(!achievementService.checkAchievement(userId, 15)){
                achievementService.getAchievement(userId, 15);
            }
        }
        if(count >= 1000){
            if(!achievementService.checkAchievement(userId, 16)){
                achievementService.getAchievement(userId, 16);
            }
        }
        if(count >= 2000){
            if(!achievementService.checkAchievement(userId, 17)){
                achievementService.getAchievement(userId, 17);
            }
        }
        if(count >= 3000){
            if(!achievementService.checkAchievement(userId, 18)){
                achievementService.getAchievement(userId, 18);
            }
        }
        return Result.success(count);
    }

    /**
     * 获取用户需要复习的单词
     */
    @GetMapping("/reviewWords/{userId}")
    public Result getReviewWords(@PathVariable Integer userId, @RequestParam Integer num) {
        log.info("获取用户{}需要复习的单词", userId);
        List<Word> list = wordService.findReviewWordsByUserId(userId, num);
        if (list == null) {
            return Result.error("没有需要复习的单词");
        }
        return Result.success(list);
    }

    /**
     * 用户复习完单词
     */
    @PostMapping("/reviewComplete/{userId}")
    public Result reviewComplete(@PathVariable Integer userId, @RequestParam Integer wordId) {
        log.info("用户{}复习完单词{}", userId, wordId);
        studyService.reviewCompleteWord(userId, wordId);
        return Result.success();
    }

    /**
     * 查询用户需要复习多少单词
     */
    @GetMapping("/reviewWordsNum/{userId}")
    public Result getReviewWordsNum(@PathVariable Integer userId) {
        log.info("获取用户{}需要复习的单词数量", userId);
        Integer count = wordService.findReviewWordsNumByUserId(userId);
        if (count == null) {
            count = 0;
        }
        return Result.success(count);
    }

    /**
     * 查询用户每日学习计划单词
     */
    @GetMapping("/planWords/{userId}")
    public Result getPlanWords(@PathVariable Integer userId) {
        log.info("获取用户{}每日学习计划单词", userId);
        Integer planStudyNum = userService.findPlanWordsByUserId(userId);
        if (planStudyNum == null) {
            planStudyNum = 10;
        }
        return Result.success(planStudyNum);
    }

    /**
     * 查询用户每日复习计划
     */
    @GetMapping("/planReviewWords/{userId}")
    public Result getPlanReviewWords(@PathVariable Integer userId) {
        log.info("获取用户{}每日复习计划单词", userId);
        Integer planReviewNum = userService.findPlanReviewWordsByUserId(userId);
        if (planReviewNum == null) {
            planReviewNum = 10;
        }
        return Result.success(planReviewNum);
    }

    /**
     * 更改用户每日学习计划
     */
    @PostMapping("/planWords/{userId}")
    public Result updatePlanWords(@PathVariable Integer userId, @RequestParam Integer planNum) {
        log.info("更改用户{}每日学习计划为{}", userId, planNum);
        userService.updatePlanStudyNum(userId, planNum);
        return Result.success();
    }

    /**
     * 更改用户每日复习计划
     */
    @PostMapping("/planReviewWords/{userId}")
    public Result updatePlanReviewWords(@PathVariable Integer userId, @RequestParam Integer planNum) {
        log.info("更改用户{}每日复习计划为{}", userId, planNum);
        userService.updatePlanReviewNum(userId, planNum);
        return Result.success();
    }
}