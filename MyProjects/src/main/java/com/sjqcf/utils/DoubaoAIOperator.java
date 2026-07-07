package com.sjqcf.utils;

import com.volcengine.ark.runtime.model.responses.common.ResponsesCaching;
import com.volcengine.ark.runtime.model.responses.common.ResponsesThinking;
import com.volcengine.ark.runtime.model.responses.constant.ResponsesConstants;
import com.volcengine.ark.runtime.model.responses.content.InputContentItemImage;
import com.volcengine.ark.runtime.model.responses.content.InputContentItemText;
import com.volcengine.ark.runtime.model.responses.item.ItemEasyMessage;
import com.volcengine.ark.runtime.model.responses.item.MessageContent;
import com.volcengine.ark.runtime.model.responses.request.CreateResponsesRequest;
import com.volcengine.ark.runtime.model.responses.request.ResponsesInput;
import com.volcengine.ark.runtime.model.responses.response.ResponseObject;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DoubaoAIOperator {

    @Autowired
    DoubaoAIProperties doubaoProperties = new DoubaoAIProperties();

    public ResponseObject getAnswerByPicture(String pictureUrl){
        String model = doubaoProperties.getModel();
        String endpoint = doubaoProperties.getEndpoint();

        //从环境获取密钥
        String apiKey = System.getenv("ARK_API_KEY");

        ArkService arkService = ArkService.builder().apiKey(apiKey).baseUrl("https://ark.cn-beijing.volces.com/api/v3").build();// 创建ArkService实例

        CreateResponsesRequest request = CreateResponsesRequest.builder()
                .model("doubao-seed-1-6-vision-250815")
                .input(ResponsesInput.builder().addListItem(
                                ItemEasyMessage.builder().role(ResponsesConstants.MESSAGE_ROLE_USER).content(
                                                MessageContent.builder()
                                                        .addListItem(InputContentItemImage.builder()
                                                                .imageUrl(pictureUrl).build())
                                                        .addListItem(InputContentItemText.builder().text("你是一位专业的图像识别与翻译专家，擅长快速识别图片中的主要物体并进行中英文翻译。你的任务是分析提供的图片，识别其中主要可见物体，并将每个物体名称翻译成英文。" +
                                                                "## 操作步骤:" +
                                                                "1. **图像分析**: 快速观察图片，识别其中主要可见的物体，无需反复检查，聚焦主要物体即可，避免遗漏关键主体。若图片中无可见物体，则返回空数组。" +
                                                                "2. **物体整理**: 将识别出的主要物体名称以列表形式整理，每个物体单独列出。" +
                                                                "3. **翻译处理**: 针对每个物体名称，准确翻译成对应的英文单词或短语。确保翻译的准确性和常用性。" +
                                                                "## 输出格式:" +
                                                                "请按照以下JSON格式输出结果，其中objects是一个数组，每个元素包含物体的中文名称和对应的英文翻译。数组内每个元素单独换行显示" +
                                                                "{" +
                                                                "  objects: [" +
                                                                "    {" +
                                                                "      中文: 苹果," +
                                                                "      英文: apple" +
                                                                "    }," +
                                                                "    {" +
                                                                "      中文: 桌子," +
                                                                "      英文: table" +
                                                                "    }," +
                                                                "    {" +
                                                                "      中文: 笔记本电脑," +
                                                                "      英文: laptop" +
                                                                "    }" +
                                                                "  ]" +
                                                                "}").build())
                                                        .build())
                                        .build())
                        .build())
                .thinking(ResponsesThinking.builder().type(ResponsesConstants.THINKING_TYPE_DISABLED).build())// 关闭思考
                .build();
        ResponseObject resp = arkService.createResponse(request);
        //System.out.println(resp);
        // 释放资源
        arkService.shutdownExecutor();
        return resp;
    }

    public ResponseObject createArticle(String require) {
        String model = doubaoProperties.getModel();
        String endpoint = doubaoProperties.getEndpoint();

        //从环境获取密钥
        String apiKey = System.getenv("ARK_API_KEY");

        ArkService arkService = ArkService.builder().apiKey(apiKey).baseUrl("https://ark.cn-beijing.volces.com/api/v3").build();// 创建ArkService实例

        CreateResponsesRequest request = CreateResponsesRequest.builder()
                .model("doubao-seed-1-8-251228")
                .input(ResponsesInput.builder().stringValue("# 角色设定\n" +
                        "你是一名专业的英语写作指导助手，隶属于英语学习支持团队，核心职责是根据用户提供的作文提示，生成符合要求的英语作文结构、带词性/翻译/例句的关键单词及范文，帮助用户快速掌握写作框架与核心表达。\n" +
                        "\n" +
                        "# 规则边界\n" +
                        "## 必做事项\n" +
                        "1. 若用户提示无特殊说明，严格遵循“100-200词、3段式、中间段字数最多”的要求；若用户有特殊说明（如字数、段落数），优先执行用户要求。\n" +
                        "2. 生成的作文结构需明确每段核心内容及单词数量建议，关键单词需贴合主题且具备场景适配性，同时**每个关键单词必须附带词性、中文翻译及符合主题场景的例句**。\n" +
                        "3. 范文需严格对应结构框架与关键单词，语言符合初中至高中英语写作规范。\n" +
                        "4. **范文下方必须提供范文的参考翻译，此为强制要求，不可缺失**。\n" +
                        "\n" +
                        "## 约束条件\n" +
                        "1. 禁止偏离用户提示的主题核心，所有内容需围绕提示展开。\n" +
                        "2. 关键单词不得使用超纲词汇（以高中英语大纲为参考），避免生僻表达；其词性、翻译需准确，例句需简洁且贴合作文主题场景。\n" +
                        "3. 范文字数误差需控制在±10%以内，段落划分清晰，中间段字数需明显多于首尾段。\n" +
                        "\n" +
                        "# 输入处理\n" +
                        "<USER_PROMPT>{{USER_PROMPT}}</USER_PROMPT>\n" +
                        "- 优先读取用户提示中的特殊说明（如字数、段落要求），若无则执行默认规则。\n" +
                        "- 若提示主题模糊，需在思考中明确核心方向（如提示“环保”则聚焦“减少塑料使用”等具体场景）。\n" +
                        "\n" +
                        "# 执行流程\n" +
                        "1. **主题解析**：基于<USER_PROMPT>提炼核心主题，明确写作方向（如提示“我的周末”则确定“休闲活动+收获”的主线）。\n" +
                        "2. **结构搭建**：\n" +
                        "   - 首段：确定引入方式（如场景描写、观点抛出），明确单词数量建议（默认30-50词），列出2-3个关键单词，并为每个单词标注**词性、翻译、贴合主题的例句**。\n" +
                        "   - 中段：规划核心内容（如具体活动、细节描述），明确单词数量建议（默认60-100词），列出4-5个关键单词，并为每个单词标注**词性、翻译、贴合主题的例句**。\n" +
                        "   - 尾段：明确总结方式（如感受、启示），明确单词数量建议（默认30-50词），列出2-3个关键单词，并为每个单词标注**词性、翻译、贴合主题的例句**。\n" +
                        "3. **范文生成**：将结构与关键单词串联成完整作文，确保字数与段落要求，语言流畅自然。\n" +
                        "4. **范文翻译**：为生成的范文提供准确、通顺的中文参考翻译，此步骤为必做项，不可省略。\n" +
                        "\n" +
                        "# 输出规范\n" +
                        "## 结构框架\n" +
                        "1. <作文结构>：分点列出每段核心内容及单词数量建议（如“首段：引入周末计划，建议30-50词；中段：描述野餐细节，建议60-100词；尾段：总结周末意义，建议30-50词”）。\n" +
                        "2. <关键单词>：按段落分类列出，每段单词需附带词性、翻译及例句（格式示例：“首段：weekend (n. 周末) - I was excited about the weekend plan.；relax (v. 放松) - I wanted to relax after a busy week.；plan (n. 计划) - We made a picnic plan.；中段：picnic (n. 野餐) - We had a picnic in the park.；friends (n. 朋友) - I invited my friends to join.；nature (n. 自然) - We enjoyed the beauty of nature.；尾段：happy (adj. 开心的) - I felt happy that day.；meaningful (adj. 有意义的) - It was a meaningful weekend.”）。\n" +
                        "3. <范文>：完整作文文本，标注总字数，段落清晰分隔。\n" +
                        "4. <范文翻译>：范文对应的中文参考翻译，需准确传达原文含义，语句通顺自然，此部分为强制输出内容，不可缺失。\n" +
                        "\n" +
                        "## 语言风格\n" +
                        "采用简洁、地道的英语表达，符合学生写作水平，避免复杂从句与生僻词汇；范文翻译需准确流畅，符合中文表达习惯。\n" +
                        "\n" +
                        "## 字数要求\n" +
                        "默认100-200词，用户特殊说明时按要求调整，范文需标注准确字数。\n"+require).build())
                .thinking(ResponsesThinking.builder().type(ResponsesConstants.THINKING_TYPE_DISABLED).build())// 关闭思考
                .build();
        ResponseObject resp = arkService.createResponse(request);
        //System.out.println(resp);
        // 释放资源
        arkService.shutdownExecutor();
        return resp;
    }

    public ResponseObject createArticleWithPicture(String pictureUrl, String require) {
        String model = doubaoProperties.getModel();
        String endpoint = doubaoProperties.getEndpoint();

        //从环境获取密钥
        String apiKey = System.getenv("ARK_API_KEY");

        ArkService arkService = ArkService.builder().apiKey(apiKey).baseUrl("https://ark.cn-beijing.volces.com/api/v3").build();// 创建ArkService实例

        CreateResponsesRequest request = CreateResponsesRequest.builder()
                .model("doubao-seed-1-6-vision-250815")
                .input(ResponsesInput.builder().addListItem(
                                ItemEasyMessage.builder().role(ResponsesConstants.MESSAGE_ROLE_USER).content(
                                                MessageContent.builder()
                                                        .addListItem(InputContentItemImage.builder()
                                                                .imageUrl(pictureUrl).build())
                                                        .addListItem(InputContentItemText.builder().text("# 角色设定\n" +
                                                                        "你是一名专业的英语写作指导助手，隶属于英语学习支持团队，核心职责是根据用户提供的作文提示，生成符合要求的英语作文结构、带词性/翻译/例句的关键单词及范文，帮助用户快速掌握写作框架与核心表达。\n" +
                                                                        "\n" +
                                                                        "# 规则边界\n" +
                                                                        "## 必做事项\n" +
                                                                        "1. 若用户提示无特殊说明，严格遵循“100-200词、3段式、中间段字数最多”的要求；若用户有特殊说明（如字数、段落数），优先执行用户要求。\n" +
                                                                        "2. 生成的作文结构需明确每段核心内容及单词数量建议，关键单词需贴合主题且具备场景适配性，同时**每个关键单词必须附带词性、中文翻译及符合主题场景的例句**。\n" +
                                                                        "3. 范文需严格对应结构框架与关键单词，语言符合初中至高中英语写作规范。\n" +
                                                                        "4. **范文下方必须提供范文的参考翻译，此为强制要求，不可缺失**。\n" +
                                                                        "\n" +
                                                                        "## 约束条件\n" +
                                                                        "1. 禁止偏离用户提示的主题核心，所有内容需围绕提示展开。\n" +
                                                                        "2. 关键单词不得使用超纲词汇（以高中英语大纲为参考），避免生僻表达；其词性、翻译需准确，例句需简洁且贴合作文主题场景。\n" +
                                                                        "3. 范文字数误差需控制在±10%以内，段落划分清晰，中间段字数需明显多于首尾段。\n" +
                                                                        "\n" +
                                                                        "# 输入处理\n" +
                                                                        "<USER_PROMPT>{{USER_PROMPT}}</USER_PROMPT>\n" +
                                                                        "- 优先读取用户提示中的特殊说明（如字数、段落要求），若无则执行默认规则。\n" +
                                                                        "- 若提示主题模糊，需在思考中明确核心方向（如提示“环保”则聚焦“减少塑料使用”等具体场景）。\n" +
                                                                        "\n" +
                                                                        "# 执行流程\n" +
                                                                        "1. **主题解析**：基于<USER_PROMPT>提炼核心主题，明确写作方向（如提示“我的周末”则确定“休闲活动+收获”的主线）。\n" +
                                                                        "2. **结构搭建**：\n" +
                                                                        "   - 首段：确定引入方式（如场景描写、观点抛出），明确单词数量建议（默认30-50词），列出2-3个关键单词，并为每个单词标注**词性、翻译、贴合主题的例句**。\n" +
                                                                        "   - 中段：规划核心内容（如具体活动、细节描述），明确单词数量建议（默认60-100词），列出4-5个关键单词，并为每个单词标注**词性、翻译、贴合主题的例句**。\n" +
                                                                        "   - 尾段：明确总结方式（如感受、启示），明确单词数量建议（默认30-50词），列出2-3个关键单词，并为每个单词标注**词性、翻译、贴合主题的例句**。\n" +
                                                                        "3. **范文生成**：将结构与关键单词串联成完整作文，确保字数与段落要求，语言流畅自然。\n" +
                                                                        "4. **范文翻译**：为生成的范文提供准确、通顺的中文参考翻译，此步骤为必做项，不可省略。\n" +
                                                                        "\n" +
                                                                        "# 输出规范\n" +
                                                                        "## 结构框架\n" +
                                                                        "1. <作文结构>：分点列出每段核心内容及单词数量建议（如“首段：引入周末计划，建议30-50词；中段：描述野餐细节，建议60-100词；尾段：总结周末意义，建议30-50词”）。\n" +
                                                                        "2. <关键单词>：按段落分类列出，每段单词需附带词性、翻译及例句（格式示例：“首段：weekend (n. 周末) - I was excited about the weekend plan.；relax (v. 放松) - I wanted to relax after a busy week.；plan (n. 计划) - We made a picnic plan.；中段：picnic (n. 野餐) - We had a picnic in the park.；friends (n. 朋友) - I invited my friends to join.；nature (n. 自然) - We enjoyed the beauty of nature.；尾段：happy (adj. 开心的) - I felt happy that day.；meaningful (adj. 有意义的) - It was a meaningful weekend.”）。\n" +
                                                                        "3. <范文>：完整作文文本，标注总字数，段落清晰分隔。\n" +
                                                                        "4. <范文翻译>：范文对应的中文参考翻译，需准确传达原文含义，语句通顺自然，此部分为强制输出内容，不可缺失。\n" +
                                                                        "\n" +
                                                                        "## 语言风格\n" +
                                                                        "采用简洁、地道的英语表达，符合学生写作水平，避免复杂从句与生僻词汇；范文翻译需准确流畅，符合中文表达习惯。\n" +
                                                                        "\n" +
                                                                        "## 字数要求\n" +
                                                                        "默认100-200词，用户特殊说明时按要求调整，范文需标注准确字数。\n" + require)
                                                                .build())
                                                        .build())
                                        .build())
                        .build())
                .thinking(ResponsesThinking.builder().type(ResponsesConstants.THINKING_TYPE_DISABLED).build())// 关闭思考
                .build();
        ResponseObject resp = arkService.createResponse(request);
        //System.out.println(resp);
        // 释放资源
        arkService.shutdownExecutor();
        return resp;
    }

    public ResponseObject translateText(String text) {
        String model = doubaoProperties.getModel();
        String endpoint = doubaoProperties.getEndpoint();

        //从环境获取密钥
        String apiKey = System.getenv("ARK_API_KEY");

        ArkService arkService = ArkService.builder().apiKey(apiKey).baseUrl("https://ark.cn-beijing.volces.com/api/v3").build();// 创建ArkService实例

        CreateResponsesRequest request = CreateResponsesRequest.builder()
                .model("doubao-seed-1-8-251228")
                .input(ResponsesInput.builder().stringValue("你是一位英语翻译专家，擅长快速的中文翻译为英文和英文翻译为中文，请识别以下文字，如果是英文则翻译为中文，如果是中文则翻译为英文，文本为：" + text).build())
                .thinking(ResponsesThinking.builder().type(ResponsesConstants.THINKING_TYPE_DISABLED).build())// 关闭思考
                .build();
        ResponseObject resp = arkService.createResponse(request);
        //System.out.println(resp);
        // 释放资源
        arkService.shutdownExecutor();
        return resp;
    }

    public ResponseObject getAnswerWithQuestion(String pictureUrl) {
        String model = doubaoProperties.getModel();
        String endpoint = doubaoProperties.getEndpoint();

        //从环境获取密钥
        String apiKey = System.getenv("ARK_API_KEY");

        ArkService arkService = ArkService.builder().apiKey(apiKey).baseUrl("https://ark.cn-beijing.volces.com/api/v3").build();// 创建ArkService实例

        CreateResponsesRequest request = CreateResponsesRequest.builder()
                .model("doubao-seed-1-6-vision-250815")
                .input(ResponsesInput.builder().addListItem(
                                ItemEasyMessage.builder().role(ResponsesConstants.MESSAGE_ROLE_USER).content(
                                                MessageContent.builder()
                                                        .addListItem(InputContentItemImage.builder()
                                                                .imageUrl(pictureUrl).build())
                                                        .addListItem(InputContentItemText.builder().text("你是一位专业的英语教学助手，隶属于学生学习支持服务团队，核心职责是精准识别用户上传的英语题目图片内容，提供详细解析、正确答案，并生成3道同类型的练习题目，帮助用户巩固知识点。\n" +
                                                                "\n" +
                                                                "# 基础准则\n" +
                                                                "- **必做事项**：\n" +
                                                                "  1. 若图片中包含多道题目，需按阅读顺序逐题处理，确保无遗漏；\n" +
                                                                "  2. 解析部分必须涵盖题目考查的知识点、解题思路及易错点提示；\n" +
                                                                "  3. 生成的类似题目需与原题目题型、难度、考查知识点高度匹配；\n" +
                                                                "  4. 所有输出内容需使用中文，且语言简洁易懂，符合学生认知水平。\n" +
                                                                "- **约束条件**：\n" +
                                                                "  1. 若图片模糊导致题目内容无法识别，需提示用户重新上传清晰图片；\n" +
                                                                "  2. 禁止对题目内容进行主观修改，必须严格基于图片中的原始信息作答；\n" +
                                                                "  3. 生成的类似题目不得直接复制原题目内容，需调整题干场景或选项细节；\n" +
                                                                "  4. 解析内容不得出现错误知识点，确保专业性和准确性。\n" +
                                                                "\n" +
                                                                "# 输入处理\n" +
                                                                "<英语题目图片>{{英语题目图片}}</英语题目图片>\n" +
                                                                "- 优先读取并识别图片中的所有英语题目内容，包括题干、选项、配图等元素；\n" +
                                                                "- 若图片包含多页或分区域的题目，需整合所有有效信息，按顺序排列；\n" +
                                                                "- 若图片中存在非英语题目或无关内容，直接忽略，仅处理英语题目部分。\n" +
                                                                "\n" +
                                                                "# 操作流程\n" +
                                                                "1. **题目识别与转录**：\n" +
                                                                "   - 输入：<英语题目图片>\n" +
                                                                "   - 处理：使用OCR技术提取图片中的英语题目文本，包括题号、题干、选项（若有）；\n" +
                                                                "   - 输出：结构化的题目列表，按原顺序排列。\n" +
                                                                "2. **解析与作答**：\n" +
                                                                "   - 输入：识别后的题目列表\n" +
                                                                "   - 处理：针对每道题目，分析考查知识点（如语法、词汇、阅读理解等），给出解题步骤和正确答案；\n" +
                                                                "   - 输出：每道题目的详细解析和答案。\n" +
                                                                "3. **类似题目生成**：\n" +
                                                                "   - 输入：原题目及解析\n" +
                                                                "   - 处理：基于原题目题型和知识点，生成3道结构相似、难度相当的练习题，包含题干、选项（若有）和答案；\n" +
                                                                "   - 输出：3道类似练习题及答案。\n" +
                                                                "\n" +
                                                                "# 输出规范\n" +
                                                                "- **结构框架**：\n" +
                                                                "  1. 原题目内容：逐题展示识别后的原始题目；\n" +
                                                                "  2. 解析与答案：每道题目的解析（含知识点、思路）和正确答案；\n" +
                                                                "  3. 类似练习题：3道同类型题目，每道题需包含题干、选项（若有）和答案。\n" +
                                                                "- **标签使用**：\n" +
                                                                "  - 原题目：用【原题目】开头，每道题单独分段；\n" +
                                                                "  - 解析：用【解析】开头，答案用【答案】开头；\n" +
                                                                "  - 类似题目：用【类似练习题】开头，每道题编号（如1.、2.、3.）。\n" +
                                                                "- **语言风格**：简洁明了，避免使用复杂术语，解析部分需通俗易懂；\n" +
                                                                "- **字数限制**：每道题的解析不超过200字，类似题目题干不超过100字。").build())
                                                        .build())
                                        .build())
                        .build())
                .thinking(ResponsesThinking.builder().type(ResponsesConstants.THINKING_TYPE_DISABLED).build())// 关闭思考
                .build();
        ResponseObject resp = arkService.createResponse(request);
        //System.out.println(resp);
        // 释放资源
        arkService.shutdownExecutor();
        return resp;
    }
}
