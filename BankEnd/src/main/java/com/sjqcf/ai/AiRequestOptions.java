package com.sjqcf.ai;

/**
 * 一次 AI 调用的中立参数。调用方只表达意图，不出现任何厂商字段名，
 * 具体翻译成模型名还是请求体字段由 {@link ChatClientRegistry#chatOptionsBuilder(String, AiRequestOptions)} 完成。
 */
public record AiRequestOptions(Boolean deepThinking) {

    public static AiRequestOptions none() {
        return new AiRequestOptions(null);
    }
}
