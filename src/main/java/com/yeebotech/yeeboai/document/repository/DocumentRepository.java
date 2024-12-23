package com.yeebotech.yeeboai.document.repository;

import com.yeebotech.yeeboai.chat.entity.Chat;
import com.yeebotech.yeeboai.document.entity.Document;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    // 可以在这里根据需求添加自定义查询方法
    // 比如根据文件名称查询文档
    Document findByFileName(String fileName);

    void deleteByKnowledgeBaseId(Long conversationId);

    List<Document> findByKnowledgeBaseId(Long knowledgeBaseId);
}
