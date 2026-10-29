package com.sjqcf.service.impl;

import com.sjqcf.mapper.UserMapper;
import com.sjqcf.mapper.WordMapper;
import com.sjqcf.pojo.User;
import com.sjqcf.pojo.Word;
import com.sjqcf.service.StudyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class StudyServiceImpl implements StudyService {

    @Autowired
    private WordMapper wordMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取用户学习单词
     */
    @Override
    public List<Word> studyWord(Integer userId, Integer num) {
        Integer userType = userMapper.getUserType(userId);
        List<Word> list = null;
        //商务英语
        if(userType == 4){
            list = wordMapper.findUnstudiedBusinessWords(userId,userType,num);
        }
        //出国英语
        else if(userType == 5){
            list = wordMapper.findUnstudiedAbroadWords(userId,userType,num);
        }
        else{
            list = wordMapper.findUnstudiedWords(userId,userType,num);
        }
        return list;
    }

    /**
     * 完成单词学习
     */
    @Override
    public void completeWord(Integer userId, Integer wordId) {
        User user = userMapper.findById(userId);
        //更新用户今日学习单词数量
        LocalDate lastStudyDay = user.getLastStudyDay();
        LocalDate now = LocalDate.now();
        if(lastStudyDay.equals(now)){//同一天
            log.info("同天学习");
            userMapper.updateStudyWordNum(userId,now);
        }
        else{
            log.info("不同天学习");
            userMapper.setStudyWordNum(userId,now);
        }

        //更新最后学习时间
        compareLastStudyTime(user);

        if(user.getType() == 4){
            Word word = wordMapper.findBusinessWordById(wordId);
            wordMapper.completeBusinessWord(userId,wordId,word.getType());
        }
        else if(user.getType() == 5){
            Word word = wordMapper.findAbroadWordById(wordId);
            wordMapper.completeAbroadWord(userId,wordId,word.getType());
        }
        else{
            Word word = wordMapper.findById(wordId);
            wordMapper.completeWord(userId,wordId,word.getType());
        }
    }

    /**
     * 比较上次学习时间
     */
    @Override
    public void compareLastStudyTime(User user) {
        LocalDate now = LocalDate.now();
        LocalDate lastStudyDay = user.getLastStudyDay();

        if(lastStudyDay == null){
            user.setLastStudyDay(now);
            user.setStudyDays(1);
            user.setTotalStudyDays(1);
            userMapper.update(user);
            return;
        }

        if(!lastStudyDay.equals(now)){//不是同一天
            if(lastStudyDay.plusDays(1).equals(now)){//是连续学习
                user.setLastStudyDay(now);//更新最后学习时间
                user.setStudyDays(user.getStudyDays() + 1);//更新连续学习天数
                user.setTotalStudyDays(user.getTotalStudyDays() + 1);//更新总学习天数
                userMapper.update(user);
            }
            else{
                user.setLastStudyDay(now);//更新最后学习时间
                user.setStudyDays(1);//更新连续学习天数
                user.setTotalStudyDays(user.getTotalStudyDays() + 1);//更新总学习天数
                userMapper.update(user);
            }
        }
    }

    @Override
    public Integer getTodayNewWordsCount(Integer userId) {
        LocalDate now = LocalDate.now();
        return wordMapper.getTodayNewWordsCount(userId,now);
    }

    @Override
    public Integer getTodayReviewWordsCount(Integer userId) {
        LocalDate now = LocalDate.now();
        return wordMapper.getTodayReviewWordsCount(userId,now);
    }

    /**
     * 完成单词复习
     */
    @Override
    public void reviewCompleteWord(Integer userId, Integer wordId) {
        User user = userMapper.findById(userId);
        //更新用户今日复习单词数量
        LocalDate lastStudyDay = user.getLastStudyDay();
        LocalDate now = LocalDate.now();
        if(lastStudyDay.equals(now)){//同一天
            userMapper.updateReviewWordNum(userId,now);
        }
        else{
            userMapper.setReviewWordNum(userId,now);
        }
        //更新最后学习时间
        compareLastStudyTime(user);
        if(user.getType() == 4){
            Word word = wordMapper.findBusinessWordById(wordId);
            wordMapper.updateCompleteBusinessWord(userId,wordId,word.getType());
        }
        else if(user.getType() == 5){
            Word word = wordMapper.findAbroadWordById(wordId);
            wordMapper.updateCompleteAbroadWord(userId,wordId,word.getType());
        }
        else{
            Word word = wordMapper.findById(wordId);
            wordMapper.updateReviewCount(userId,wordId,word.getType());
        }
    }
}
