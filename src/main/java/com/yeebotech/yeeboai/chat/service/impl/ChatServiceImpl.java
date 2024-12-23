package com.yeebotech.yeeboai.chat.service.impl;

import com.yeebotech.yeeboai.chat.dto.ChatDTO;
import com.yeebotech.yeeboai.chat.dto.ChatRequest;
import com.yeebotech.yeeboai.chat.dto.ChatResponse;
import com.yeebotech.yeeboai.chat.dto.ConversationDTO;
import com.yeebotech.yeeboai.chat.entity.Chat;
import com.yeebotech.yeeboai.chat.entity.ChatChunk;
import com.yeebotech.yeeboai.chat.entity.Conversation;
import com.yeebotech.yeeboai.chat.repository.ChatChunkRepository;
import com.yeebotech.yeeboai.chat.repository.ChatRepository;
import com.yeebotech.yeeboai.chat.repository.ConversationRepository;
import com.yeebotech.yeeboai.chat.service.ChatService;
import com.yeebotech.yeeboai.chat.util.OpenAIClientUtil;
import com.yeebotech.yeeboai.common.exception.ResourceNotFoundException;
import com.yeebotech.yeeboai.common.exception.UnauthorizedException;
import com.yeebotech.yeeboai.document.entity.Chunk;
import com.yeebotech.yeeboai.document.entity.KnowledgeBase;
import com.yeebotech.yeeboai.document.service.DocumentService;
import com.yeebotech.yeeboai.document.service.KnowledgeBaseService;
import com.yeebotech.yeeboai.milvus.service.MilvusService;
import io.milvus.v2.service.vector.response.SearchResp;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private MilvusService milvusService;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private ChatChunkRepository chatChunkRepository;

    @Override
    public ChatResponse processChat(Long userId, ChatRequest chatRequest) {
        // Step 1: 提取用户输入
        List<Map<String, String>> messages = convertChatRequestToList(chatRequest);

        Map<String, String> lastMessage = null;
        if (messages != null && !messages.isEmpty()) {
            lastMessage = messages.get(messages.size() - 1);
        }

        String content = null;
        if (lastMessage != null) {
            content = lastMessage.get("content");
        }

        // Step 2: 搜索向量数据库
        float[] embedding = documentService.embeddingForText(content);
        List<KnowledgeBase> knowledgeBaseList = knowledgeBaseService.getKnowledgeBasesByUserId(userId);
        // 将 List<KnowledgeBase> 转换为 List<Long>，获取每个 KnowledgeBase 的 id
        List<Long> knowledgeBaseIds = knowledgeBaseList.stream()
                .map(KnowledgeBase::getId)  // 假设 KnowledgeBase 类有 getId() 方法
                .collect(Collectors.toList());
        List<List<SearchResp.SearchResult>> searchResults = milvusService.searchVectors(embedding, knowledgeBaseIds);
        StringBuilder vectorSearchResult = new StringBuilder();

        List<Chunk> chunks = new ArrayList<>();
        for (List<SearchResp.SearchResult> results : searchResults) {
            for (SearchResp.SearchResult result : results) {
                System.out.println("SearchResult: " + result);
                // 获取 entity 中的特定字段（假设你需要获取 'name' 字段）
                Map<String, Object> entity = result.getEntity();
                Long chunkID = (Long) entity.get("chunk_id");
                Chunk chunk = documentService.getChunkById(chunkID);
                chunks.add(chunk);
                vectorSearchResult.append(chunk.getContent()).append("\n");
            }
        }

        // Step 3: 拼接 System message
        Map<String, String> systemMessage = Map.of(
                "role", "system",
                "content", "使用 <Reference></Reference> 标记中的内容作为本次对话的参考：\n\n" +
                        "<Reference>" + vectorSearchResult + "</Reference>\n\n" +
                        "回答要求：\n" +
                        "- 如果你不清楚答案，你需要澄清。\n" +
                        "- 避免提及你是从 <Reference></Reference> 获取的知识。\n" +
                        "- 保持答案与 <Reference></Reference> 中描述的一致。\n" +
                        "- 使用 Markdown 语法优化回答格式。\n" +
                        "- 使用与问题相同的语言回答。"
        );
        messages.add(0, systemMessage);

        // Step 4: 请求 OpenAI API
        String openAIResponse = OpenAIClientUtil.generateResponse(messages);

        // Step 5: 判断是否存在 conversationId
        int conversationId = chatRequest.getConversationId();
        Conversation conversation = null;

        if (conversationId != 0) {
            conversation = conversationRepository.findById((long) conversationId)
                    .orElseThrow(() -> new IllegalArgumentException("会话不存在"));
            saveChat(content, openAIResponse, conversation, chunks);
        } else {
            conversation = new Conversation();
            conversation.setTitle(content);
            conversation.setUserId(userId);
            conversationRepository.save(conversation);
            saveChat(content, openAIResponse, conversation, chunks);
        }

        // Step 6: 返回结果
        return new ChatResponse(openAIResponse, chunks, conversation);
    }

    private List<Map<String, String>> convertChatRequestToList(ChatRequest chatRequest) {
        try {
            List<ChatRequest.Message> messages = chatRequest.getMessages();
            return messages.stream()
                    .map(message -> Map.of("role", message.getRole(), "content", message.getContent()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error converting chat request to list: " + e.getMessage());
            throw e;
        }
    }

    private void saveChat(String question, String content, Conversation conversation, List<Chunk> chunks) {
        try {
            Chat chat = new Chat();
            chat.setQuestion(question);
            chat.setAnswer(content);
            chat.setConversation(conversation);
            chatRepository.save(chat);
            for (Chunk chunk : chunks) {
                try {
                    ChatChunk chatChunk = new ChatChunk();
                    chatChunk.setChat(chat);
                    chatChunk.setChunk(chunk);
                    chatChunkRepository.save(chatChunk);
                } catch (Exception e) {
                    System.err.println("Error saving chat-chunk association: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving chat: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Conversation> getAllConversations() {
        List<Conversation> conversations = conversationRepository.findAll();
        return conversations;
    }

    @Override
    public List<Conversation> getConversationByUserId(Long userId) {
        List<Conversation> conversations = conversationRepository.findByUserId(userId);
        return conversations;
    }



    private ConversationDTO convertToDTO(Conversation conversation) {
        // 将聊天记录转换为 ChatDTO 列表
        List<ChatDTO> chatDTOList = conversation.getChats().stream()
                .map(chat -> new ChatDTO(chat)) // 使用 ChatDTO 构造函数将 Chat 实体转换为 ChatDTO，自动处理 ChunkDTO 列表
                .collect(Collectors.toList());

        // 返回包含聊天记录的会话DTO
        return new ConversationDTO(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                chatDTOList
        );
    }



    @Override
    public ConversationDTO getConversationById(Long id) {
        Conversation conversation = conversationRepository.findById(id).orElse(null);
        return conversation != null ? convertToDTO(conversation) : null;
    }

    @Override
    @Transactional
    public void deleteConversationById(Long conversationId, Long userId) {
        // 查询资源是否存在
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("资源未找到"));
        // 校验权限
        if (!conversation.getUserId().equals(userId)) {
            throw new UnauthorizedException("无权限删除该资源");
        }

        // 删除会话下的所有聊天记录及其相关的块
        List<Chat> chats = chatRepository.findByConversationId(conversationId);
        for (Chat chat : chats) {
            // 删除每个 Chat 的 ChatChunk
            chatChunkRepository.deleteByChatId(chat.getId());
        }
        // 删除聊天记录
        chatRepository.deleteByConversationId(conversationId);

        // 删除会话
        conversationRepository.deleteById(conversationId);
    }

}
