package com.yeebotech.yeeboai.document.service;

import com.yeebotech.yeeboai.document.dto.KnowledgeBaseWithDocumentsDTO;
import com.yeebotech.yeeboai.document.entity.KnowledgeBase;

import java.util.List;

public interface KnowledgeBaseService {

    KnowledgeBase createKnowledgeBase(KnowledgeBase knowledgeBase);

    List<KnowledgeBase> getAllKnowledgeBases();

    // 根据 userId 查询
    List<KnowledgeBase> getKnowledgeBasesByUserId(Long userId);
    // 获取带有关联 Document 的 KnowledgeBase
    KnowledgeBaseWithDocumentsDTO getKnowledgeBaseById(Long id);

    void deleteKnowledgeBase(Long id, Long userId);
}
