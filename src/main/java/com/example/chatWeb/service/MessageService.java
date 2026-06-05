package com.example.chatWeb.service;

import com.example.chatWeb.dto.request.EditMessageRequest;
import com.example.chatWeb.dto.request.MessageRequest;
import com.example.chatWeb.dto.request.SearchMessagesRequest;
import com.example.chatWeb.dto.request.SeenMessageRequest;
import com.example.chatWeb.dto.response.AttachmentResponse;
import com.example.chatWeb.dto.response.MessageResponse;
import com.example.chatWeb.dto.response.SeenMessageResponse;
import com.example.chatWeb.entity.*;
import com.example.chatWeb.enums.MsgType;
import com.example.chatWeb.exception.AppException;
import com.example.chatWeb.exception.ErrorCode;
import com.example.chatWeb.repository.ConversationMemberRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ConversationService conversationService;
    private final CloudinaryService cloudinaryService;
    private final ConversationMemberRepository conversationMemberRepository;

    @Transactional
    public MessageResponse sendMessage(MessageRequest request,String email, List<MultipartFile> files) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Long currentId = currentUser.getId();

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

        // Xử lý phản hồi tin nhắn cũ (Reply)
        if(request.getReplyToId() != null) {
            message.setReplyTo(messageRepository.getReferenceById(request.getReplyToId()));
        }

        // Xử lý đính kèm File / Hình ảnh qua Cloudinary
        if(files != null && !files.isEmpty()) {
            List<Attachment> attachments = new ArrayList<>();

            for(MultipartFile file : files) {
                if(!file.isEmpty()) {
                    Map uploadResult = cloudinaryService.uploadFile(file);

                    Attachment attachment = new Attachment();
                    attachment.setFileUrl(uploadResult.get("secure_url").toString());
                    attachment.setPublicId(uploadResult.get("public_id").toString());
                    attachment.setFileType(file.getContentType());
                    attachment.setMessage(message);
                    attachment.setFileSize(file.getSize());

                    attachments.add(attachment);
                }
            }

            if(!attachments.isEmpty()) {
                message.setAttachments(attachments);

                String firstContentType = files.get(0).getContentType();
                message.setType(firstContentType != null && firstContentType.startsWith("image")
                        ? MsgType.IMAGE : MsgType.FILE);
            }
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
    public void recallMessage(Long messageId, String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Long currentId = currentUser.getId();

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getSender().getId().equals(currentId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (message.getIsDeleted() != null && message.getIsDeleted()) {
            return;
        }

        if(message.getAttachments() != null) {
            for(Attachment attachment : message.getAttachments()) {
                try {
                    cloudinaryService.deleteFile(attachment.getPublicId());
                } catch (Exception e) {
                    System.err.println("Lỗi xóa file: " + e.getMessage());
                }
            }
        }

        message.setIsDeleted(true);
        message.setDeletedAt(OffsetDateTime.now());
        message.setDeletedBy(userRepository.getReferenceById(message.getSender().getId()));
        message.setContent("Tin nhắn đã được thu hồi");

        messageRepository.save(message);
    }

    @Transactional
    public MessageResponse editMessage(EditMessageRequest request, String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Long currentId = currentUser.getId();

        Message message = messageRepository.findById(request.getMessageId())
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getSender().getId().equals(currentId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (message.getIsDeleted() != null && message.getIsDeleted()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (message.getType() != MsgType.TEXT) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        message.setContent(request.getContent());
        message.setIsEdited(true);

        Message savedMessage = messageRepository.save(message);

        Conversation conversation = savedMessage.getConversation();

        if (conversation.getLastMessage() != null
                && conversation.getLastMessage().getId().equals(savedMessage.getId())) {
            conversation.setLastMessage(savedMessage);
            conversationRepository.save(conversation);
        }

        return mapToResponse(savedMessage);
    }

    @Transactional
    public SeenMessageResponse seenMessage(SeenMessageRequest request, String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Long currentId = currentUser.getId();

        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        if (!conversationService.isMember(conversation, currentId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Message message = messageRepository.findById(request.getMessageId())
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getConversation().getId().equals(request.getConversationId())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        ConversationMember member = conversationMemberRepository
                .findByConversationIdAndUserId(request.getConversationId(), currentId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_IN_CONVERSATION));

        Message currentLastSeen = member.getLastSeenMessage();

        if (currentLastSeen == null || message.getId() > currentLastSeen.getId()) {
            member.setLastSeenMessage(message);
            conversationMemberRepository.save(member);
        }

        return SeenMessageResponse.builder()
                .conversationId(request.getConversationId())
                .messageId(message.getId())
                .userId(currentId)
                .seenAt(OffsetDateTime.now())
                .build();
    }

    public List<MessageResponse> searchMessages(SearchMessagesRequest request) {
        Long currentUserId = getCurrentUser();

        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        if(!conversationService.isMember(conversation, currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if(request.getKeyword() == null || request.getKeyword().trim().isEmpty()) {
            return new ArrayList<>();
        }
        return messageRepository.findByConversationIdAndContentContainingIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc(
                request.getConversationId(), request.getKeyword()
        )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private MessageResponse mapToResponse(Message message) {
        User sender = message.getSender();
        boolean isDeleted = message.getIsDeleted() != null && message.getIsDeleted();

        String displayContent = message.getIsDeleted()
                ? "Tin nhắn đã được thu hồi"
                : message.getContent();

        List<AttachmentResponse> attachments = null;
        if(message.getAttachments() != null && !isDeleted) {
            attachments = message.getAttachments().stream()
                    .map(a -> AttachmentResponse.builder()
                            .id(a.getId())
                            .fileUrl(a.getFileUrl())
                            .fileType(a.getFileType())
                            .fileSize(a.getFileSize())
                            .build())
                    .toList();
        }

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
                .isEdited(message.getIsEdited() != null && message.getIsEdited())
                .isDeleted(message.getIsDeleted() != null && message.getIsDeleted())
                .attachments(attachments)
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
