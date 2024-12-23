package com.yeebotech.yeeboai.chat.repository;

import com.yeebotech.yeeboai.chat.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    // JpaRepository 自带了 saveAll 和其他常用方法
    List<Conversation> findByUserId(Long userId);
}
