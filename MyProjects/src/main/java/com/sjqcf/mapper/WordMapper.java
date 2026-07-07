package com.sjqcf.mapper;


import com.sjqcf.pojo.Word;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface WordMapper {
    List<Word> findAll();

    List<Word> findByEn(String english);

    List<Word> findByCn(String chinese);

    List<Word> findStarByUserId(Integer id);

    Integer findStarNumByUserId(Integer id);

    Word findById(Integer id);

    void star(Integer userId, Integer wordId);

    List<Word> findUnstudiedWords(Integer userId, Integer userType, Integer num);

    List<Word> findUnstudiedBusinessWords(Integer userId, Integer userType, Integer num);

    List<Word> findUnstudiedAbroadWords(Integer userId, Integer userType, Integer num);

    void completeWord(Integer userId, Integer wordId, Integer type);

    void completeBusinessWord(Integer userId, Integer wordId, Integer type);

    void completeAbroadWord(Integer userId, Integer wordId, Integer type);

    Integer getTodayNewWordsCount(Integer userId, LocalDate now);

    Integer getTodayReviewWordsCount(Integer userId, LocalDate now);

    Integer getStudiedNumByUserId(Integer userId, Integer type);

    void cancelStar(Integer userId, Integer wordId);

    Integer checkStar(Integer userId, Integer wordId);

    List<Word> findStudiedBusinessWordsByUserId(Integer userId, Integer type);

    List<Word> findStudiedAbroadWordsByUserId(Integer userId, Integer type);

    List<Word> findStudiedSixWordsByUserId(Integer userId, Integer type);

    List<Word> findStudiedFourWordsByUserId(Integer userId, Integer type);

    List<Word> findStudiedWordsByUserId(Integer userId, Integer type);

    List<Word> findReviewBusinessWordsByUserId(Integer userId, Integer num);

    List<Word> findReviewAbroadWordsByUserId(Integer userId, Integer num);

    List<Word> findReviewSixWordsByUserId(Integer userId, Integer num, Integer type);

    List<Word> findReviewFourWordsByUserId(Integer userId, Integer num, Integer type);

    List<Word> findReviewWordsByUserId(Integer userId, Integer num, Integer type);

    void updateCompleteBusinessWord(Integer userId, Integer wordId, Integer type);

    void updateCompleteAbroadWord(Integer userId, Integer wordId, Integer type);

    void reviewCompleteWord(Integer userId, Integer wordId, Integer type);

    void updateReviewCount(Integer userId, Integer wordId, Integer type);

    Integer findReviewNumByUserId(Integer userId, Integer type);

    Integer findReviewBusinessWordsNumByUserId(Integer userId);

    Integer findReviewAbroadWordsNumByUserId(Integer userId);

    Integer findReviewSixWordsNumByUserId(Integer userId);

    Integer findReviewFourWordsNumByUserId(Integer userId);

    Integer findReviewWordsNumByUserId(Integer userId);

    List<Word> find(String word);

    Word findBusinessWordById(Integer id);

    Word findAbroadWordById(Integer id);
}
