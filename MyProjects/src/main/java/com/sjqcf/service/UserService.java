package com.sjqcf.service;

import com.sjqcf.pojo.LoginData;
import com.sjqcf.pojo.LoginInfo;
import com.sjqcf.pojo.User;
import com.sjqcf.pojo.Word;

import java.util.List;

public interface UserService {
    String sendCode(String ToEmail);

    User findById(Integer id);

    void update(User user);

    void updateUserType(Integer userId, Integer typeChoice);

    Integer getTotalStudyDays(Integer id);

    Integer getStudyDays(Integer id);

    Integer getTypeByUserId(Integer userId);

    String getSignature(Integer userId);

    String getName(Integer userId);

    String getAvatar(Integer userId);

    void updateName(Integer userId, String name);

    void updateSignature(Integer userId, String signature);

    Integer findPlanWordsByUserId(Integer userId);

    Integer findPlanReviewWordsByUserId(Integer userId);

    void updatePlanStudyNum(Integer userId, Integer planNum);

    void updatePlanReviewNum(Integer userId, Integer planNum);

    void updateAvatar(Integer userId, String avatar);

    Boolean isNightMode(Integer userId);

    void updateIsNightMode(Integer userId, Boolean isNightMode);

    LoginInfo loginByPassword(LoginData loginData);

    LoginInfo loginByEmail(LoginData loginData);

    String getPassword(Integer userId);

    void updatePassword(Integer userId, String password);

    Boolean isStudyYesterday(Integer userId);

    void resetStudyDays(Integer userId);
}
