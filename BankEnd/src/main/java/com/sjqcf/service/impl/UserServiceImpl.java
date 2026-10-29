package com.sjqcf.service.impl;

import com.sjqcf.exception.MyException;
import com.sjqcf.mapper.CodeMapper;
import com.sjqcf.mapper.UserMapper;
import com.sjqcf.pojo.LoginData;
import com.sjqcf.pojo.LoginInfo;
import com.sjqcf.pojo.User;
import com.sjqcf.service.UserService;
import com.sjqcf.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CodeMapper codeMapper;

    @Override
    public String sendCode(String ToEmail) {
        if(ToEmail == null || ToEmail.length() == 0){
            throw new MyException("邮箱异常");
        }
        //生成一个六位随机数字
        String code = (int)(Math.random()*1000000) + "";
        log.info("发送验证码:{}",code);
        LocalDateTime now = LocalDateTime.now();
        //stringRedisTemplate.opsForValue().set("login:code:" + ToEmail,code,5, TimeUnit.MINUTES);//5分钟有效期
        //保存到数据库
        codeMapper.insertCode(ToEmail,code,now);
        return code;
    }

    @Override
    public LoginInfo loginByPassword(LoginData loginData) {
        String email = loginData.getEmail();

        if(email == null || email.length() == 0){
            throw new MyException("邮箱异常");
        }

        User user = userMapper.findByEmail(email);
        if(user == null){
            throw new MyException("用户不存在");
        }
        if(!user.getPassword().equals(loginData.getPassword())){
            throw new MyException("密码错误");
        }

        // 生成JWT
        Map<String,Object> claims = new HashMap<>();// 声明一个map
        claims.put("id",user.getId());// 用户id
        claims.put("username",user.getUsername());// 用户名
        String token = JwtUtils.generateJwt(claims);// 生成JWT

//        UserDto userDto = new UserDto(user.getId(),user.getUsername(),user.getImage());// 创建一个UserDto对象
//        Map<String,Object> usermap = BeanUtil.beanToMap(userDto);// userDto转map
//
//        stringRedisTemplate.opsForHash().putAll("login:user:" + token,usermap);// 保存用户信息

        return new LoginInfo(user.getId(),user.getUsername(),token);
    }

    @Override
    public LoginInfo loginByEmail(LoginData loginData) {
        String email = loginData.getEmail();
        String code = loginData.getCode();

        if(email == null || email.length() == 0){
            throw new MyException("邮箱异常");
        }

        LocalDateTime now = LocalDateTime.now();
        // 从数据库中查询验证码
        String codeEntity = codeMapper.getCode(email,now);
        if(codeEntity == null){
            throw new MyException("验证码不存在");
        }
        if(!codeEntity.equals(code)){
            throw new MyException("验证码错误");
        }

        // 查询用户是否存在
        User user = userMapper.findByEmail(email);
        if(user == null){
            // 注册用户
            user = new User();
            user.setEmail(email);
            user.setUsername(email.split("@")[0]);
            user.setPassword("1");
            userMapper.save(user);
        }

        // 生成JWT
        Map<String,Object> claims = new HashMap<>();// 声明一个map
        claims.put("id",user.getId());// 用户id
        claims.put("username",user.getUsername());// 用户名
        String token = JwtUtils.generateJwt(claims);// 生成JWT

        //删除验证码
        codeMapper.deleteCode(email);

        return new LoginInfo(user.getId(),user.getUsername(),token);
    }

    @Override
    public String getPassword(Integer userId) {
        return userMapper.getPassword(userId);
    }

    @Override
    public void updatePassword(Integer userId, String password) {
        userMapper.updatePassword(userId,password);
    }

    @Override
    public Boolean isStudyYesterday(Integer userId) {
        LocalDate now = LocalDate.now();
        LocalDate lastStudyDay = userMapper.getLastStudyDay(userId);
        if(lastStudyDay == null){
            return false;
        }
        return lastStudyDay.equals(now.minusDays(1));
    }

    @Override
    public void resetStudyDays(Integer userId) {
        userMapper.setStudyDays(userId,0);
    }

    @Override
    public User findById(Integer id) {
        return userMapper.findById(id);
    }

    @Override
    public void update(User user) {
        if(user.getEmail() != null) {
            if (user.getEmail().length() == 0) {
                throw new MyException("邮箱异常");
            }
        }
        userMapper.update(user);
    }

    @Override
    public void updateUserType(Integer userId, Integer typeChoice) {
        if(typeChoice < 1 || typeChoice > 5){
            throw new MyException("用户类型异常");
        }
        userMapper.updateUserType(userId,typeChoice);
    }

    @Override
    public Integer getTotalStudyDays(Integer id) {
        Integer studyDays = userMapper.getTotalStudyDays(id);
        return studyDays;
    }

    @Override
    public Integer getStudyDays(Integer id) {
        return userMapper.getStudayDays(id);
    }

    @Override
    public Integer getTypeByUserId(Integer userId) {
        return userMapper.getUserTypeByuserId(userId);
    }

    @Override
    public String getSignature(Integer userId) {
        return userMapper.getSignature(userId);
    }

    @Override
    public String getName(Integer userId) {
        return userMapper.getNameByuserId(userId);
    }

    @Override
    public String getAvatar(Integer userId) {
        return userMapper.getAvatar(userId);
    }

    @Override
    public void updateName(Integer userId, String name) {
        userMapper.updateName(userId,name);
    }

    @Override
    public void updateSignature(Integer userId, String signature) {
        userMapper.updateSignature(userId,signature);
    }

    @Override
    public Integer findPlanWordsByUserId(Integer userId) {
        return userMapper.findPlanWordsByUserId(userId);
    }

    @Override
    public Integer findPlanReviewWordsByUserId(Integer userId) {
        return userMapper.findPlanReviewWordsByUserId(userId);
    }

    @Override
    public void updatePlanStudyNum(Integer userId, Integer planNum) {
        boolean isExist = userMapper.findPlanByUser(userId);
        if(!isExist){
            userMapper.newPlan(userId);
        }
        userMapper.updatePlanStudyNum(userId,planNum);
    }

    @Override
    public void updatePlanReviewNum(Integer userId, Integer planNum) {
        boolean isExist = userMapper.findPlanByUser(userId);
        if(!isExist){
            userMapper.newPlan(userId);
        }
        userMapper.updatePlanReviewNum(userId,planNum);
    }

    @Override
    public void updateAvatar(Integer userId, String avatar) {
        userMapper.updateAvatar(userId,avatar);
    }

    @Override
    public Boolean isNightMode(Integer userId) {
        return userMapper.isNightMode(userId);
    }

    @Override
    public void updateIsNightMode(Integer userId, Boolean isNightMode) {
        userMapper.updateIsNightMode(userId,isNightMode);
    }
}
