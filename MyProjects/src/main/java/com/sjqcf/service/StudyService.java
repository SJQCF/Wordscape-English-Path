package com.sjqcf.service;

import com.sjqcf.pojo.User;
import com.sjqcf.pojo.Word;

import java.util.List;

public interface StudyService {
    List<Word> studyWord(Integer userId, Integer num);

    void completeWord(Integer userId, Integer wordId);

    void compareLastStudyTime(User user);

    Integer getTodayNewWordsCount(Integer userId);

    Integer getTodayReviewWordsCount(Integer userId);

    void reviewCompleteWord(Integer userId, Integer wordId);
}
