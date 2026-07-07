package com.sjqcf.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Audio {
    private Integer id;// id
    private String audioUrl;// 音频地址
    private String title;// 标题
    private Integer duration;// 时长
    private Integer category;// 类别1.高中听力，2.四级听力，3.六级听力，4.新闻报道，5.英文歌曲,6.日常对话
    private String transcript;// 听力原文
}
