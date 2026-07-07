package com.sjqcf.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAchievement {
    private Integer id;//主键
    private Integer userId;//用户id
    private Integer achievementId;//成就id
    private String obtainedTime;//获取时间
}
