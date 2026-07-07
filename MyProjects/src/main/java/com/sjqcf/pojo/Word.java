package com.sjqcf.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Word {
    private Integer id;//单词id
    private String word;//单词
    private String phonetic;//音标
    private String definition;//释义
    private String example;//例句
    private Integer type;//类型 1.高中,2.四级,3.六级,4.商务,5.出国
}
