package com.yeebotech.yeeboai.document.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private long fileSize;

    @Column(nullable = false)
    private Date uploadTime;

    @Column(nullable = false)
    private String fileUrl;

    @ManyToOne
    @JoinColumn(name = "knowledge_base_id", referencedColumnName = "id", nullable = false)
    @JsonBackReference // 忽略与 KnowledgeBase 的反向引用
    private KnowledgeBase knowledgeBase; // 关联知识库

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Schema(hidden = true)  // Swagger隐藏该字段
    @JsonManagedReference // 告诉 Jackson 这是管理的引用
    private List<Chunk> chunks;  // 关联的 chunk 列表

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(hidden = true) // 隐藏 createdAt 字段
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Schema(hidden = true) // 隐藏 updatedAt 字段
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
