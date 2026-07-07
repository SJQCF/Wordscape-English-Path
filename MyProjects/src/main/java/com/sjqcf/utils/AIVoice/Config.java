package com.sjqcf.utils.AIVoice;
// 配置类
public class Config {
    // WebSocket连接配置
    public static final String WS_URL = "wss://openspeech.bytedance.com/api/v3/realtime/dialogue";
    public static final String API_RESOURCE_ID = "volc.speech.dialog";

    // 用户需要配置的参数
    public static String API_APP_ID = "4397878811";
    public static String API_ACCESS_KEY = "NepQmweXDhfkPjHMr8DhAZHa-IJx9hVk";
    public static String API_APP_KEY = "PlgvMymc7f3tQnJ6";

    // 音频参数配置
    public static final int INPUT_SAMPLE_RATE = 16000;
    public static final int OUTPUT_SAMPLE_RATE = 24000;
    public static final int CHANNELS = 1;
    public static final int INPUT_FRAMES_PER_BUFFER = 160;
    public static final int OUTPUT_FRAMES_PER_BUFFER = 512;
    public static final int BUFFER_SECONDS = 100;

    // 音频格式
    public static final String DEFAULT_PCM = "pcm";
    public static final String PCM_S16LE = "pcm_s16le";

    // TTS 配置
    public static final String DEFAULT_SPEAKER = "zh_male_yunzhou_jupiter_bigtts";
    
    public static String botName = "口语老师";
    public static String systemRole = "你是一位专业的英语口语老师，帮助学生练习日常对话。你的第一句话是'May I help you?'";
    public static String speakingStyle = "语速适中，发音清晰，耐心友好";
    public static String speaker = DEFAULT_SPEAKER;

    // 网络配置
    public static final int AUDIO_CHUNK_SIZE = 640; // 字节，对应20ms音频数据
    public static final long AUDIO_SEND_INTERVAL = 20; // 毫秒

    // WAV文件配置
    public static final int WAV_HEADER_SIZE = 44; // WAV文件头大小

    // 命令行参数默认值
    public static String audioFilePath = "";
    public static String mod = "audio";
    public static String pcmFormat = PCM_S16LE;

    public static void setAppId(String appId) {
        API_APP_ID = appId;
    }

    public static void setAccessKey(String accessKey) {
        API_ACCESS_KEY = accessKey;
    }

    public static void setAudioFilePath(String path) {
        audioFilePath = path;
    }

    public static void setMod(String mode) {
        mod = mode;
    }

    /** 浏览器麦克风经 WebSocket 上行，服务端转发火山（云上部署使用）。 */
    public static boolean isBrowserClientMode() {
        return "browser".equals(mod);
    }

    public static void setPcmFormat(String format) {
        pcmFormat = format;
    }
    
    // 新增设置方法
    public static void setBotName(String name) {
        botName = name;
    }
    
    public static void setSystemRole(String role) {
        systemRole = role;
    }
    
    public static void setSpeakingStyle(String style) {
        speakingStyle = style;
    }
    
    public static void setSpeaker(String speaker) {
        Config.speaker = speaker;
    }
}