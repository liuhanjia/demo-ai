package com.yeebotech.yeeboai.chat.service;

import com.yeebotech.yeeboai.chat.dto.ChatRequest;
import com.yeebotech.yeeboai.chat.dto.ChatResponse;
import com.yeebotech.yeeboai.chat.dto.ConversationDTO;
import com.yeebotech.yeeboai.chat.entity.Conversation;

import java.util.List;

public interface ChatService {
    /**
     * 处理聊天请求
     * @param chatRequest 聊天请求对象
     * @return 聊天响应
     */
    ChatResponse processChat(Long userId, ChatRequest chatRequest);

    List<Conversation> getAllConversations();
    List<Conversation> getConversationByUserId(Long userId);

    ConversationDTO getConversationById(Long id);

    /**
     * 删除指定的 Conversation
     * @param conversationId 会话 ID
     */
    void deleteConversationById(Long conversationId, Long userId);
}
