package com.example.chatWeb.repository;

import com.example.chatWeb.entity.Message;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // Lấy tin nhắn theo conversationId, sắp xếp theo thời gian giảm dần
    @EntityGraph(attributePaths = {"sender", "replyTo", "attachments"})
    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    @Query("SELECT m FROM Message m " +
            "JOIN FETCH m.sender " +
            "LEFT JOIN FETCH m.replyTo " +
            "WHERE m.conversation.id = :conversationId")
    Page<Message> findByConversationIdWithSender(@Param("conversationId") Long conversationId, Pageable pageable);

    @Query("""
    SELECT COUNT(m)
    FROM Message m
    WHERE m.conversation.id = :conversationId
      AND m.id > :lastSeenMessageId
      AND m.sender.id <> :userId
      AND m.isDeleted = false
""")
    Long countUnreadAfterLastSeen(
            @Param("conversationId") Long conversationId,
            @Param("lastSeenMessageId") Long lastSeenMessageId,
            @Param("userId") Long userId
    );

    @Query("""
    SELECT COUNT(m)
    FROM Message m
    WHERE m.conversation.id = :conversationId
      AND m.sender.id <> :userId
      AND m.isDeleted = false
""")
    Long countUnreadWhenNoLastSeen(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId
    );

    List<Message> findByConversationIdAndContentContainingIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc(
            Long conversationId, String content
    );
}
