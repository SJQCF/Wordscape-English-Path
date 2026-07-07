package com.sjqcf.service.impl;

import com.sjqcf.service.AIService;
import com.volcengine.ark.runtime.model.responses.response.ResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sjqcf.utils.DoubaoAIOperator;

@Service
public class AiServiceImpl implements AIService {

    @Autowired
    DoubaoAIOperator doubaoAIOperator;

    @Override
    public ResponseObject getAnswer(String pictureUrl) {
        return doubaoAIOperator.getAnswerByPicture(pictureUrl);
    }

    @Override
    public ResponseObject createArticle(String require) {
        return doubaoAIOperator.createArticle(require);
    }

    @Override
    public ResponseObject createArticleWithPicture(String pictureUrl, String require) {
        return doubaoAIOperator.createArticleWithPicture(pictureUrl, require);
    }

    @Override
    public ResponseObject translateText(String text) {
        return doubaoAIOperator.translateText(text);
    }

    @Override
    public ResponseObject getAnswerWithQuestion(String pictureUrl) {
        return doubaoAIOperator.getAnswerWithQuestion(pictureUrl);
    }
}
