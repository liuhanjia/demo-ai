package com.yeebotech.yeeboai.document.service;

import com.yeebotech.yeeboai.document.entity.Chunk;
import com.yeebotech.yeeboai.document.entity.Document;
import com.yeebotech.yeeboai.document.entity.KnowledgeBase;
import io.milvus.v2.service.vector.response.SearchResp;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DocumentService {
    // 上传文档
    Document uploadDocument(MultipartFile file, KnowledgeBase knowledgeBase) throws Exception;

    String downloadDocument(String filename) throws Exception;

    // 根据 ID 获取文档
    Document getDocumentById(Long id);

    // Embedding
    float[] embeddingForText(String text);

    Chunk getChunkById(Long id);
}
