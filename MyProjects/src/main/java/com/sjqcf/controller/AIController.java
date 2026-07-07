package com.sjqcf.controller;

import com.sjqcf.pojo.Result;
import com.sjqcf.service.AIService;
import com.sjqcf.service.VoiceSessionRegistry;
import com.sjqcf.utils.AliyunOSSOperator;
import com.sjqcf.utils.AIVoice.*;
import com.volcengine.ark.runtime.model.responses.response.ResponseObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private VoiceSessionRegistry voiceSessionRegistry;

    @Autowired
    private AliyunOSSOperator aliyunOSSOperator = new AliyunOSSOperator();

    /**
     * 获取图片中的各种物体的名称和翻译
     */
    @PostMapping("/getAnswerByPicture")
    public Result getAnswerByPicture(MultipartFile file) throws Exception {
        log.info("获取图片中的各种物体的名称和翻译");
        byte[] fileBytes = file.getBytes();// 获取文件字节数组
        String pictureUrl = aliyunOSSOperator.upload(fileBytes, file.getOriginalFilename());
        String answer = aiService.getAnswer(pictureUrl).getOutput().toString();
        return Result.success(answer);
    }

    /**
     * ai创作文章(无图片)
     */
    @GetMapping("/createArticle")
    public Result createArticle(String require) {
        log.info("ai创作文章,require:{}", require);
        String answer = String.valueOf(aiService.createArticle(require));
        log.info("ai创作文章完毕");
        return Result.success(answer);
    }

    /**
     * ai创作文章(有图片)
     */
    @PostMapping("/createArticleWithPicture")
    public Result createArticleWithPicture(MultipartFile file, String require) throws Exception {
        log.info("ai创作文章(有图片),require:{}", require);
        byte[] fileBytes = file.getBytes();// 获取文件字节数组
        String pictureUrl = aliyunOSSOperator.upload(fileBytes, file.getOriginalFilename());
        String answer = aiService.createArticleWithPicture(pictureUrl,require).getOutput().toString();
        log.info("ai创作文章(有图片)完毕");
        return Result.success(answer);
    }

    /**
     * 开始AI口语练习会话
     */
    @PostMapping("/startVoiceChat")
    public Result startVoiceChat(@RequestBody Map<String, String> params) {
        try {
            log.info("开始AI口语练习会话");

            String mode = params.getOrDefault("mode", "browser");
            String audioFilePath = params.get("audioFilePath");
            String pcmFormat = params.getOrDefault("pcmFormat", "pcm_s16le");

            String botName = params.getOrDefault("botName", "口语老师");
            String systemRole = params.getOrDefault("systemRole", "你是一位专业的英语口语老师，帮助学生练习日常对话。你的第一句话是'May I help you?'");
            String speakingStyle = params.getOrDefault("speakingStyle", "语速适中，发音清晰，耐心友好");
            String speaker = params.get("speaker");
            
            if (speaker == null || speaker.isEmpty()) {
                speaker = Config.DEFAULT_SPEAKER;
            } else if (!isValidSpeaker(speaker)) {
                log.warn("无效的音色ID: {}, 使用默认音色", speaker);
                speaker = Config.DEFAULT_SPEAKER;
            }

            if (mode == null || mode.isEmpty()) {
                mode = "browser";
            }

            String sessionId = Protocol.generateSessionId();

            Config.setMod(mode);
            if (audioFilePath != null && !audioFilePath.isEmpty()) {
                Config.setAudioFilePath(audioFilePath);
            } else {
                Config.setAudioFilePath("");
            }
            Config.setPcmFormat(pcmFormat);

            CallManager callManager = new CallManager(botName, systemRole, speakingStyle, speaker, mode);
            callManager.setVoiceClientSessionId(sessionId);
            CountDownLatch volcanoLatch = new CountDownLatch(1);
            callManager.setVolcanoReadyLatch(volcanoLatch);

            new Thread(() -> {
                try {
                    callManager.start();
                } catch (javax.sound.sampled.LineUnavailableException e) {
                    log.error("❌ 麦克风设备不可用: {}", e.getMessage());
                    log.error("云上请使用 mode=browser，由浏览器采集麦克风并走 WebSocket 上行。");
                    voiceSessionRegistry.remove(sessionId);
                    callManager.releaseVolcanoLatchIfPending();
                } catch (Exception e) {
                    log.error("语音会话执行出错: ", e);
                    voiceSessionRegistry.remove(sessionId);
                    callManager.releaseVolcanoLatchIfPending();
                }
            }).start();

            try {
                if (!volcanoLatch.await(30, TimeUnit.SECONDS)) {
                    log.error("火山实时语音会话未在时限内就绪");
                    try {
                        callManager.stop();
                    } catch (Exception ignored) {
                    }
                    voiceSessionRegistry.remove(sessionId);
                    return Result.error("语音服务连接超时，请稍后重试");
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                try {
                    callManager.stop();
                } catch (Exception ignored) {
                }
                voiceSessionRegistry.remove(sessionId);
                return Result.error("启动语音会话被中断");
            }

            voiceSessionRegistry.put(sessionId, callManager);

            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", sessionId);
            response.put("status", "started");
            response.put("mode", mode);
            response.put("botName", botName);
            response.put("speaker", speaker);

            log.info("会话启动成功，sessionId: {}, botName: {}", sessionId, botName);
            return Result.success(response);

        } catch (Exception e) {
            log.error("启动语音会话失败: ", e);
            return Result.error("启动语音会话失败: " + e.getMessage());
        }
    }
    
    private boolean isValidSpeaker(String speaker) {
        if (speaker == null || speaker.isEmpty()) {
            return false;
        }
        
        String[] validSpeakers = {
            "zh_male_yunzhou_jupiter_bigtts",
            "zh_female_shuangkuai_jupiter_bigtts",
            "zh_male_wennuan_jupiter_bigtts",
            "zh_female_tianmei_jupiter_bigtts",
            "zh_male_chengshu_jupiter_bigtts",
            "zh_female_qingxin_jupiter_bigtts"
        };
        
        for (String valid : validSpeakers) {
            if (valid.equals(speaker)) {
                return true;
            }
        }
        
        return false;
    }


    /**
     * 结束语音会话
     */
    @PostMapping("/endVoiceChat/{sessionId}")
    public Result endVoiceChat(@PathVariable String sessionId) {
        try {
            CallManager callManager = voiceSessionRegistry.get(sessionId);
            if (callManager != null) {
                callManager.stop();
                voiceSessionRegistry.remove(sessionId);
                log.info("会话 {} 已结束", sessionId);
                return Result.success("会话已结束");
            } else {
                return Result.error("会话不存在");
            }
        } catch (Exception e) {
            log.error("结束会话失败: ", e);
            return Result.error("结束会话失败: " + e.getMessage());
        }
    }

    /**
     * 获取会话状态
     */
    @GetMapping("/getSessionStatus/{sessionId}")
    public Result getSessionStatus(@PathVariable String sessionId) {
        CallManager callManager = voiceSessionRegistry.get(sessionId);
        Map<String, Object> status = new HashMap<>();
        status.put("sessionId", sessionId);
        status.put("isActive", callManager != null);

        if (callManager != null) {
            status.put("status", "active");
            status.put("isRunning", callManager.isSessionRunning());
        } else {
            status.put("status", "inactive");
        }

        return Result.success(status);
    }

    /**
     * 获取所有活跃会话列表
     */
    @GetMapping("/getActiveSessions")
    public Result getActiveSessions() {
        Map<String, Object> response = new HashMap<>();
        response.put("activeSessionCount", voiceSessionRegistry.size());
        response.put("sessions", voiceSessionRegistry.sessionIds());
        return Result.success(response);
    }

    /**
     * ai翻译文本
     */
    @PostMapping("/translateText")
    public Result translateText(String text) {
        log.info("ai翻译文本:{}", text);
        ResponseObject responseObject = aiService.translateText(text);
        log.info("ai翻译文本完毕，翻译结果为：{}", responseObject.getOutput().toString());
        return Result.success(responseObject.getOutput().toString());
    }

    /**
     * ai识题
     */
    @PostMapping("/getAnswerWithQuestion")
    public Result getAnswerWithQuestion(MultipartFile file) throws Exception {
        log.info("ai识题");
        byte[] fileBytes = file.getBytes();// 获取文件字节数组
        String pictureUrl = aliyunOSSOperator.upload(fileBytes, file.getOriginalFilename());
        String answer = aiService.getAnswerWithQuestion(pictureUrl).getOutput().toString();
        log.info("ai识题完毕，识题结果为：{}", answer);
        return Result.success(answer);
    }
}