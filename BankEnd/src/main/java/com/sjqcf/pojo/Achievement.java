package com.sjqcf.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Achievement {
    private Integer id;
    private String name;
    private String description;//描述
    private Integer type;// 类型,1.连续天数, 2.总学习天数, 3.学习单词个数
    private Integer targetValue;//目标值
    private String badgeIcon;//图标路径
}
