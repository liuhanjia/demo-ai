package com.yeebotech.yeeboai.document.controller;

import com.yeebotech.yeeboai.document.dto.DocumentDTO;
import com.yeebotech.yeeboai.document.entity.Document;
import com.yeebotech.yeeboai.document.entity.KnowledgeBase;
import com.yeebotech.yeeboai.document.service.DocumentService;
import com.yeebotech.yeeboai.common.dto.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Operation(summary = "上传并解析PDF文档", description = "上传PDF并解析成文本")
    @PostMapping("/upload")
    public ResponseEntity<ApiResult<DocumentDTO>> uploadAndParsePdf(@RequestParam("file") MultipartFile file,
                                                                    @RequestParam("knowledge_base_id") Long knowledgeBaseId) {
        try {
            KnowledgeBase knowledgeBase = new KnowledgeBase();
            knowledgeBase.setId(knowledgeBaseId); // 假设通过 ID 获取知识库对象

            // 上传文件并解析
            Document document = documentService.uploadDocument(file, knowledgeBase);

            // 成功时返回标准的 JSON 格式
            return ResponseEntity.ok(ApiResult.success(new DocumentDTO(document)));
        } catch (Exception e) {
            System.out.println("失败: " + e.getMessage());
            // 错误时返回标准的 JSON 格式
            return ResponseEntity.status(500).body(ApiResult.error(500, "Error uploading or parsing file"));
        }
    }

    @Operation(summary = "下载文档", description = "根据文档 ID 获取文档下载地址")
    @PostMapping("/download")
    public ResponseEntity<ApiResult<String>> downloadDocument(@RequestParam("document_id") long documentId) {
        try {
            // 根据 ID 获取文档
            Document document = documentService.getDocumentById(documentId);
            String filename = document.getFileName();
            String downloadUrl = documentService.downloadDocument(filename);
            return ResponseEntity.ok(ApiResult.success(downloadUrl));
        } catch (Exception e) {
            System.out.println("获取文件下载地址失败：" + e.getMessage());
            return ResponseEntity.status(500).body(ApiResult.error(500, "Error downloading file"));
        }
    }

    @Operation(summary = "根据文档 ID 获取文档", description = "根据文档 ID 获取文档详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<Document>> getDocumentById(@PathVariable("id") Long id) {
        try {
            // 根据 ID 获取文档
            Document document = documentService.getDocumentById(id);

            // 成功时返回标准的 JSON 格式
            return ResponseEntity.ok(ApiResult.success(document));
        } catch (Exception e) {
            // 错误时返回标准的 JSON 格式
            return ResponseEntity.status(500).body(ApiResult.error(500, "Document not found with id: " + id));
        }
    }
}
