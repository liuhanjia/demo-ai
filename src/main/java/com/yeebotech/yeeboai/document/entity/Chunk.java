package com.yeebotech.yeeboai.document.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@Entity
@ToString(exclude = "document")
public class Chunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_id")
    @JsonBackReference // 忽略与 document 的反向引用
    private Document document;  // 每个 Chunk 都关联一个 Document

    @Lob
    private String content;  // 存储文本内容
    private String vectorDbId;  // 向量数据库的 ID

    private Date createTime;  // 创建时间

    // Lombok 会自动生成 Getters、Setters、toString、equals 和 hashCode 方法
}
