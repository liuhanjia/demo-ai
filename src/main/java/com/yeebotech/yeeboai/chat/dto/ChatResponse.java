package com.yeebotech.yeeboai.chat.dto;

import com.yeebotech.yeeboai.chat.entity.Conversation;
import com.yeebotech.yeeboai.document.entity.Chunk;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String content;
    private List<Chunk> chunks;
    private Conversation conversation;
}
