package com.sjqcf.ai;

import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientAsync;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.setup.OpenAiSetup;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 统一 AI 接入层的核心：按配置为每个 provider 构建 ChatClient。
 *
 * 对外只暴露 {@link #chat(String)}、{@link #vision()} 和 {@link #listProviders()}，
 * 调用方不感知具体厂商。需要厂商特性时用 {@link #rawClient(String)} 拿到底层 SDK 客户端。
 */
@Component
public class ChatClientRegistry {

    private static final Logger log = LoggerFactory.getLogger(ChatClientRegistry.class);

    private final AiProperties properties;
    private final Map<String, ProviderRuntime> runtimes;
    private final ObservationRegistry observationRegistry;
    private final MeterRegistry meterRegistry;

    public ChatClientRegistry(AiProperties properties,
                              ObjectProvider<ToolCallingManager> toolCallingManagerProvider,
                              ObjectProvider<ObservationRegistry> observationRegistryProvider,
                              ObjectProvider<MeterRegistry> meterRegistryProvider) {
        this.properties = properties;
        ToolCallingManager toolCallingManager = toolCallingManagerProvider
                .getIfAvailable(() -> ToolCallingManager.builder().build());
        this.observationRegistry = observationRegistryProvider.getIfAvailable(() -> ObservationRegistry.NOOP);
        this.meterRegistry = meterRegistryProvider.getIfAvailable(SimpleMeterRegistry::new);

        Map<String, ProviderRuntime> built = new LinkedHashMap<>();
        properties.getProviders().forEach((id, config) -> built.put(id, build(id, config, toolCallingManager)));
        this.runtimes = Map.copyOf(built);

        requireProvider(properties.getDefaultProvider(), "default-provider");
        requireProvider(properties.getVisionProvider(), "vision-provider");
        log.info("统一 AI 接入层加载完成，provider={}，默认={}，识图={}",
                runtimes.keySet(), properties.getDefaultProvider(), properties.getVisionProvider());
    }

    /** 按 providerId 取对话客户端，providerId 为空时使用默认 provider。 */
    public ChatClient chat(String providerId) {
        return runtime(providerId).chatClient();
    }

    /** 默认 provider 的对话客户端。 */
    public ChatClient chat() {
        return chat(properties.getDefaultProvider());
    }

    /** 识图专用客户端，固定在 vision-provider 上，不受用户选择影响。 */
    public ChatClient vision() {
        return runtime(properties.getVisionProvider()).visionClient();
    }

    /**
     * 把一次调用的中立参数解析为具体的 OpenAI 兼容选项。
     *
     * 调用方只表达"要不要深度思考"，由这里按 provider 配置翻译成厂商需要的模型名或请求体字段，
     * 同时把 provider 的默认 model / temperature / extra-body 一起合并进来，避免请求级选项覆盖掉默认值。
     */
    public OpenAiChatOptions.Builder chatOptionsBuilder(String providerId, AiRequestOptions requestOptions) {
        ProviderRuntime runtime = runtime(providerId);
        AiProperties.ProviderConfig config = properties.getProviders().get(runtime.id());

        OpenAiChatOptions.Builder builder = OpenAiChatOptions.builder().model(runtime.model());
        if (config.getTemperature() != null) {
            builder.temperature(config.getTemperature());
        }

        Map<String, Object> extraBody = new LinkedHashMap<>();
        if (config.getExtraBody() != null) {
            deepMerge(extraBody, config.getExtraBody());
        }

        Boolean deepThinking = requestOptions == null ? null : requestOptions.deepThinking();
        if (deepThinking != null) {
            AiProperties.ThinkingConfig thinking = config.getThinking();
            if (thinking == null) {
                throw new IllegalArgumentException(
                        "AI [" + runtime.displayName() + "] 不支持按次切换深度思考");
            }
            String model = deepThinking ? thinking.getEnabledModel() : thinking.getDisabledModel();
            if (model != null && !model.isBlank()) {
                builder.model(model);
            }
            deepMerge(extraBody, deepThinking ? thinking.getEnabledBody() : thinking.getDisabledBody());
        }

        if (!extraBody.isEmpty()) {
            builder.extraBody(extraBody);
        }
        return builder;
    }

    /** 递归合并厂商扩展字段，避免请求级参数把 provider 的默认字段整体覆盖。 */
    @SuppressWarnings("unchecked")
    private static void deepMerge(Map<String, Object> target, Map<String, ?> source) {
        if (source == null || source.isEmpty()) {
            return;
        }
        source.forEach((key, value) -> {
            Object existing = target.get(key);
            if (existing instanceof Map && value instanceof Map) {
                deepMerge((Map<String, Object>) existing, (Map<String, ?>) value);
            } else if (value != null) {
                target.put(key, value);
            }
        });
    }

    /** 厂商原生 SDK 客户端，用于统一层覆盖不到的厂商特性。 */
    public OpenAIClient rawClient(String providerId) {
        return runtime(providerId).rawClient();
    }

    public ProviderRuntime requireRuntime(String providerId) {
        return runtime(providerId);
    }

    public String defaultProvider() {
        return properties.getDefaultProvider();
    }

    /** 提供给前端的可选 AI 列表，只包含 id、名称和能力，不泄露密钥与地址。 */
    public List<ProviderInfo> listProviders() {
        return runtimes.values().stream()
                .filter(ProviderRuntime::userSelectable)
                .map(runtime -> new ProviderInfo(
                        runtime.id(),
                        runtime.displayName(),
                        runtime.model(),
                        runtime.capabilities(),
                        runtime.hasVision(),
                        runtime.hasThinking()))
                .toList();
    }

    private ProviderRuntime runtime(String providerId) {
        String id = (providerId == null || providerId.isBlank()) ? properties.getDefaultProvider() : providerId;
        ProviderRuntime runtime = runtimes.get(id);
        if (runtime == null) {
            throw new IllegalArgumentException("未知的 AI: " + id + "，可选值: " + runtimes.keySet());
        }
        return runtime;
    }

    private void requireProvider(String id, String property) {
        if (id == null || !runtimes.containsKey(id)) {
            throw new IllegalStateException("app.ai." + property + " 指向未定义的 provider: " + id
                    + "，已配置: " + runtimes.keySet());
        }
    }

    private ProviderRuntime build(String id, AiProperties.ProviderConfig config, ToolCallingManager toolCallingManager) {
        if (config.getBaseUrl() == null || config.getBaseUrl().isBlank()) {
            throw new IllegalStateException("provider [" + id + "] 缺少 base-url");
        }
        if (config.getModel() == null || config.getModel().isBlank()) {
            throw new IllegalStateException("provider [" + id + "] 缺少 model");
        }

        OpenAIClient rawClient = buildSyncClient(config);
        OpenAIClientAsync asyncClient = buildAsyncClient(config);
        ChatClient chatClient = ChatClient.builder(
                buildModel(config, config.getModel(), rawClient, asyncClient, toolCallingManager)).build();

        String visionModel = config.getVisionModel();
        boolean hasVision = visionModel != null && !visionModel.isBlank() && !visionModel.equals(config.getModel());
        ChatClient visionClient = hasVision
                ? ChatClient.builder(buildModel(config, visionModel, rawClient, asyncClient, toolCallingManager)).build()
                : chatClient;

        String displayName = (config.getDisplayName() == null || config.getDisplayName().isBlank())
                ? id : config.getDisplayName();

        Set<AiProperties.Capability> capabilities = config.getCapabilities() == null
                ? Set.of(AiProperties.Capability.CHAT)
                : Set.copyOf(config.getCapabilities());

        return new ProviderRuntime(id, displayName, config.getModel(), visionModel, hasVision,
                capabilities, config.isUserSelectable(),
                config.supportsThinking(),
                chatClient, visionClient, rawClient);
    }

    private OpenAIClient buildSyncClient(AiProperties.ProviderConfig config) {
        // 复用 Spring AI 自己的客户端装配逻辑，避免依赖未随 starter 传递的 okhttp 客户端模块。
        return OpenAiSetup.setupSyncClient(
                config.getBaseUrl(),
                config.getApiKey(),
                null,                                   // credential
                null,                                   // azureDeploymentName
                null,                                   // azureOpenAiServiceVersion
                null,                                   // organizationId
                false,                                  // isAzure
                false,                                  // isGitHubModels
                config.getModel(),                      // modelName
                config.getTimeout() == null ? Duration.ofSeconds(60) : config.getTimeout(),
                config.getMaxRetries() == null ? 3 : config.getMaxRetries(),
                null,                                   // proxy
                config.getExtraHeaders() == null ? Map.of() : config.getExtraHeaders(),
                observationRegistry,
                meterRegistry,
                List.of());                             // httpClientCustomizers
    }

    private OpenAIClientAsync buildAsyncClient(AiProperties.ProviderConfig config) {
        // 必须和同步客户端一起传入：只传同步客户端时，Spring AI 会拿不含凭证的 options 懒建异步客户端并报错。
        return OpenAiSetup.setupAsyncClient(
                config.getBaseUrl(),
                config.getApiKey(),
                null,
                null,
                null,
                null,
                false,
                false,
                config.getModel(),
                config.getTimeout() == null ? Duration.ofSeconds(60) : config.getTimeout(),
                config.getMaxRetries() == null ? 3 : config.getMaxRetries(),
                null,
                config.getExtraHeaders() == null ? Map.of() : config.getExtraHeaders(),
                observationRegistry,
                meterRegistry,
                List.of());
    }

    private OpenAiChatModel buildModel(AiProperties.ProviderConfig config, String model,
                                       OpenAIClient rawClient, OpenAIClientAsync asyncClient,
                                       ToolCallingManager toolCallingManager) {
        OpenAiChatOptions.Builder options = OpenAiChatOptions.builder().model(model);
        if (config.getTemperature() != null) {
            options.temperature(config.getTemperature());
        }
        if (config.getExtraBody() != null && !config.getExtraBody().isEmpty()) {
            options.extraBody(config.getExtraBody());
        }
        return OpenAiChatModel.builder()
                .openAiClient(rawClient)
                .openAiClientAsync(asyncClient)
                .options(options.build())
                .toolCallingManager(toolCallingManager)
                .build();
    }

    public record ProviderInfo(String id, String displayName, String model,
                               Set<AiProperties.Capability> capabilities, boolean vision,
                               boolean thinking) {
    }

    public record ProviderRuntime(String id, String displayName, String model, String visionModel,
                                  boolean hasVision, Set<AiProperties.Capability> capabilities,
                                  boolean userSelectable, boolean hasThinking,
                                  ChatClient chatClient, ChatClient visionClient, OpenAIClient rawClient) {
    }
}
