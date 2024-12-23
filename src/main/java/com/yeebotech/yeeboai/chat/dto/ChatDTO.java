package com.yeebotech.yeeboai.chat.dto;

import com.yeebotech.yeeboai.chat.entity.Chat;
import com.yeebotech.yeeboai.document.dto.ChunkDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class ChatDTO {
    @Schema(description = "聊天ID")
    private Long id;

    @Schema(description = "问题")
    private String question;

    @Schema(description = "回答")
    private String answer;

    @Schema(description = "聊天创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "聊天更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "Chunk 列表")
    private List<ChunkDTO> chunkDTOs;

    // 新的构造函数，根据 Chat 实体和 ChatChunk 关系填充 List<ChunkDTO>
    public ChatDTO(Chat chat) {
        this.id = chat.getId();
        this.question = chat.getQuestion();
        this.answer = chat.getAnswer();
        this.createdAt = chat.getCreatedAt();
        this.updatedAt = chat.getUpdatedAt();
        this.chunkDTOs = chat.getChatChunks().stream()
                .map(chatChunk -> new ChunkDTO(chatChunk.getChunk())) // 从关联的 ChatChunk 获取 Chunk 并转为 ChunkDTO
                .collect(Collectors.toList());
    }
}
