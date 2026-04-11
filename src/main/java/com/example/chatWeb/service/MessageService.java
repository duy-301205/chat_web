package com.example.chatWeb.service;

import com.example.chatWeb.dto.request.MessageRequest;
import com.example.chatWeb.dto.response.MessageResponse;
import com.example.chatWeb.entity.Conversation;
import com.example.chatWeb.entity.Message;
import com.example.chatWeb.entity.User;
import com.example.chatWeb.enums.MsgType;
import com.example.chatWeb.exception.AppException;
import com.example.chatWeb.exception.ErrorCode;
import com.example.chatWeb.repository.ConversationRepository;
import com.example.chatWeb.repository.MessageRepository;
import com.example.chatWeb.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ConversationService conversationService;

    public MessageResponse sendMessage(MessageRequest request) {
        Long currentId = getCurrentUser();

        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        if (!conversationService.isMember(conversation, currentId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(userRepository.getReferenceById(currentId));
        message.setContent(request.getContent());
        message.setType(request.getType() != null ? request.getType() : MsgType.TEXT);

        if(request.getReplyToId() != null) {
            message.setReplyTo(messageRepository.getReferenceById(request.getReplyToId()));
        }

        Message savedMessage = messageRepository.save(message);

        conversation.setLastMessage(savedMessage);
        conversation.setLastMessageAt(savedMessage.getCreatedAt());
        conversationRepository.save(conversation);

        return mapToResponse(savedMessage);
    }

    public Page<MessageResponse> getMessages(Long conversationId, Pageable pageable) {
        Long currentId = getCurrentUser();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        if (!conversationService.isMember(conversation, currentId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public void recallMessage(Long messageId) {
        Long currentId = getCurrentUser();

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getSender().getId().equals(currentId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (message.getIsDeleted()) {
            return;
        }

        message.setIsDeleted(true);
        message.setDeletedAt(OffsetDateTime.now());
        message.setDeletedBy(userRepository.getReferenceById(message.getSender().getId()));
        message.setContent("Tin nhắn đã được thu hồi");

        messageRepository.save(message);
    }

    private MessageResponse mapToResponse(Message message) {
        User sender = message.getSender();

        String displayContent = message.getIsDeleted()
                ? "Tin nhắn đã được thu hồi"
                : message.getContent();

        return MessageResponse.builder()
                .id(message.getId())
                .senderId(sender.getId())
                .senderName(sender.getActualUsername())
                .senderAvatar(sender.getAvatarUrl())
                .content(displayContent)
                .type(message.getType())
                .createdAt(message.getCreatedAt())
                .replyToId(message.getReplyTo() != null ? message.getReplyTo().getId() : null)
                .replyToContent(message.getReplyTo() != null ? message.getReplyTo().getContent() : null)
                .isEdited(message.getIsEdited())
                .isDeleted(message.getIsDeleted())
                .build();
    }

    private Long getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user.getId();
        }
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
}
