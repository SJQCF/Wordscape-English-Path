package com.sjqcf.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface CodeMapper {
    void insertCode(String ToEmail, String code, LocalDateTime now);

    String getCode(String ToEmail, LocalDateTime now);

    void deleteCode(String ToEmail);
}
