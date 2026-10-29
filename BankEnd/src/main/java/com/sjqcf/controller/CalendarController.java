package com.sjqcf.controller;

import com.sjqcf.pojo.Result;
import com.sjqcf.service.CalendarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/calendar")
public class CalendarController {

    @Autowired
    private CalendarService calendarService;

    /**
     * 获取用户当月每天的学习情况
     */
    @GetMapping("/{userId}")
    public Result getCalendar(@PathVariable Integer userId,
                              @RequestParam(required = false) Integer month,
                              @RequestParam(required = false) Integer year) {
        log.info("获取用户{}的{}月的学习日历信息", userId, month);
        if (month == null) {
            month = java.time.LocalDate.now().getMonthValue();
        }
        if (year == null) {
            year = java.time.LocalDate.now().getYear();
        }
        return Result.success(calendarService.getCalendar(userId, month, year));
    }
}
