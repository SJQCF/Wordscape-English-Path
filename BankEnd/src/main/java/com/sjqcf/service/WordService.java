package com.sjqcf.service;

import com.sjqcf.pojo.Word;
import com.sjqcf.pojo.WordCheck;
import com.sjqcf.pojo.WordCheckReturn;

import java.util.List;

public interface WordService {
    List<Word> list(Integer pageNum);

    List<Word> findByEn(String english,Integer pageNum);

    List<Word> findByCn(String chinese,Integer pageNum);

    List<Word> findStarByUserId(Integer id,Integer pageNum);

    Integer findStarNumByUserId(Integer id);

    WordCheckReturn checkWordAnswer(WordCheck wordCheck);

    void star(Integer userId, Integer wordId);

    Integer findStudiedNumByUserId(Integer userId);

    void cancelStar(Integer userId, Integer wordId);

    Boolean checkStar(Integer userId, Integer wordId);

    List<Word> findStudiedWordsByUserId(Integer userId, Integer pageNum, Integer type);

    List<Word> findReviewWordsByUserId(Integer userId, Integer num);

    Integer findReviewWordsNumByUserId(Integer userId);

    Word getWordById(Integer id);

    List<Word> find(String word, Integer pageNum);
}
