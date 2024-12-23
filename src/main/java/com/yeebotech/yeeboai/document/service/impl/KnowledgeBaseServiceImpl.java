package com.yeebotech.yeeboai.document.service.impl;

import com.yeebotech.yeeboai.document.entity.Chunk;
import com.yeebotech.yeeboai.chat.entity.ChatChunk;
import com.yeebotech.yeeboai.chat.repository.ChatChunkRepository;
import com.yeebotech.yeeboai.common.exception.ResourceNotFoundException;
import com.yeebotech.yeeboai.common.exception.UnauthorizedException;
import com.yeebotech.yeeboai.common.oss.OssService;
import com.yeebotech.yeeboai.document.dto.KnowledgeBaseWithDocumentsDTO;
import com.yeebotech.yeeboai.document.entity.Document;
import com.yeebotech.yeeboai.document.entity.KnowledgeBase;
import com.yeebotech.yeeboai.document.repository.ChunkRepository;
import com.yeebotech.yeeboai.document.repository.DocumentRepository;
import com.yeebotech.yeeboai.document.repository.KnowledgeBaseRepository;
import com.yeebotech.yeeboai.document.service.KnowledgeBaseService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    @Autowired
    private KnowledgeBaseRepository knowledgeBaseRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ChunkRepository chunkRepository;

    @Autowired
    private OssService ossService;

    @Autowired
    private ChatChunkRepository chatChunkRepository;

    @Override
    public KnowledgeBase createKnowledgeBase(KnowledgeBase knowledgeBase) {
        return knowledgeBaseRepository.save(knowledgeBase);
    }

    @Override
    public List<KnowledgeBase> getAllKnowledgeBases() {
        return knowledgeBaseRepository.findAll();
    }

    @Override
    public List<KnowledgeBase> getKnowledgeBasesByUserId(Long userId) {
        return knowledgeBaseRepository.findByUserId(userId);
    }

    @Override
    public KnowledgeBaseWithDocumentsDTO getKnowledgeBaseById(Long id) {
        // 从数据库中加载 KnowledgeBase 对象，包括关联的 Document
        KnowledgeBase knowledgeBase = knowledgeBaseRepository.findById(id).orElse(null);
        if (knowledgeBase != null) {
            // 将 KnowledgeBase 转换为 KnowledgeBaseWithDocumentsDTO（包括 Document）
            return new KnowledgeBaseWithDocumentsDTO(knowledgeBase);
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteKnowledgeBase(Long id, Long userId) {

        // 查询资源是否存在
        KnowledgeBase knowledgeBase = knowledgeBaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("资源未找到"));
        // 校验权限
        if (!knowledgeBase.getUserId().equals(userId)) {
            throw new UnauthorizedException("无权限删除该资源");
        }

        // 删除会话下的所有聊天记录及其相关的块
        List<Document> documents = documentRepository.findByKnowledgeBaseId(id);
        for (Document document : documents) {
            // 遍历每个 Document 中的 Chunk 并删除相关的 ChatChunk 数据
            for (Chunk chunk : document.getChunks()) {
                // 删除与 Chunk 相关的 ChatChunk 数据
                List<ChatChunk> chatChunks = chatChunkRepository.findByChunkId(chunk.getId());
                for (ChatChunk chatChunk : chatChunks) {
                    chatChunkRepository.delete(chatChunk); // 删除与 Chunk 相关的 ChatChunk 数据
                }
                // 删除 Chunk 数据
                chunkRepository.deleteById(chunk.getId());
            }
            // 删除 OSS 文件
            String fileName = document.getFileName(); // 假设 `Document` 中有一个字段存储 OSS 文件路径
            if (fileName != null && !fileName.isEmpty()) {
                try {
                    ossService.deleteFile(fileName); // 使用 OssService 的 deleteFile 方法
                    System.out.println("Successfully deleted file from OSS: " + fileName);
                } catch (Exception e) {
                    System.err.println("Error deleting file from OSS: " + fileName + ". " + e.getMessage());
                }
            }
        }
        documentRepository.deleteByKnowledgeBaseId(id);
        knowledgeBaseRepository.deleteById(id);
    }
}
