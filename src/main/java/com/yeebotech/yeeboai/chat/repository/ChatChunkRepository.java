package com.yeebotech.yeeboai.chat.repository;

import com.yeebotech.yeeboai.chat.entity.ChatChunk;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatChunkRepository extends JpaRepository<ChatChunk, Long> {
    void deleteByChatId(Long chatId);
    List<ChatChunk> findByChunkId(Long chunkId);
}

