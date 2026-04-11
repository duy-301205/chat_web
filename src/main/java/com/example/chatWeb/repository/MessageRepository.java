package com.example.chatWeb.repository;

import com.example.chatWeb.entity.Message;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // Lấy tin nhắn theo conversationId, sắp xếp theo thời gian giảm dần
    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    @Query("SELECT m FROM Message m " +
            "JOIN FETCH m.sender " +
            "LEFT JOIN FETCH m.replyTo " +
            "WHERE m.conversation.id = :conversationId")
    Page<Message> findByConversationIdWithSender(@Param("conversationId") Long conversationId, Pageable pageable);
}
