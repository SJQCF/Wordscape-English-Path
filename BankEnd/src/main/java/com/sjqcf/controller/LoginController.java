package com.sjqcf.controller;

import com.sjqcf.pojo.LoginData;
import com.sjqcf.pojo.LoginInfo;
import com.sjqcf.pojo.Result;
import com.sjqcf.service.SendEmailService;
import com.sjqcf.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private SendEmailService sendEmailService;

    /**
     * 发送验证码给邮箱
     */
    @PostMapping("/code")
    public Result sendCode(@RequestParam String email) {
        log.info("发送验证码给邮箱:{}", email);
        String code = userService.sendCode(email);
        sendEmailService.sendCode(email, code);
        return Result.success();
    }

    /**
     * 使用账号密码登录
     *
     * @param loginData
     */
    @PostMapping("/password")
    public Result login(@RequestBody LoginData loginData) {
        log.info("使用账号密码登录");
        LoginInfo loginInfo = userService.loginByPassword(loginData);
        return Result.success(loginInfo);
    }

    /**
     * 使用邮箱验证码登录或注册
     */
    @PostMapping("/email")
    public Result loginByEmail(@RequestBody LoginData loginData) {
        log.info("使用邮箱验证码登录或注册");
        LoginInfo loginInfo = userService.loginByEmail(loginData);
        return Result.success(loginInfo);
    }
}
