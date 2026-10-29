package com.sjqcf.utils;

import java.time.LocalDate;

public class DateValidation {
    //看用户此次学习是否连续
    public static boolean isContinuous(LocalDate lastStudyDay/*当前登录日期*/) {
        if (lastStudyDay == null){
            return false;
        }
        LocalDate nowDay = LocalDate.now();
        return nowDay.isAfter(lastStudyDay) && nowDay.getDayOfYear() - lastStudyDay.getDayOfYear() == 1;
    }
}
