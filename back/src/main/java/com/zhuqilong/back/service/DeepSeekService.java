package com.zhuqilong.back.service;

import com.zhuqilong.back.config.DeepSeekConfig;
import com.zhuqilong.back.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeepSeekService {

    @Autowired
    private DeepSeekConfig deepSeekConfig;

    @Autowired
    private RestTemplate restTemplate;

    public ChatResponse getChatResponse(String userMessage) {
        ChatResponse response = new ChatResponse();

        try {
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepSeekConfig.getApiKey());

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", deepSeekConfig.getModel());
            requestBody.put("stream", false);

            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一位专业的智能健康助手，专为老年人提供健康咨询、生活建议和聊天服务。请使用简洁、友好、温暖的语言回答老年人的问题，语速适中，避免使用专业术语。");
            messages.add(systemMessage);

            Map<String, String> userMessageMap = new HashMap<>();
            userMessageMap.put("role", "user");
            userMessageMap.put("content", userMessage);
            messages.add(userMessageMap);

            requestBody.put("messages", messages);

            // 构建完整请求
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 发送请求
            ResponseEntity<Map> apiResponse = restTemplate.exchange(
                    deepSeekConfig.getBaseUrl() + "/chat/completions",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            // 处理响应
            if (apiResponse.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = apiResponse.getBody();
                if (responseBody != null) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> choice = choices.get(0);
                        Map<String, String> message = (Map<String, String>) choice.get("message");
                        if (message != null) {
                            response.setMessage(message.get("content"));
                            response.setSuccess(true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setMessage("抱歉，我暂时无法为您提供帮助。请稍后再试。");
            response.setSuccess(false);
        }

        return response;
    }
}