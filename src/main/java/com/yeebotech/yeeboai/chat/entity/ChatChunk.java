package com.yeebotech.yeeboai.chat.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.yeebotech.yeeboai.document.entity.Chunk;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_chunk") // 指定表名
public class ChatChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    // 外键：指向 Chat 实体
    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false, foreignKey = @ForeignKey(name = "fk_chat_chunk_chat"))
    @JsonBackReference // 忽略与 document 的反向引用
    private Chat chat;

    // 外键：指向 Chunk 实体
    @ManyToOne
    @JoinColumn(name = "chunk_id", nullable = false, foreignKey = @ForeignKey(name = "fk_chat_chunk_chunk"))
    @JsonBackReference // 忽略与 document 的反向引用
    private Chunk chunk;
}
