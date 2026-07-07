package com.sjqcf.controller;

import com.sjqcf.pojo.Result;
import com.sjqcf.pojo.User;
import com.sjqcf.service.AchievementService;
import com.sjqcf.service.UserService;
import com.sjqcf.utils.AliyunOSSOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserService userService;

    @Autowired
    private AliyunOSSOperator aliyunOSSOperator;

    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        log.info("查询用户:{}", id);
        User user = userService.findById(id);
        return Result.success(user);
    }

    @PutMapping
    public Result update(@RequestBody User user) {
        log.info("修改用户:{}", user);
        userService.update(user);
        return Result.success();
    }

    /**
     * 获取用户当前类型
     */
    @GetMapping("/type/{userId}")
    public Result getUserType(@PathVariable Integer userId) {
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        log.info("获取用户当前类型");
        Integer type = userService.getTypeByUserId(userId);
        if (type == null) {
            return Result.error("用户类型未设置");
        }
        return Result.success(type);
    }

    /**
     * 设置用户类型
     * 1.高中,2.四级,3.六级,4.商务,5.出国
     */
    @PostMapping("/type/{userId}")
    public Result setUserType(@PathVariable Integer userId, @RequestParam Integer typeChoice) {
        log.info("设置用户{}类型:{}", userId, typeChoice);
        userService.updateUserType(userId, typeChoice);
        return Result.success();
    }

    /**
     * 获取用户一共学习天数
     */
    @GetMapping("/totalStudyDays/{userId}")
    public Result getTotalStudyDays(@PathVariable Integer userId) {
        log.info("获取用户{}学习天数", userId);
        Integer studyDays = userService.getTotalStudyDays(userId);
        if (studyDays == null) {
            studyDays = 0;
        }
        if (studyDays >= 10) {
            if (!achievementService.checkAchievement(userId, 6)) {
                achievementService.getAchievement(userId, 6);
            }
        }
        if (studyDays >= 30) {
            if (!achievementService.checkAchievement(userId, 7)) {
                achievementService.getAchievement(userId, 7);
            }
        }
        if (studyDays >= 60) {
            if (!achievementService.checkAchievement(userId, 8)) {
                achievementService.getAchievement(userId, 8);
            }
        }
        if (studyDays >= 100) {
            if (!achievementService.checkAchievement(userId, 9)) {
                achievementService.getAchievement(userId, 9);
            }
        }
        if (studyDays >= 365) {
            if (!achievementService.checkAchievement(userId, 10)) {
                achievementService.getAchievement(userId, 10);
            }
        }
        return Result.success(studyDays);
    }

    /**
     * 获取用户连续学习天数
     */
    @GetMapping("/studyDays/{userId}")
    public Result getStudyDays(@PathVariable Integer userId) {
        log.info("获取用户{}连续学习天数", userId);
        Integer studyDays = userService.getStudyDays(userId);
        if (studyDays == null) {
            studyDays = 0;
        }
        if (studyDays >= 1) {
            if (!achievementService.checkAchievement(userId, 1)) {
                achievementService.getAchievement(userId, 1);
            }
        }
        if (studyDays >= 3) {
            if (!achievementService.checkAchievement(userId, 2)) {
                achievementService.getAchievement(userId, 2);
            }
        }
        if (studyDays >= 7) {
            if (!achievementService.checkAchievement(userId, 3)) {
                achievementService.getAchievement(userId, 3);
            }
        }
        if (studyDays >= 15) {
            if (!achievementService.checkAchievement(userId, 4)) {
                achievementService.getAchievement(userId, 4);
            }
        }
        if (studyDays >= 30) {
            if (!achievementService.checkAchievement(userId, 5)) {
                achievementService.getAchievement(userId, 5);
            }
        }
        return Result.success(studyDays);
    }

    /**
     * 查询用户签名
     */
    @GetMapping("/signature/{userId}")
    public Result getSignature(@PathVariable Integer userId) {
        log.info("查询用户{}签名", userId);
        String signature = userService.getSignature(userId);
        return Result.success(signature);
    }

    /**
     * 查询用户名字
     */
    @GetMapping("/name/{userId}")
    public Result getName(@PathVariable Integer userId) {
        log.info("查询用户{}名字", userId);
        String name = userService.getName(userId);
        return Result.success(name);
    }

    /**
     * 查询用户头像
     */
    @GetMapping("/avatar/{userId}")
    public Result getAvatar(@PathVariable Integer userId) {
        log.info("查询用户{}头像", userId);
        String avatar = userService.getAvatar(userId);
        return Result.success(avatar);
    }

    /**
     * 更改用户名字
     */
    @PostMapping("/name/{userId}")
    public Result updateName(@PathVariable Integer userId, @RequestParam String name) {
        log.info("更改用户{}名字:{}", userId, name);
        userService.updateName(userId, name);
        return Result.success();
    }

    /**
     * 更改用户签名
     */
    @PostMapping("/signature/{userId}")
    public Result updateSignature(@PathVariable Integer userId, @RequestParam String signature) {
        log.info("更改用户{}签名:{}", userId, signature);
        userService.updateSignature(userId, signature);
        return Result.success();
    }

    /**
     * 更改用户头像
     */
    @PostMapping("/avatar/{userId}")
    public Result updateAvatar(@PathVariable Integer userId, @RequestParam MultipartFile file) throws Exception {
        log.info("更改用户{}", userId);
        byte[] fileBytes = file.getBytes();// 获取文件字节数组
        // 上传文件到OSS
        String avatarUrl = aliyunOSSOperator.upload(fileBytes, file.getOriginalFilename());
        userService.updateAvatar(userId, avatarUrl);
        return Result.success();
    }

    /**
     * 查询用户是否是黑夜模式
     */
    @GetMapping("/isNightMode/{userId}")
    public Result isNightMode(@PathVariable Integer userId) {
        log.info("查询用户{}是否是黑夜模式", userId);
        return Result.success(userService.isNightMode(userId));
    }

    /**
     * 更改用户黑夜模式
     */
    @PostMapping("/isNightMode/{userId}")
    public Result updateIsNightMode(@PathVariable Integer userId, @RequestParam Boolean isNightMode) {
        log.info("更改用户{}黑夜模式:{}", userId, isNightMode);
        userService.updateIsNightMode(userId, isNightMode);
        return Result.success();
    }

    /**
     * 查询用户当前密码
     */
    @GetMapping("/password/{userId}")
    public Result getPassword(@PathVariable Integer userId) {
        log.info("查询用户{}当前密码", userId);
        return Result.success(userService.getPassword(userId));
    }

    /**
     * 更改用户密码
     */
    @PostMapping("/password/{userId}")
    public Result updatePassword(@PathVariable Integer userId, @RequestParam String password) {
        log.info("更改用户{}密码:{}", userId, password);
        userService.updatePassword(userId, password);
        return Result.success();
    }

    /**
     * 查询用户前一天是否学习
     */
    @GetMapping("/isStudyYesterday/{userId}")
    public Result isStudyYesterday(@PathVariable Integer userId) {
        log.info("查询用户{}是否学习昨天", userId);
        return Result.success(userService.isStudyYesterday(userId));
    }

    /**
     * 用户连续学习天数归零
     */
    @PostMapping("/resetStudyDays/{userId}")
    public Result resetStudyDays(@PathVariable Integer userId) {
        log.info("用户{}连续学习天数归零", userId);
        userService.resetStudyDays(userId);
        return Result.success();
    }
}
