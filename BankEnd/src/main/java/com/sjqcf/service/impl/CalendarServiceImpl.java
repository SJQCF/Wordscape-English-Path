package com.sjqcf.service.impl;

import com.sjqcf.mapper.CalendarMapper;
import com.sjqcf.pojo.CalendarDay;
import com.sjqcf.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalendarServiceImpl implements CalendarService {

    @Autowired
    private CalendarMapper calendarMapper;

    @Override
    public List<CalendarDay> getCalendar(Integer userId, Integer month, Integer year) {
        return calendarMapper.getCalendar(userId, month ,year);
    }
}
