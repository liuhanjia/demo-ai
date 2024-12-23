package com.yeebotech.yeeboai.milvus.service.impl;

import com.google.gson.JsonObject;
import com.yeebotech.yeeboai.milvus.service.MilvusService;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.SearchResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MilvusServiceImpl implements MilvusService {

    private final MilvusClientV2 milvusClient;

    @Autowired
    public MilvusServiceImpl(MilvusClientV2 milvusClient) {
        this.milvusClient = milvusClient;
    }


    @Override
    public List<List<SearchResp.SearchResult>> searchVectors(float[] embedding, List<Long> knowledgeBaseIds) {
        // 构造过滤条件，将多个 `knowledge_base_id` 转为 OR 条件
        String filter = knowledgeBaseIds.stream()
                .map(id -> "knowledge_base_id == " + id)
                .collect(Collectors.joining(" || ")); // 使用 "||" 连接多个条件

        FloatVec queryVector = new FloatVec(embedding);

        SearchReq searchReq = SearchReq.builder()
                .collectionName("kb_yeebotech")
                .data(Collections.singletonList(queryVector))
                .outputFields(Arrays.asList("chunk_id", "knowledge_base_id", "content")) // 指定返回字段
                .filter(filter) // 添加过滤条件
                .topK(3) // 指定返回 Top K 的搜索结果
                .build();

        SearchResp searchResp = milvusClient.search(searchReq);

        return searchResp.getSearchResults();
    }


    @Override
    public boolean insertVectors(String collectionName, List<JsonObject> jsonVectors) {
        InsertReq insertReq = InsertReq.builder()
                .collectionName(collectionName)
                .data(jsonVectors)
                .build();
        try {
            milvusClient.insert(insertReq);
        } catch (Exception e) {
            System.err.println("Error inserting vectors: " + e.getMessage());
            // 可以根据实际情况进行更细粒度的异常处理，比如根据不同的异常类型做不同的响应
            throw new RuntimeException("向Milvus插入向量数据失败，集合名称：" + collectionName, e);
        }
        return true; // 如果没有抛出异常，说明插入成功，返回true
    }
}
