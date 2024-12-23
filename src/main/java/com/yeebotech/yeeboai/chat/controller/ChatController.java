package com.yeebotech.yeeboai.chat.controller;

import com.yeebotech.yeeboai.auth.utils.JwtUtil;
import com.yeebotech.yeeboai.chat.dto.ConversationDTO;
import com.yeebotech.yeeboai.chat.entity.Conversation;
import com.yeebotech.yeeboai.chat.service.ChatService;
import com.yeebotech.yeeboai.chat.dto.ChatRequest;
import com.yeebotech.yeeboai.chat.dto.ChatResponse;
import com.yeebotech.yeeboai.common.dto.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/completions")
    public ResponseEntity<ApiResult<ChatResponse>> handleChat(@RequestBody ChatRequest chatRequest,HttpServletRequest request) {
        try {
            // 打印请求头部中的 Authorization
            String authorizationHeader = request.getHeader("Authorization");
            System.out.println("Authorization: " + authorizationHeader);
            Long userId = JwtUtil.getUserIdFromToken(authorizationHeader);
            if (userId == 0) {
                return ResponseEntity.status(401).body(ApiResult.error(401, "获取用户失败"));
            }
            // 处理聊天请求
            ChatResponse chatResponse = chatService.processChat(userId, chatRequest);

            // 成功时返回标准的 JSON 格式
            return ResponseEntity.ok(ApiResult.success(chatResponse));
        } catch (Exception e) {
            // 错误时返回标准的 JSON 格式
            return ResponseEntity.status(500).body(ApiResult.error(500, e.getMessage()));
        }
    }

    @Operation(summary = "获取所有会话列表", description = "返回所有会话的概要信息。")
    @GetMapping
    public ApiResult<List<Conversation>> getAllConversations(HttpServletRequest request) {
        // 打印请求头部中的 Authorization
        String authorizationHeader = request.getHeader("Authorization");
        System.out.println("Authorization: " + authorizationHeader);
        Long userId = JwtUtil.getUserIdFromToken(authorizationHeader);
        if (userId == 0) {
            return ApiResult.error(401, "获取用户失败");
        }
        List<Conversation> conversations = chatService.getConversationByUserId(userId);
        return ApiResult.success(conversations);
    }

    @Operation(summary = "根据ID获取会话详情", description = "根据会话ID返回会话的详细信息。")
    @GetMapping("/{id}")
    public ApiResult<ConversationDTO> getConversationById(@PathVariable Long id) {
        ConversationDTO conversation = chatService.getConversationById(id);
        return conversation != null ? ApiResult.success(conversation) : ApiResult.error(500,"会话未找到");
    }

    @Operation(summary = "删除指定的会话", description = "根据会话ID删除指定的会话。")
    @DeleteMapping("/{id}")
    public ApiResult<Void> deleteConversationById(HttpServletRequest request, @PathVariable Long id) {
        String authorizationHeader = request.getHeader("Authorization");
        Long userId = JwtUtil.getUserIdFromToken(authorizationHeader); // 从 token 中提取 userId
        if (userId == null) {
            return ApiResult.error(401, "未登录");
        }
        // 删除会话
        chatService.deleteConversationById(id, userId);
        ApiResult<Void> response = ApiResult.success();
        return response;
    }
}
