package com.sjqcf.controller;

import com.sjqcf.ai.AgentConversationService;
import com.sjqcf.ai.AgentChatRequest;
import com.sjqcf.ai.ChatClientRegistry;
import com.sjqcf.ai.LearningAgentService;
import com.sjqcf.pojo.Result;
import com.sjqcf.service.AIService;
import com.sjqcf.utils.AliyunOSSOperator;
import com.sjqcf.utils.AIVoice.*;
import com.sjqcf.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private LearningAgentService learningAgentService;

    @Autowired
    private ChatClientRegistry chatClientRegistry;

    @Autowired
    private AgentConversationService agentConversationService;

    @Autowired
    private AliyunOSSOperator aliyunOSSOperator = new AliyunOSSOperator();

    // 存储活跃的通话会话
    private static final Map<String, CallManager> activeSessions = new ConcurrentHashMap<>();

    /**
     * 获取图片中的各种物体的名称和翻译
     */
    @PostMapping("/getAnswerByPicture")
    public Result getAnswerByPicture(MultipartFile file) throws Exception {
        log.info("获取图片中的各种物体的名称和翻译");
        byte[] fileBytes = file.getBytes();// 获取文件字节数组
        String pictureUrl = aliyunOSSOperator.upload(fileBytes, file.getOriginalFilename());
        String answer = aiService.getAnswer(pictureUrl);
        return Result.success(answer);
    }

    /**
     * ai创作文章(无图片)
     */
    @GetMapping("/createArticle")
    public Result createArticle(String require) {
        log.info("ai创作文章,require:{}", require);
        String answer = aiService.createArticle(require);
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
        String answer = aiService.createArticleWithPicture(pictureUrl, require);
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

            String mode = params.getOrDefault("mode", "text");
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
                mode = "text";
            }

            String sessionId = Protocol.generateSessionId();

            Config.setMod(mode);
            if (audioFilePath != null && !audioFilePath.isEmpty()) {
                Config.setAudioFilePath(audioFilePath);
            } else {
                Config.setAudioFilePath("");
            }
            Config.setPcmFormat(pcmFormat);

            CallManager callManager = new CallManager(botName, systemRole, speakingStyle, speaker);
            activeSessions.put(sessionId, callManager);

            new Thread(() -> {
                try {
                    callManager.start();
                } catch (javax.sound.sampled.LineUnavailableException e) {
                    log.error("❌ 麦克风设备不可用: {}", e.getMessage());
                    log.error("");
                    log.error("💡 解决方案:");
                    log.error("   选项1 - 使用文本模式 (推荐用于服务器/Docker):");
                    log.error("   POST /ai/startVoiceChat");
                    log.error("   {{\"mode\": \"text\", \"botName\": \"口语老师\", \"systemRole\": \"...\"}}");
                    log.error("");
                    log.error("   选项2 - 使用音频文件:");
                    log.error("   POST /ai/startVoiceChat");
                    log.error("   {{\"mode\": \"audio\", \"audioFilePath\": \"/path/to/audio.wav\"}}");
                    log.error("");
                    log.error("   选项3 - Docker容器添加音频设备:");
                    log.error("   docker run --device /dev/snd:/dev/snd --group-add audio ...");
                    log.error("");
                    activeSessions.remove(sessionId);
                } catch (Exception e) {
                    log.error("语音会话执行出错: ", e);
                    activeSessions.remove(sessionId);
                }
            }).start();

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
            CallManager callManager = activeSessions.get(sessionId);
            if (callManager != null) {
                callManager.stop();
                activeSessions.remove(sessionId);
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
        CallManager callManager = activeSessions.get(sessionId);
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
        response.put("activeSessionCount", activeSessions.size());
        response.put("sessions", activeSessions.keySet());
        return Result.success(response);
    }

    /**
     * ai翻译文本
     */
    @PostMapping("/translateText")
    public Result translateText(String text) {
        log.info("ai翻译文本:{}", text);
        String answer = aiService.translateText(text);
        log.info("ai翻译文本完毕，翻译结果为：{}", answer);
        return Result.success(answer);
    }

    /**
     * ai识题
     */
    @PostMapping("/getAnswerWithQuestion")
    public Result getAnswerWithQuestion(MultipartFile file) throws Exception {
        log.info("ai识题");
        byte[] fileBytes = file.getBytes();// 获取文件字节数组
        String pictureUrl = aliyunOSSOperator.upload(fileBytes, file.getOriginalFilename());
        String answer = aiService.getAnswerWithQuestion(pictureUrl);
        log.info("ai识题完毕，识题结果为：{}", answer);
        return Result.success(answer);
    }

    /**
     * 与学习规划 Agent 对话。相同 sessionId 会保留上下文记忆。
     */
    @PostMapping("/agent/chat")
    public Result chatWithAgent(@RequestBody AgentChatRequest request,
                                @RequestHeader(value = "token", required = false) String token) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return Result.error("message 不能为空");
        }

        Integer userId = currentUserId(token);
        if (userId == null) {
            return Result.error("未登录或登录已过期");
        }

        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = java.util.UUID.randomUUID().toString();
        }

        log.info("Agent 对话开始，sessionId: {}, providerId: {}, deepThinking: {}",
                sessionId, request.getProviderId(), request.getDeepThinking());
        String answer;
        try {
            answer = learningAgentService.chat(
                    sessionId,
                    userId,
                    request.getMessage(),
                    request.getProviderId(),
                    request.getDeepThinking());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(e.getMessage());
        }
        log.info("Agent 对话完成，sessionId: {}", sessionId);

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("answer", answer);
        return Result.success(response);
    }

    /**
     * 当前用户的历史会话列表，按最后活跃时间倒序。
     */
    @GetMapping("/agent/conversations")
    public Result listAgentConversations(@RequestHeader(value = "token", required = false) String token) {
        Integer userId = currentUserId(token);
        if (userId == null) {
            return Result.error("未登录或登录已过期");
        }
        return Result.success(agentConversationService.list(userId));
    }

    /**
     * 读取某个历史会话的全部消息，用于前端回看并继续对话。
     */
    @GetMapping("/agent/conversations/{sessionId}")
    public Result getAgentConversation(@PathVariable String sessionId,
                                       @RequestHeader(value = "token", required = false) String token) {
        try {
            return Result.success(agentConversationService.messages(sessionId, currentUserId(token)));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 可切换的 AI 列表。只返回 id、名称、模型和能力，不含密钥与接口地址。
     */
    @GetMapping("/providers")
    public Result listProviders() {
        return Result.success(chatClientRegistry.listProviders());
    }

    /**
     * 清空一个 Agent 会话的短期记忆。
     */
    @DeleteMapping("/agent/chat/{sessionId}")
    public Result clearAgentConversation(@PathVariable String sessionId,
                                         @RequestHeader(value = "token", required = false) String token) {
        try {
            agentConversationService.delete(sessionId, currentUserId(token));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
        return Result.success();
    }

    /**
     * 从 JWT 解析当前用户 ID。TokenInterceptor 已经校验过签名，这里只取声明。
     */
    private Integer currentUserId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            Claims claims = JwtUtils.parseJwt(token);
            Object id = claims.get("id");
            if (id instanceof Number number) {
                return number.intValue();
            }
            return id == null ? null : Integer.valueOf(id.toString());
        } catch (Exception e) {
            log.warn("解析 token 获取用户 ID 失败: {}", e.getMessage());
            return null;
        }
    }
}
