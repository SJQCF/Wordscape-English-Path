package com.sjqcf.service;

import com.sjqcf.pojo.Result;
import com.volcengine.ark.runtime.model.responses.response.ResponseObject;

import javax.imageio.ImageWriter;

public interface AIService {
    ResponseObject getAnswer(String pictureUrl);

    ResponseObject createArticle(String require);

    ResponseObject createArticleWithPicture(String pictureUrl, String require);

    ResponseObject translateText(String text);

    ResponseObject getAnswerWithQuestion(String pictureUrl);
}
