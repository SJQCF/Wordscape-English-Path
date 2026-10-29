package com.sjqcf.service.impl;

import com.sjqcf.ai.AiPromptCatalog;
import com.sjqcf.ai.ChatClientRegistry;
import com.sjqcf.service.AIService;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Locale;

@Service
public class AiServiceImpl implements AIService {

    private final ChatClientRegistry chatClientRegistry;

    public AiServiceImpl(ChatClientRegistry chatClientRegistry) {
        this.chatClientRegistry = chatClientRegistry;
    }

    @Override
    public String getAnswer(String pictureUrl) {
        return callWithImage(AiPromptCatalog.PICTURE_OBJECTS, pictureUrl);
    }

    @Override
    public String createArticle(String require) {
        return call(AiPromptCatalog.article(require));
    }

    @Override
    public String createArticleWithPicture(String pictureUrl, String require) {
        return callWithImage(AiPromptCatalog.article(require), pictureUrl);
    }

    @Override
    public String translateText(String text) {
        return call(AiPromptCatalog.TRANSLATION_SYSTEM + "\n待翻译文本：\n" + text);
    }

    @Override
    public String getAnswerWithQuestion(String pictureUrl) {
        return callWithImage(AiPromptCatalog.QUESTION_SOLVING, pictureUrl);
    }

    private String call(String prompt) {
        String content = chatClientRegistry.chat().prompt()
                .user(prompt)
                .call()
                .content();
        return requireContent(content);
    }

    private String callWithImage(String prompt, String pictureUrl) {
        UserMessage message = UserMessage.builder()
                .text(prompt)
                .media(new Media(resolveImageType(pictureUrl), URI.create(pictureUrl)))
                .build();

        String content = chatClientRegistry.vision().prompt()
                .messages(message)
                .call()
                .content();
        return requireContent(content);
    }

    private String requireContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("AI 未返回有效内容");
        }
        return content;
    }

    private MediaType resolveImageType(String pictureUrl) {
        String lowerUrl = pictureUrl.toLowerCase(Locale.ROOT);
        if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }
        if (lowerUrl.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        if (lowerUrl.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.IMAGE_PNG;
    }
}
