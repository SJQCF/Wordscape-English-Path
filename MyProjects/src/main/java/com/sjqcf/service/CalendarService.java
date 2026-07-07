package com.sjqcf.service;

import com.sjqcf.pojo.CalendarDay;

import java.util.List;

public interface CalendarService {
    List<CalendarDay> getCalendar(Integer userId, Integer month, Integer year);
}
