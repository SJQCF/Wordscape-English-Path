package com.sjqcf.service;

public interface AIService {
    String getAnswer(String pictureUrl);

    String createArticle(String require);

    String createArticleWithPicture(String pictureUrl, String require);

    String translateText(String text);

    String getAnswerWithQuestion(String pictureUrl);
}
