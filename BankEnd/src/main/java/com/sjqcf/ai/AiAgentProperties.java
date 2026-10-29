package com.sjqcf.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.ai.agent")
public class AiAgentProperties {
    /**
     * 单次最多交给模型的消息条数，包含摘要消息。
     */
    private int memorySize = 20;
    /**
     * 未摘要消息达到该条数后触发压缩。
     */
    private int summaryTriggerMessages = 40;
    /**
     * 摘要后保留的最近消息条数，这些消息继续以原文进入上下文。
     */
    private int summaryKeepMessages = 10;
    /**
     * 单次摘要最多处理的消息条数，避免一次读取无限历史。
     */
    private int summaryBatchSize = 100;
    /**
     * 摘要文本的最大字符数。
     */
    private int maxSummaryChars = 6000;
    private String systemPrompt;
}
