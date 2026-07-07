package com.sjqcf.mapper;

import com.sjqcf.pojo.CalendarDay;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CalendarMapper {
    List<CalendarDay> getCalendar(Integer userId, Integer month, Integer year);
}
