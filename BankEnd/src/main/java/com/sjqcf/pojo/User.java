package com.sjqcf.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Integer id;
    private String username;//用户名
    private String password;//密码
    private String image;//头像
    private String email;//邮箱
    private Integer studyDays;//连续学习天数
    private Integer totalStudyDays;//总学习天数
    private LocalDate lastStudyDay;//最后学习日期
    private Integer type;//用户类型 1.高中,2.四级,3.六级,4.商务,5.出国
    private Boolean darkMode;//夜间模式

    public User(String email) {
        this.email = email;
        //截取邮箱前4位
        String suffix = email.substring(0, 4);
        this.username = "nick" + suffix;
        this.password = "";
    }
}
