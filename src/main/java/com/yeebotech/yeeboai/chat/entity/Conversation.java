package com.yeebotech.yeeboai.chat.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.yeebotech.yeeboai.document.entity.Chunk;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String title;


    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Schema(hidden = true)  // Swagger隐藏该字段
    @JsonManagedReference // 告诉 Jackson 这是管理的引用
    @JsonIgnore  // 忽略该字段，避免在序列化时返回
    private List<Chat> chats;  // 关联的 chat 列表

    @Column(name = "user_id", nullable = false)
    @Schema(description = "用户ID", example = "123")
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(hidden = true)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Schema(hidden = true)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
