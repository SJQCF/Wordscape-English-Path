package com.sjqcf.utils.AIVoice;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;
// 请求载荷类
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestPayloads {
    
    // StartSession请求载荷
    public static class StartSessionPayload {
        public ASRPayload asr;
        public TTSPayload tts;
        public DialogPayload dialog;
        
        public StartSessionPayload() {
            this.asr = new ASRPayload();
            this.tts = new TTSPayload();
            this.dialog = new DialogPayload();
        }
    }
    
    public static class ASRPayload {
        public Map<String, Object> extra = new HashMap<>();
    }
    
    public static class TTSPayload {
        public String speaker = Config.speaker;  // 使用动态配置
        public AudioConfig audio_config = new AudioConfig();
    }

    public static class VoiceCloneConfig {
        public boolean enable = true;
        public String clone_id = "zh_female_xiaohe_jupiter_bigtts";  // 克隆音色的 ID
        public String text = "";      // 用于克隆的文本（可选）

        public void setCloneId(String cloneId) {
            this.clone_id = cloneId;
        }

        public void enableClone(boolean enable) {
            this.enable = enable;
        }
    }

    public static class AudioConfig {
        public int channel = 1;
        public String format = Config.pcmFormat;
        public int sample_rate = Config.OUTPUT_SAMPLE_RATE;
    }
    
    public static class DialogPayload {
        public String dialog_id = "";
        public String bot_name = Config.botName;  // 使用动态配置
        public String system_role = Config.systemRole;  // 使用动态配置
        public String speaking_style = Config.speakingStyle;  // 使用动态配置
        public LocationInfo location = new LocationInfo();
        public Map<String, Object> extra = new HashMap<>();
    }
    
    public static class LocationInfo {
        public double longitude = 0.0;
        public double latitude = 0.0;
        public String city = "北京";
        public String country = "中国";
        public String province = "北京";
        public String district = "";
        public String town = "";
        public String country_code = "CN";
        public String address = "";
    }
    
    // SayHello请求载荷
    public static class SayHelloPayload {
        public String content;
        
        public SayHelloPayload(String content) {
            this.content = content;
        }
    }
    
    // ChatTextQuery请求载荷
    public static class ChatTextQueryPayload {
        public String content;
        
        public ChatTextQueryPayload(String content) {
            this.content = content;
        }
    }
    
    // ChatTTSText请求载荷
    public static class ChatTTSTextPayload {
        public boolean start;
        public boolean end;
        public String content;
        
        public ChatTTSTextPayload(boolean start, boolean end, String content) {
            this.start = start;
            this.end = end;
            this.content = content;
        }
    }
    
    // ChatRAGText请求载荷
    public static class ChatRAGTextPayload {
        public String external_rag;
        
        public ChatRAGTextPayload(String externalRAG) {
            this.external_rag = externalRAG;
        }
    }
}