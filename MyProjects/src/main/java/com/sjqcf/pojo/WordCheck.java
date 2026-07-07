package com.sjqcf.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WordCheck {
    private String userInput;      // 用户输入
    private String correctAnswer;  // 正确答案
    private Integer wordId;        // 单词ID
}
