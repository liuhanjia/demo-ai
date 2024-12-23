package com.yeebotech.yeeboai.document.service.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;
import com.yeebotech.yeeboai.document.entity.Chunk;
import com.yeebotech.yeeboai.document.entity.Document;
import com.yeebotech.yeeboai.document.entity.KnowledgeBase;
import com.yeebotech.yeeboai.document.repository.ChunkRepository;
import com.yeebotech.yeeboai.document.repository.DocumentRepository;
import com.yeebotech.yeeboai.document.service.DocumentService;
import com.yeebotech.yeeboai.common.oss.OssService;
import com.yeebotech.yeeboai.milvus.service.MilvusService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private OssService ossService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ChunkRepository chunkRepository;

    @Autowired
    private MilvusService milvusService;

    @Override
    public Document uploadDocument(MultipartFile file, KnowledgeBase knowledgeBase) throws Exception {
        // 文件上传到 OSS
        ossService.uploadFile(file.getOriginalFilename(), file.getInputStream());

        // 创建 Document 实体
        Document document = new Document();
        document.setFileName(file.getOriginalFilename());
        document.setFileSize(file.getSize());
        document.setUploadTime(new Date());
        document.setFileUrl("https://shunwei-oms.oss-cn-shenzhen.aliyuncs.com/uploads/laravel%E6%96%87%E6%A1%A3.pdf");
        document.setKnowledgeBase(knowledgeBase);

        // 保存文件信息到数据库
        document = documentRepository.save(document);

        // 解析 PDF 文件
        List<Chunk> chunks = parsePdf(file.getInputStream(), document);

        // 将 chunk 数据存入 Milvus
        storeChunksInMilvus(knowledgeBase.getId(), chunks);

        // 保存分段内容（chunks）到数据库
        chunkRepository.saveAll(chunks);

        // 将 chunks 设置到 Document 中
        document.setChunks(chunks);
        documentRepository.save(document);

        return document;
    }

    @Override
    public String downloadDocument(String filename) throws Exception {
        return ossService.downloadFile(filename);
    }

    @Override
    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with id: " + id));
    }

    @Override
    public Chunk getChunkById(Long id) {
        return chunkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chunk not found with id: " + id));
    }

    // 解析 PDF 文件内容并分割为 chunks
    private List<Chunk> parsePdf(InputStream inputStream, Document document) throws IOException {
        try (PDDocument pdDocument = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);  // 从第一页开始解析
            stripper.setEndPage(pdDocument.getNumberOfPages());  // 直到最后一页
            String content = stripper.getText(pdDocument);  // 获取所有页面的文本内容

            // 根据换行符或其他规则进行分段
            List<String> paragraphs = splitIntoChunks(content);

            // 将每个段落保存为 Chunk
            return paragraphs.stream()
                    .map(paragraph -> createChunk(paragraph, document))
                    .collect(Collectors.toList());
        }
    }

    // 按固定长度切分
    private List<String> splitIntoChunks(String content) {
        List<String> chunks = new ArrayList<>();
        int length = content.length();
        for (int i = 0; i < length; i += 512) {
            chunks.add(content.substring(i, Math.min(length, i + 512)));
        }
        return chunks;
    }

    // 创建 Chunk 实体
    private Chunk createChunk(String content, Document document) {
        Chunk chunk = new Chunk();
        chunk.setDocument(document);  // 将每个 chunk 与 document 关联
        chunk.setContent(content);
        chunk.setVectorDbId("");  // 向量数据库 ID，暂时为空，可以后续补充
        chunk.setCreateTime(new Date());
        return chunk;
    }

    // 将 chunk 存储到 Milvus
    private void storeChunksInMilvus(Long kbId, List<Chunk> chunks) {
        try {
            // 先保存 Chunk 实体，生成 id
            List<Chunk> savedChunks = chunkRepository.saveAll(chunks);

            // 生成 chunk 的向量
            List<float[]> embeddings = generateEmbeddings(savedChunks);

            // 构造向量数据并包含文本
            List<JsonObject> jsonVectors = savedChunks.stream()
                    .map(chunk -> {
                        JsonObject jsonObject = new JsonObject();
                        JsonArray jsonArray = new JsonArray();
                        // 将每个 chunk 的向量值添加到 jsonArray 中
                        for (float value : embeddings.get(savedChunks.indexOf(chunk))) {
                            jsonArray.add(value);  // 向量值
                        }
                        jsonObject.add("embedding", jsonArray);  // 将 JsonArray 作为 "vector" 添加到 JsonObject 中
                        jsonObject.addProperty("chunk_id", chunk.getId());  // 将 chunk 的数据库 ID 添加到 jsonObject
                        jsonObject.addProperty("knowledge_base_id", kbId);
                        // 添加文本内容字段
                        jsonObject.addProperty("content", chunk.getContent());  // 添加文本内容

                        return jsonObject;
                    })
                    .collect(Collectors.toList());
            milvusService.insertVectors("kb_yeebotech",jsonVectors);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 使用 OpenAI API 生成 Embedding 向量
    private List<float[]> generateEmbeddings(List<Chunk> chunks) {
        // 调用 OpenAI Embedding API 并返回向量数据
        // 这里需要处理调用 OpenAI API，并将文本转换为向量
        // 假设你已经有了一个函数来调用 OpenAI Embedding API

        // 这里返回的向量应该是一个浮动数组列表
        return chunks.stream()
                .map(chunk -> embeddingForText(chunk.getContent()))  // 每个 chunk 内容生成向量
                .collect(Collectors.toList());
    }

    // 这里模拟调用 OpenAI API，返回一个浮动数组
    @Override
    public float[] embeddingForText(String text) {
        // 模拟调用 OpenAI API 返回的 1536 维向量
        String apiKey = "这是key";

        // 手动转义特殊字符
        String escapedInputText = text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");

        // 创建请求体
        String requestBody = String.format("{\"input\": \"%s\", \"model\": \"text-embedding-3-small\"}", escapedInputText);

        // 创建HttpClient实例
        HttpClient client = HttpClient.newHttpClient();

        // 创建HttpRequest实例
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://openai.cyanrocks.com/v1/embeddings"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            // 发送同步请求，并获取响应
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();
            List<Double> embeddingList = JsonPath.read(responseBody, "$.data[0].embedding");

            // 将 List<Double> 转换为 float[]
            float[] embedding = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                embedding[i] = embeddingList.get(i).floatValue();
            }

            return embedding;
        } catch (Exception e) {
            e.printStackTrace();
            return new float[0];
        }
    }
}
