package com.sjqcf.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 统一 AI 接入层配置。
 *
 * 所有 provider 都走 OpenAI 兼容协议，差异只体现在 base-url、api-key 和 model 上，
 * 因此不需要为每个厂商写适配代码。厂商扩展参数通过 extra-body / extra-headers 透传。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    /** 未显式指定 providerId 时使用的默认 AI。 */
    private String defaultProvider = "ark";

    /** 识图、识题等固定场景使用的 AI，不向用户开放切换。 */
    private String visionProvider = "ark";

    private Map<String, ProviderConfig> providers = new LinkedHashMap<>();

    public enum Capability {
        /** 可作为普通对话模型使用。 */
        CHAT,
        /** 支持 function calling，可用于 Agent 工具调用。 */
        TOOLS
    }

    @Data
    public static class ProviderConfig {

        /** 展示给用户的名称。 */
        private String displayName;

        /** OpenAI 兼容端点，例如 https://api.deepseek.com */
        private String baseUrl;

        private String apiKey;

        /** 对话默认模型。 */
        private String model;

        /** 可选：识图模型。为空时识图复用 model。 */
        private String visionModel;

        private Double temperature;

        private Duration timeout = Duration.ofSeconds(60);

        private Integer maxRetries = 3;

        /** 该 AI 具备的能力，默认支持对话与工具调用。 */
        private Set<Capability> capabilities = new LinkedHashSet<>(Set.of(Capability.CHAT, Capability.TOOLS));

        /** 是否出现在用户可切换列表中。 */
        private boolean userSelectable = true;

        /** 厂商扩展请求参数，原样透传到 OpenAI 请求体，例如方舟的 thinking。 */
        private Map<String, Object> extraBody = new LinkedHashMap<>();

        /** 厂商扩展请求头。 */
        private Map<String, String> extraHeaders = new LinkedHashMap<>();

        /** 深度思考按次开关的厂商表达方式。为空表示该 provider 不支持按次切换。 */
        private ThinkingConfig thinking;

        public boolean supports(Capability capability) {
            return capabilities != null && capabilities.contains(capability);
        }

        public boolean supportsThinking() {
            return thinking != null;
        }
    }

    /**
     * 深度思考开关的厂商表达方式。模型名和请求体字段可以只配一种，也可以同时配；
     * 具体用哪种取决于厂商：有的靠切模型，有的靠请求体字段。
     */
    @Data
    public static class ThinkingConfig {

        /** 开启思考时切换到的模型，例如 deepseek-reasoner。 */
        private String enabledModel;

        /** 关闭思考时切换到的模型，例如 deepseek-chat。 */
        private String disabledModel;

        /** 开启思考时合并进请求体的字段。 */
        private Map<String, Object> enabledBody = new LinkedHashMap<>();

        /** 关闭思考时合并进请求体的字段。 */
        private Map<String, Object> disabledBody = new LinkedHashMap<>();
    }
}
