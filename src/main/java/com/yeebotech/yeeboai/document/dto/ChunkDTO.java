package com.yeebotech.yeeboai.document.dto;

import com.yeebotech.yeeboai.document.entity.Chunk;
import lombok.Data;

@Data
public class ChunkDTO {
    private Long id;
    private String content;
    private Float score;
    private DocumentDTO document;

    public ChunkDTO(Chunk chunk) {
        this.id = chunk.getId();
        this.content = chunk.getContent();
        this.document = new DocumentDTO(chunk.getDocument());
    }
}
