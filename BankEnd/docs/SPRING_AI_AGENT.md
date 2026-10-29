# Spring AI Agent 接入说明

## 1. 当前结构

项目使用统一的 AI 接入层：所有厂商都按 OpenAI 兼容协议接入，差异只在
`base-url`、`api-key` 和 `model`。Spring AI 负责模型调用和工具编排。

| 概念 | 当前实现 | 作用 |
| --- | --- | --- |
| 接入层 | `ChatClientRegistry` | 按 `app.ai.providers` 为每个 AI 构建 `ChatClient` |
| 配置 | `AiProperties` | 绑定 `app.ai.*`：默认 AI、识图 AI、各 provider |
| Model | `ChatClient` | 与大模型通信，理解用户意图 |
| Memory | `DatabaseChatMemory` | 消息落库到 `ai_chat_memory`，按 `sessionId` 读取最近 20 条 |
| Tool | `EnglishLearningTools` | 让模型查询词库、课程、听力和图书资源 |
| Agent Loop | `LearningAgentService` | 组织提示词、记忆、模型和工具调用 |
| API | `POST /ai/agent/chat`、`GET /ai/providers` | Agent 对话入口与可选 AI 列表 |

核心调用链：

```text
用户消息
  -> AgentChatRequest.providerId 选择 AI（为空用默认 AI）
  -> MessageChatMemoryAdvisor 读取会话历史
  -> ChatClient 请求对应厂商的 OpenAI 兼容端点
  -> 模型判断是否需要工具
  -> Spring AI 调用 @Tool 方法
  -> 工具结果返回模型
  -> 模型生成最终回答
  -> Advisor 保存本轮对话
```

## 2. 配置

所有 AI 都在 `app.ai.providers` 下声明，密钥通过环境变量注入：

```text
ARK_API_KEY         火山方舟 API Key
ARK_BASE_URL        默认 https://ark.cn-beijing.volces.com/api/v3
ARK_TEXT_MODEL      默认 doubao-seed-1-8-251228
ARK_VISION_MODEL    默认 doubao-seed-1-6-vision-250815
DEEPSEEK_API_KEY    DeepSeek API Key
DEEPSEEK_BASE_URL   默认 https://api.deepseek.com
DEEPSEEK_MODEL      默认 deepseek-chat
```

两个路由开关：

- `app.ai.default-provider`：用户未指定 `providerId` 时使用的 AI。
- `app.ai.vision-provider`：识图、识题固定使用的 AI，不对用户开放切换。

每个 provider 支持：

| 字段 | 说明 |
| --- | --- |
| `display-name` | 展示给用户的名称 |
| `base-url` / `api-key` | OpenAI 兼容端点与密钥 |
| `model` | 对话默认模型 |
| `vision-model` | 可选，识图模型；为空时复用 `model` |
| `temperature` / `timeout` / `max-retries` | 采样与网络参数 |
| `capabilities` | `chat` 可对话；`tools` 支持 function calling |
| `user-selectable` | 是否出现在 `/ai/providers` 列表中 |
| `extra-body` / `extra-headers` | 厂商扩展参数，见第 6 节 |

新增一个 AI 只需要在 `app.ai.providers` 下加一段配置，不用改代码。

## 3. 调用 Agent

首次请求可以只发送消息，服务会生成并返回 `sessionId`：

```http
POST /ai/agent/chat
Content-Type: application/json
token: <JWT>

{
  "message": "我准备四级考试，帮我查一下 hear 的用法，并推荐适合的听力类型"
}
```

切换 AI 时传入 `providerId`（取值来自 `GET /ai/providers`），
携带同一个 `sessionId` 就能保留该会话的最近上下文：

```json
{
  "sessionId": "上一次响应返回的值",
  "message": "再给我一个难度相近的例句",
  "providerId": "deepseek"
}
```

历史对话接口（都按 JWT 里的用户维度隔离，只能访问自己的会话）：

```http
GET    /ai/agent/conversations              # 当前用户的历史会话列表，按最后活跃时间倒序
GET    /ai/agent/conversations/{sessionId}  # 某个会话的全部消息，用于回看并继续对话
DELETE /ai/agent/chat/{sessionId}           # 删除某个会话（消息 + 会话索引），会校验归属
```

继续一个历史会话：带上该会话的 `sessionId` 调用 `POST /ai/agent/chat` 即可，
服务端会从记忆里读出最近的上下文继续推理。

## 4. 现有普通 AI 接口

以下接口已经改为通过统一接入层调用，HTTP 路径和返回结构保持兼容。
它们不接受 `providerId`，其中带图片的三个固定使用 `vision-provider`：

```text
POST /ai/getAnswerByPicture          识图，固定 vision-provider
POST /ai/createArticleWithPicture    带图写作，固定 vision-provider
POST /ai/getAnswerWithQuestion       识题，固定 vision-provider
GET  /ai/createArticle               纯文本，使用默认 AI
POST /ai/translateText               纯文本，使用默认 AI
```

实时语音会话仍使用原有流式音频协议，没有强行改造成普通 ChatClient
请求。语音识别、模型推理和语音合成属于低延迟流式链路，适合后续单独设计。

## 5. 哪些场景可以切换 AI

- 可以切换：`POST /ai/agent/chat`，通过 `providerId` 指定，缺省用 `default-provider`。
- 不能切换：所有识图、识题、带图写作接口，接口层面根本不接收 `providerId`，
  由后端固定在 `vision-provider` 上，前端即使传了也不生效。
- 可选列表：`GET /ai/providers` 只返回 `id`、`displayName`、`model`、`capabilities`，
  不返回密钥和接口地址。
- 能力校验：Agent 需要工具调用，若所选 AI 的 `capabilities` 不含 `tools`，
  服务端会直接拒绝并返回明确错误，不会带着错误配置继续推理。

## 6. 使用厂商特性

统一层只保证 OpenAI 规范内的能力。厂商扩展分三类处理。

1. 请求参数扩展（最常见）：在 provider 下配置 `extra-body`，会合并进请求体。
   例如关闭方舟 doubao-seed-1-8 的深度思考：

   ```yaml
   app:
     ai:
       providers:
         ark:
           extra-body:
             thinking:
               type: disabled
   ```

   其它例子：Qwen 的 `enable_search`、DeepSeek 的 `reasoning_format` 等，
   字段名和取值以厂商文档为准。

2. 请求头扩展：配置 `extra-headers`，用于少数厂商要求的自定义鉴权头或版本头。

3. 响应字段扩展：例如 `deepseek-reasoner` 返回的 `reasoning_content` 不在 OpenAI
   schema 里，Spring AI 默认不会映射。可靠做法是针对这一个场景用 `RestClient`
   自行调用 `/chat/completions` 并解析响应；统一层的 `ChatClientRegistry.rawClient(providerId)`
   暴露了底层 `OpenAIClient`，可以复用同一份 `base-url` / `api-key` / `extra-headers`。

注意事项：

- `extra-body` 是 `Map<String, Object>`，编译期不校验，字段写错要到收到 400 才暴露。
- 非标准能力依赖具体厂商，切换 provider 后不一定可用，界面要结合 `capabilities` 做提示。
- 把厂商特性集中在配置里，业务代码只依赖 `ChatClient`，避免统一层被个别厂商污染。

## 7. 如何继续学习 Agent

建议按下面的顺序迭代，每一步只增加一种能力：

1. 工具：在 `EnglishLearningTools` 增加查用户收藏、学习记录、生成复习计划等只读工具。
2. 记忆：已替换为 MySQL 版 `DatabaseChatMemory`，可继续扩展按用户维度查询历史、加 Redis 缓存等。
3. RAG：把教材、听力原文和历史错题向量化，通过 `QuestionAnswerAdvisor` 检索后回答。
4. MCP：把词库、学习记录等能力暴露为 MCP Server，让不同 Agent 复用同一组工具。
5. 工作流：将“诊断水平 -> 生成计划 -> 布置练习 -> 检查结果”拆成多节点流程。
6. 可观测性：记录每次模型调用、工具参数、工具耗时、Token 和失败原因。

## 8. 设计原则

- 只读工具可以直接提供；修改用户数据的工具必须从 JWT 获取用户身份，不能相信模型传入的 `userId`。
- 工具描述要清楚说明何时使用、参数含义和返回内容，模型依赖描述选择工具。
- 不要把所有业务方法一次性暴露给模型，只暴露完成当前 Agent 目标所需的最小工具集。
- 生产环境要把当前内存记忆替换成可持久化实现，并设置超时、限流、重试和最大工具调用次数。
- 新增 AI 时优先只改配置；确需厂商特化逻辑时放进入口服务，不要扩散到接入层。

## 9. 对话记忆持久化（DatabaseChatMemory）

`ChatMemory` 已从内存版 `MessageWindowChatMemory` 换成自定义实现
`com.sjqcf.ai.memory.DatabaseChatMemory`：消息写入 MySQL 的 `ai_chat_memory` 表，
服务重启后按 `sessionId` 仍能恢复上下文。建表脚本见 `sql/ai_chat_memory.sql`。

消息表本身不知道消息属于谁（`ChatMemory` 只接收 `sessionId`），所以用户归属单独放在
`ai_chat_conversation` 表（建表脚本 `sql/ai_chat_conversation.sql`）：控制器从 JWT 取
`userId`，由 `AgentConversationService` 建立 `sessionId` 与用户的关联，历史列表、
读取和删除都基于这张表做归属校验。

| 环节 | 行为 |
| --- | --- |
| 写入 `add` | 只追加新消息，不覆盖历史；每轮对话写入 USER + ASSISTANT 两条 |
| 读取 `get` | 取该会话最近 `app.ai.agent.memory-size` 条（默认 20）喂给模型 |
| 清空 `clear` | 删除该会话全部消息；`DELETE /ai/agent/chat/{sessionId}` 会走到这里 |

要点：

- 数据库保留完整对话历史，模型上下文只截取最近 N 条，两者解耦。
- 装配在 `AiConfig#learningChatMemory`，注入 `ChatMemoryMapper` 与 `memory-size`；
  自定义实现存在时，Spring AI 默认的内存仓储自动失效。
- 只还原 USER / ASSISTANT / SYSTEM 文本消息；TOOL 消息带结构化响应体，当前跳过。
  本项目的 Agent 记忆不写入 TOOL 消息，不受影响。
- 想调整上下文长度，改 `app.ai.agent.memory-size` 即可，无需改代码。
