package com.sjqcf.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalendarDay {
    private Integer day;// 日期
    private Integer studyWordsNum;// 学习单词数
    private Integer reviewWordsNum;// 复习单词数
}
