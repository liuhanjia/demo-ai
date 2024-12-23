package com.yeebotech.yeeboai.document.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class KnowledgeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(hidden = true)
    private Long id;

    private String title;
    private String description;

    @OneToMany(mappedBy = "knowledgeBase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Schema(hidden = true)  // Swagger隐藏该字段
    @JsonIgnore  // 忽略该字段，避免在序列化时返回
    private List<Document> documents; // 这里会返回 Document 列表

    @Column(name = "user_id", nullable = false)
    @Schema(hidden = true)  // Swagger隐藏该字段
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

