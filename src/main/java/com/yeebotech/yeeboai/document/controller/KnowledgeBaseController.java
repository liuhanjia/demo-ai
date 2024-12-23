package com.yeebotech.yeeboai.document.controller;

import com.yeebotech.yeeboai.auth.utils.JwtUtil;
import com.yeebotech.yeeboai.document.dto.ChunkDTO;
import com.yeebotech.yeeboai.document.dto.KnowledgeBaseWithDocumentsDTO;
import com.yeebotech.yeeboai.document.entity.Chunk;
import com.yeebotech.yeeboai.document.entity.KnowledgeBase;
import com.yeebotech.yeeboai.document.service.DocumentService;
import com.yeebotech.yeeboai.document.service.KnowledgeBaseService;
import com.yeebotech.yeeboai.common.dto.ApiResult;
import com.yeebotech.yeeboai.milvus.service.MilvusService;
import io.milvus.grpc.FieldData;
import io.milvus.v2.service.vector.response.SearchResp;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/knowledge-base")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final DocumentService documentService;

    @Autowired
    private MilvusService milvusService;
    // 构造方法注入
    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService, DocumentService documentService) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.documentService = documentService;
    }

    // 创建知识库
    @Operation(summary = "创建文档", description = "此接口用于创建新的文档")
    @PostMapping
    public ResponseEntity<ApiResult<KnowledgeBase>> createKnowledgeBase(@RequestBody @Valid KnowledgeBase knowledgeBase,HttpServletRequest request) {
        // 打印请求头部中的 Authorization
        String authorizationHeader = request.getHeader("Authorization");
        System.out.println("Authorization: " + authorizationHeader);
        Long userId = JwtUtil.getUserIdFromToken(authorizationHeader);
        if (userId == 0) {
            return ResponseEntity.status(401).body(ApiResult.error(401, "获取用户失败"));
        }
        // 设置 userId 到知识库实体
        knowledgeBase.setUserId(userId);
        KnowledgeBase created = knowledgeBaseService.createKnowledgeBase(knowledgeBase);
        ApiResult<KnowledgeBase> response = ApiResult.success(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 获取知识库
    @Operation(summary = "获取所有文档", description = "此接口用于获取所有文档")
    @GetMapping
    public ResponseEntity<ApiResult<List<KnowledgeBase>>> getAllKnowledgeBases(HttpServletRequest request) {
        // 打印请求头部中的 Authorization
        String authorizationHeader = request.getHeader("Authorization");
        System.out.println("Authorization: " + authorizationHeader);

        // 使用 HeaderUtil 获取当前用户信息
        Long userId = JwtUtil.getUserIdFromToken(authorizationHeader);

        if (userId == 0) {
            return ResponseEntity.status(401).body(ApiResult.error(401, "Unauthorized"));
        }
        List<KnowledgeBase> knowledgeBases = knowledgeBaseService.getKnowledgeBasesByUserId(userId);
        ApiResult<List<KnowledgeBase>> response = ApiResult.success(knowledgeBases);
        return ResponseEntity.ok(response);
    }

    // 获取单个知识库信息（包含关联的 Document）
    @Operation(summary = "获取单个文档", description = "根据文档ID获取文档详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<KnowledgeBaseWithDocumentsDTO>> getKnowledgeBaseById(@PathVariable Long id) {
        KnowledgeBaseWithDocumentsDTO knowledgeBaseWithDocuments = knowledgeBaseService.getKnowledgeBaseById(id);
        ApiResult<KnowledgeBaseWithDocumentsDTO> response;
        if (knowledgeBaseWithDocuments != null) {
            response = ApiResult.success(knowledgeBaseWithDocuments);
            return ResponseEntity.ok(response);
        } else {
            response = ApiResult.error(404, "Not Found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // 删除知识库
    @Operation(summary = "删除文档", description = "根据文档ID删除文档")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult<Void>> deleteKnowledgeBase(HttpServletRequest request, @PathVariable Long id) {
        String authorizationHeader = request.getHeader("Authorization");
        Long userId = JwtUtil.getUserIdFromToken(authorizationHeader); // 从 token 中提取 userId
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResult.error(401, "未登录"));
        }
        knowledgeBaseService.deleteKnowledgeBase(id, userId);
        ApiResult<Void> response = ApiResult.success();
        return ResponseEntity.ok(response);
    }

    // 搜索向量数据库
    @Operation(summary = "搜索向量数据库", description = "根据用户query搜索向量数据库")
    @PostMapping("/search")
    public ResponseEntity<ApiResult<List<ChunkDTO>>> searchVectorDatabase(@RequestBody Map<String, String> request, HttpServletRequest httpServletRequest) {

        // 打印请求头部中的 Authorization
        String authorizationHeader = httpServletRequest.getHeader("Authorization");
        System.out.println("Authorization: " + authorizationHeader);
        Long userId = JwtUtil.getUserIdFromToken(authorizationHeader);
        if (userId == 0) {
            return ResponseEntity.status(401).body(ApiResult.error(401, "获取用户失败"));
        }
        // Extract the "query" from the request map
        String query = request.get("query");
        System.out.println((query));

        float[] embedding = documentService.embeddingForText(query);

        List<KnowledgeBase> knowledgeBaseList = knowledgeBaseService.getKnowledgeBasesByUserId(userId);
        // 将 List<KnowledgeBase> 转换为 List<Long>，获取每个 KnowledgeBase 的 id
        List<Long> knowledgeBaseIds = knowledgeBaseList.stream()
                .map(KnowledgeBase::getId)  // 假设 KnowledgeBase 类有 getId() 方法
                .collect(Collectors.toList());

        List<List<SearchResp.SearchResult>> searchResults = milvusService.searchVectors(embedding, knowledgeBaseIds);

        List<ChunkDTO> chunkList = new ArrayList<>();
        for (List<SearchResp.SearchResult> results : searchResults) {
            for (SearchResp.SearchResult result : results) {
                Map<String, Object> entity = result.getEntity();
                Long chunkID = (Long) entity.get("chunk_id");
                Chunk chunk = documentService.getChunkById(chunkID);
                ChunkDTO chunkDTO = new ChunkDTO(chunk);
                chunkDTO.setScore(result.getScore());
                chunkList.add(chunkDTO);
            }
        }

        return ResponseEntity.ok(ApiResult.success(chunkList));
    }
}
