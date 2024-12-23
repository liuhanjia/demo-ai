package com.yeebotech.yeeboai.chat.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChatRequest {
    private int conversationId;  // 新增 conversationId 字段
    private List<Message> messages;

    @Data
    public static class Message {
        private String role;
        private String content;
    }
}
