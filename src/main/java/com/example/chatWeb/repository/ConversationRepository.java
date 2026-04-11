package com.example.chatWeb.repository;

import com.example.chatWeb.entity.Conversation;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    // Lấy danh sách phòng chat của một User
    @Query("SELECT DISTINCT c FROM Conversation c " +
            "JOIN c.members m " +
            "LEFT JOIN FETCH c.lastMessage lm " +
            "LEFT JOIN FETCH lm.sender " +
            "LEFT JOIN FETCH c.members " +
            "WHERE m.user.id = :userId " +
            "ORDER BY c.lastMessageAt DESC NULLS LAST")
    List<Conversation> findAllMyConversations(@Param("userId") Long userId);


    // Kiểm tra xem đã có phòng chat 1-1 giữa người này và người kia chưa
    @Query("SELECT c FROM Conversation c " +
            "JOIN c.members m1 " +
            "JOIN c.members m2 " +
            "WHERE c.type = 'PRIVATE' " +
            "AND m1.user.id = :id1 " +
            "AND m2.user.id = :id2")
    Optional<Conversation> findPrivateChat(@Param("id1") Long id1, @Param("id2") Long id2);
}
