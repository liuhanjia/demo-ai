package com.yeebotech.yeeboai.document.repository;

import com.yeebotech.yeeboai.document.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {
    // 根据 userId 查询 KnowledgeBase
    List<KnowledgeBase> findByUserId(Long userId);
}
