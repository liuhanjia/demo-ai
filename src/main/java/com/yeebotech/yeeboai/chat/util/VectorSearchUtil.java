package com.yeebotech.yeeboai.chat.util;

import com.yeebotech.yeeboai.document.service.DocumentService;
import io.milvus.v2.service.vector.response.SearchResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VectorSearchUtil {

    @Autowired
    private DocumentService documentService;

    public String search(String query) {
        // 修正类型错误，将 loat 改为 float
        float[] embedding = documentService.embeddingForText(query);

        // 调用向量搜索服务
       // List<List<SearchResp.SearchResult>> searchResults = documentService.searchVectors(embedding);

        // 假设已完成向量搜索逻辑，这里返回模拟数据
        return "Sample context from vector database for query: " + query;
    }
}
