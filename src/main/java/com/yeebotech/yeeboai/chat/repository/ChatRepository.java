package com.yeebotech.yeeboai.chat.repository;

import com.yeebotech.yeeboai.chat.entity.Chat;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    void deleteByConversationId(Long conversationId);
    List<Chat> findByConversationId(Long conversationId);
}

