package com.sjqcf.mapper;

import com.sjqcf.pojo.User;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;

@Mapper
public interface UserMapper {
    // 根据手机号查询用户
//    public User findByPhone(String phone);

    // 添加用户
    public void save(User user);

    // 根据id查询用户
    User findById(Integer id);

    void update(User user);

    void updateUserType(Integer id, Integer typeChoice);

    Integer getUserType(Integer userId);

    Integer getTotalStudyDays(Integer id);

    Integer getStudayDays(Integer id);

    Integer getUserTypeByuserId(Integer userId);

    String getSignature(Integer userId);

    String getNameByuserId(Integer userId);

    String getAvatar(Integer userId);

    void updateName(Integer userId, String name);

    void updateSignature(Integer userId, String signature);

    Integer findPlanWordsByUserId(Integer userId);

    Integer findPlanReviewWordsByUserId(Integer userId);

    void updatePlanStudyNum(Integer userId, Integer planNum);

    void updatePlanReviewNum(Integer userId, Integer planNum);

    boolean findPlanByUser(Integer userId);

    void newPlan(Integer userId);

    void updateReviewWordNum(Integer userId, LocalDate now);

    void setReviewWordNum(Integer userId, LocalDate now);

    void updateStudyWordNum(Integer userId, LocalDate now);

    void setStudyWordNum(Integer userId, LocalDate now);

    void updateAvatar(Integer userId, String avatar);

    Boolean isNightMode(Integer userId);

    void updateIsNightMode(Integer userId, Boolean isNightMode);

    User findByEmail(String email);

    String getPassword(Integer userId);

    void updatePassword(Integer userId, String password);

    LocalDate getLastStudyDay(Integer userId);

    void setStudyDays(Integer userId, Integer studyDays);
}
