package com.sjqcf.exception;

import com.sjqcf.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class AllException {
    @ExceptionHandler
    public Result handle(Exception e){
        log.error("出现异常:",e);
        return Result.error("服务器异常");
    }

    @ExceptionHandler
    public Result handleMyException(MyException e){
        log.info("出现自定义异常",e);
        return Result.error(e.getMessage());
    }
}
