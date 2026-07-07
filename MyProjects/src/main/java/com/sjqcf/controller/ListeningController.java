package com.sjqcf.controller;

import com.sjqcf.pojo.Result;
import com.sjqcf.service.ListeningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/listening")
public class ListeningController {

    @Autowired
    private ListeningService ListeningService;

    //获取所有听力音频的名字，类型，id
    @GetMapping("/list")
    public Result list(Integer pageNum) {
        log.info("获取所有听力音频");
        return Result.success(ListeningService.getListeningList(pageNum));
    }

    //根据类型获取听力音频
    @GetMapping("/listByType")
    public Result listByType(Integer pageNum, Integer type) {
        log.info("根据类型获取听力音频");
        return Result.success(ListeningService.getListeningListByType(pageNum, type));
    }

    //根据id获取听力音频详细信息
    @GetMapping("/getById")
    public Result getById(Integer id) {
        log.info("根据id获取听力音频详细信息");
        return Result.success(ListeningService.getById(id));
    }

    //播放音频接口
    @GetMapping("/play")
    public Result play(Integer id) throws Exception {
        log.info("播放音频，音频ID: {}", id);
        try {
            String audioUrl = ListeningService.playAudio(id);
            return Result.success(audioUrl);
        } catch (Exception e) {
            log.error("播放音频失败: {}", e.getMessage());
            return Result.error("播放音频失败: " + e.getMessage());
        }
    }

    /**
     * 收藏音频
     */
    @PostMapping("/favorite/{userId}")
    public Result favoriteAudio(@PathVariable Integer userId, @RequestParam Integer audioId) {
        log.info("收藏音频，音频ID: {}, 用户ID: {}", audioId, userId);
        ListeningService.favoriteAudio(audioId, userId);
        return Result.success();
    }

    /**
     * 取消收藏音频
     */
    @PostMapping("/cancelFavorite/{userId}")
    public Result cancelFavoriteAudio(@PathVariable Integer userId, @RequestParam Integer audioId) {
        log.info("取消收藏音频，音频ID: {}, 用户ID: {}", audioId, userId);
        ListeningService.cancelFavoriteAudio(audioId, userId);
        return Result.success();
    }

    /**
     * 获取用户收藏的音频基础信息
     */
    @GetMapping("/getFavoriteAudios/{userId}")
    public Result getFavoriteAudios(@PathVariable Integer userId, @RequestParam Integer pageNum) {
        log.info("获取用户收藏的音频，用户ID: {}", userId);
        return Result.success(ListeningService.getFavoriteAudios(userId, pageNum));
    }

    /**
     * 检查用户是否收藏过此音频
     */
    @GetMapping("/checkFavorite/{userId}")
    public Result checkFavorite(@PathVariable Integer userId, @RequestParam Integer audioId) {
        log.info("检查用户是否收藏过此音频，音频ID: {}, 用户ID: {}", audioId, userId);
        return Result.success(ListeningService.checkFavoriteAudio(userId, audioId));
    }
}
