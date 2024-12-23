package com.yeebotech.yeeboai.chat.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class OpenAIClientUtil {

    private static final String OPENAI_API_URL = "https://openai.cyanrocks.com/v1/chat/completions";
    private static final String OPENAI_API_KEY = "这里是key"; // 替换为你的 OpenAI API Key

    public static String generateResponse(List<Map<String, String>> messages) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(OPENAI_API_KEY);

            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", messages
            );

            HttpEntity<String> request = new HttpEntity<>(new ObjectMapper().writeValueAsString(body), headers);
            ResponseEntity<String> response = restTemplate.exchange(OPENAI_API_URL, HttpMethod.POST, request, String.class);

            // 解析 OpenAI 的响应
            Map<String, Object> responseBody = new ObjectMapper().readValue(response.getBody(), Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");

            // 获取嵌套的 message 和 content
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");

            return content;


        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating response: " + e.getMessage();
        }
    }
}
