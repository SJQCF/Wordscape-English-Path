package com.sjqcf.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Course {
    private Integer id;//课程id
    private String title;//课程名
    private String description;//课程描述
    private String teacher;//课程老师
    private String duration;//课程时长
    private String price;//课程价格
    private String coverImage;//课程封面
}
