package com.example.chatWeb.repository;

import com.example.chatWeb.entity.ConversationMember;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {

    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);

    Optional<ConversationMember> findByConversationIdAndUserId(Long conversationId, Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ConversationMember cm WHERE cm.conversation.id = :conversationId AND cm.user.id = :userId")
    void deleteByConversationIdAndUserId(Long conversationId, Long userId);
}
