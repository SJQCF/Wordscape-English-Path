package com.sjqcf.ai;

import lombok.Data;

@Data
public class AgentChatRequest {
    private String sessionId;
    private String message;
    /** 可选：指定本次对话使用的 AI，为空时使用默认 provider。 */
    private String providerId;
    /** 可选：本次对话是否开启深度思考，null 表示沿用 provider 默认。 */
    private Boolean deepThinking;
}
