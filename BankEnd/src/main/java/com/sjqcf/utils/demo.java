package com.sjqcf.utils;
import com.volcengine.ark.runtime.model.responses.item.ItemEasyMessage;
import com.volcengine.ark.runtime.service.ArkService;
import com.volcengine.ark.runtime.model.responses.request.*;
import com.volcengine.ark.runtime.model.responses.response.ResponseObject;
import com.volcengine.ark.runtime.model.responses.constant.ResponsesConstants;
import com.volcengine.ark.runtime.model.responses.item.MessageContent;

public class demo {
    public static void main(String[] args) {
        String apiKey = System.getenv("ARK_API_KEY");
        // The base URL for model invocation
        ArkService arkService = ArkService.builder().apiKey(apiKey).baseUrl("https://ark.cn-beijing.volces.com/api/v3").build();
        // Create the first-round conversation request
        CreateResponsesRequest request = CreateResponsesRequest.builder()
                .model("doubao-seed-1-6-251015")
                .input(ResponsesInput.builder().stringValue("Hi，帮我讲个笑话。").build())
                .build();
        ResponseObject resp = arkService.createResponse(request);
        System.out.println(resp);
        // Create the second-round conversation request
        CreateResponsesRequest request2 = CreateResponsesRequest.builder()
                .model("doubao-seed-1-6-251015")
                .previousResponseId(resp.getId())
                .input(ResponsesInput.builder().addListItem(
                        ItemEasyMessage.builder().role(ResponsesConstants.MESSAGE_ROLE_USER).content(
                                MessageContent.builder().stringValue("这个笑话的笑点在哪？").build()
                        ).build()
                ).build())
                .build();
        ResponseObject resp2 = arkService.createResponse(request2);
        System.out.println(resp2);

        arkService.shutdownExecutor();
    }
}