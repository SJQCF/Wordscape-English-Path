package com.sjqcf.controller;

import com.github.pagehelper.PageHelper;
import com.sjqcf.pojo.Result;
import com.sjqcf.pojo.Word;
import com.sjqcf.service.WordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/word")
public class WordController {

    @Autowired
    private WordService wordService;

    @GetMapping("/list")
    public Result list(Integer pageNum) {
        log.info("查询单词列表");
        List<Word> list = wordService.list(pageNum);
        return Result.success(list);
    }

    @GetMapping("/findByEn")
    public Result findByEnglish(String english, Integer pageNum) {
        log.info("查询单词:{}", english);
        List<Word> list = wordService.findByEn(english, pageNum);
        return Result.success(list);
    }

    @GetMapping("/findByCn")
    public Result findByChinese(String chinese, Integer pageNum) {
        log.info("查询单词:{}", chinese);
        List<Word> list = wordService.findByCn(chinese, pageNum);
        return Result.success(list);
    }

    //查询单词
    @GetMapping("/find")
    public Result find(String word, Integer pageNum) {
        log.info("查询单词:{}", word);
        List<Word> list = wordService.find(word, pageNum);
        return Result.success(list);
    }

    @GetMapping("/findStarByUserId/{userId}")
    public Result findStarByUserId(@PathVariable Integer userId, Integer pageNum) {
        log.info("查询用户收藏的单词:{}", userId);
        List<Word> list = wordService.findStarByUserId(userId, pageNum);
        return Result.success(list);
    }

    @GetMapping("/findStarNumByUserId/{userId}")
    public Result findStarNumByUserId(@PathVariable Integer userId) {
        log.info("查询用户收藏的个数:{}", userId);
        Integer num = wordService.findStarNumByUserId(userId);
        return Result.success(num);
    }

    @PostMapping("/star/{userId}")
    public Result star(@PathVariable Integer userId, @RequestParam Integer wordId) {
        log.info("收藏单词: userId={}, wordId={}", userId, wordId);
        wordService.star(userId, wordId);
        return Result.success();
    }

    /**
     * 取消收藏单词
     */
    @PostMapping("/cancelStar/{userId}")
    public Result cancelStar(@PathVariable Integer userId, @RequestParam Integer wordId) {
        log.info("取消收藏单词: userId={}, wordId={}", userId, wordId);
        wordService.cancelStar(userId, wordId);
        return Result.success();
    }

    /**
     * 获取用户是否收藏此单词
     */
    @GetMapping("/checkStar/{userId}")
    public Result checkStar(@PathVariable Integer userId, @RequestParam Integer wordId) {
        log.info("查询用户是否收藏此单词: userId={}, wordId={}", userId, wordId);
        Boolean isStar = wordService.checkStar(userId, wordId);
        return Result.success(isStar);
    }

    /**
     * 获取用户学过的单词
     */
    @GetMapping("/studiedWords/{userId}")
    public Result findStudiedWordsByUserId(@PathVariable Integer userId, Integer pageNum, Integer type) {
        log.info("查询用户学过的单词:{}", userId);
        List<Word> list = wordService.findStudiedWordsByUserId(userId, pageNum, type);
        return Result.success(list);
    }

    /**
     * 根据id查询单词
     */
    @GetMapping("/getWordById")
    public Result findById(@RequestParam Integer wordId) {
        log.info("根据id查询单词:{}", wordId);
        Word word = wordService.getWordById(wordId);
        return Result.success(word);
    }
}
