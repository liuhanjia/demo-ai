package com.yeebotech.yeeboai.chat.dto;

import com.yeebotech.yeeboai.document.dto.DocumentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ConversationDTO {
    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "会话创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "会话中的聊天列表")
    private List<ChatDTO> chats;  // 添加 chats 列表
}
