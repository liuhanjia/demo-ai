package com.yeebotech.yeeboai.milvus.service;

import com.google.gson.JsonObject;
import io.milvus.v2.service.vector.response.SearchResp;

import java.util.List;

public interface MilvusService {
    /**
     * 搜索向量
     *
     * @param embedding 待搜索的向量
     * @return 搜索结果列表
     */
    List<List<SearchResp.SearchResult>> searchVectors(float[] embedding, List<Long> knowledgeBaseIds);

    boolean insertVectors(String collectionName, List<JsonObject> jsonVectors);
}
