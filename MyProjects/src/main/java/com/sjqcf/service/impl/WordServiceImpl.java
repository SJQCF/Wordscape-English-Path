package com.sjqcf.service.impl;


import com.github.pagehelper.PageHelper;
import com.sjqcf.mapper.UserMapper;
import com.sjqcf.mapper.WordMapper;
import com.sjqcf.pojo.Word;
import com.sjqcf.pojo.WordCheck;
import com.sjqcf.pojo.WordCheckReturn;
import com.sjqcf.service.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.sjqcf.utils.SystemConstants.PAGE_SIZE;

@Service
public class WordServiceImpl implements WordService {

    @Autowired
    private WordMapper wordMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Word> list(Integer pageNum) {//pageNum是当前页码
        PageHelper.startPage(pageNum,PAGE_SIZE);
        return wordMapper.findAll();
    }

    @Override
    public List<Word> findByEn(String english, Integer pageNum) {
        PageHelper.startPage(pageNum,PAGE_SIZE);
        return wordMapper.findByEn(english);
    }

    @Override
    public List<Word> findByCn(String chinese, Integer pageNum) {
        return wordMapper.findByCn(chinese);
    }

    @Override
    public List<Word> findStarByUserId(Integer id, Integer pageNum) {
        PageHelper.startPage(pageNum,PAGE_SIZE);
        return wordMapper.findStarByUserId(id);
    }

    @Override
    public Integer findStarNumByUserId(Integer id) {
        return wordMapper.findStarNumByUserId(id);
    }

    @Override
    public WordCheckReturn checkWordAnswer(WordCheck wordCheck) {
        WordCheckReturn wordCheckReturn = new WordCheckReturn();
        if(wordCheck.getUserInput().equals(wordCheck.getCorrectAnswer())){
            wordCheckReturn.setCorrect(true);
            wordCheckReturn.setWord(wordMapper.findById(wordCheck.getWordId()));
            return wordCheckReturn;
        }
        wordCheckReturn.setCorrect(false);
        wordCheckReturn.setWord(wordMapper.findById(wordCheck.getWordId()));
        return wordCheckReturn;
    }

    @Override
    public void star(Integer userId, Integer wordId) {
        wordMapper.star(userId,wordId);
    }

    //获取已学单词数
    @Override
    public Integer findStudiedNumByUserId(Integer userId) {
        Integer type = userMapper.getUserType(userId);
        return wordMapper.getStudiedNumByUserId(userId,type);
    }

    @Override
    public void cancelStar(Integer userId, Integer wordId) {
        wordMapper.cancelStar(userId,wordId);
    }

    @Override
    public Boolean checkStar(Integer userId, Integer wordId) {
        return wordMapper.checkStar(userId,wordId) > 0;
    }

    @Override
    public List<Word> findStudiedWordsByUserId(Integer userId, Integer pageNum, Integer type) {
        PageHelper.startPage(pageNum,PAGE_SIZE);
        if(type == 4){//商务
            return wordMapper.findStudiedBusinessWordsByUserId(userId, type);
        }
        else if(type == 5){//出国
            return wordMapper.findStudiedAbroadWordsByUserId(userId, type);
        }
        else if(type == 3){//六级
            return wordMapper.findStudiedSixWordsByUserId(userId, type);
        }
        else if(type == 2){//四级
            return wordMapper.findStudiedFourWordsByUserId(userId, type);
        }
        else{//高中
            return wordMapper.findStudiedWordsByUserId(userId, type);
        }
    }

    @Override
    public List<Word> findReviewWordsByUserId(Integer userId, Integer num) {
        Integer type = userMapper.getUserType(userId);
        if(type == 4){//商务
            return wordMapper.findReviewBusinessWordsByUserId(userId,num);
        }
        else if(type == 5){//出国
            return wordMapper.findReviewAbroadWordsByUserId(userId,num);
        }
        else if(type == 3){//六级
            return wordMapper.findReviewSixWordsByUserId(userId,num,type);
        }
        else if(type == 2){//四级
            return wordMapper.findReviewFourWordsByUserId(userId,num,type);
        }
        else{//高中
            return wordMapper.findReviewWordsByUserId(userId,num,type);
        }
    }

    @Override
    public Integer findReviewWordsNumByUserId(Integer userId) {
        Integer type = userMapper.getUserType(userId);
        if (type == 4){//商务
            return wordMapper.findReviewBusinessWordsNumByUserId(userId);
        }
        else if(type == 5){//出国
            return wordMapper.findReviewAbroadWordsNumByUserId(userId);
        }
        else if(type == 3){//六级
            return wordMapper.findReviewSixWordsNumByUserId(userId);
        }
        else if(type == 2){//四级
            return wordMapper.findReviewFourWordsNumByUserId(userId);
        }
        else{//高中
            return wordMapper.findReviewWordsNumByUserId(userId);
        }
    }

    @Override
    public Word getWordById(Integer id) {
        return wordMapper.findById(id);
    }

    @Override
    public List<Word> find(String word, Integer pageNum) {
        PageHelper.startPage(pageNum,PAGE_SIZE);
        return wordMapper.find(word);
    }
}
