package com.example.chatWeb.service;

import com.example.chatWeb.dto.request.AddGroupMembersRequest;
import com.example.chatWeb.dto.request.AddMemberRequest;
import com.example.chatWeb.dto.request.CreateConversationRequest;
import com.example.chatWeb.dto.response.ConversationResponse;
import com.example.chatWeb.dto.response.MemberResponse;
import com.example.chatWeb.entity.Conversation;
import com.example.chatWeb.entity.ConversationMember;
import com.example.chatWeb.entity.Message;
import com.example.chatWeb.entity.User;
import com.example.chatWeb.enums.ConvType;
import com.example.chatWeb.enums.MemberRole;
import com.example.chatWeb.exception.AppException;
import com.example.chatWeb.exception.ErrorCode;
import com.example.chatWeb.repository.ConversationMemberRepository;
import com.example.chatWeb.repository.ConversationRepository;
import com.example.chatWeb.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private final ConversationRepository conversationRepository;private final UserStatusService userStatusService;
    private final UserRepository userRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final MemberService memberService;
    private final EntityManager entityManager;

    public List<MemberResponse> getConversationMembers(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        return conversation.getMembers().stream()
                .map(m -> MemberResponse.builder()
                        .id(m.getId())
                        .username(m.getUser().getActualUsername())
                        .nickName(m.getNickname())
                        .avatarUrl(m.getUser().getAvatarUrl())
                        .role(m.getRole())
                        .isOnline(userStatusService.isUserOnline(m.getUser().getId()))
                        .joinedAt(m.getJoinedAt())
                        .build())
                .toList();
    }

    @Transactional
    public void addMemberToConversation(Long conversationId, AddGroupMembersRequest request) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        if(conversation.getType() == ConvType.PRIVATE) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        List<Long> userIds = request.getUserIds().stream()
                        .distinct()
                                .toList();

        List<User> users = userRepository.findAllById(userIds);
        for(User user : users) {
            if(!isMember(conversation, user.getId())) {
                memberService.addMember(new AddMemberRequest(conversation, user, MemberRole.MEMBER));
            }
        }
    }

    public boolean isMember(Conversation conversation, Long userId) {
        return conversation.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(userId));
    }

    public List<ConversationResponse> getMyConversations() {
        Long currentUserId = getCurrentUser();
        List<Conversation> conversations = conversationRepository.findAllMyConversations(currentUserId);
        return conversations.stream()
                .map(c -> mapToResponse(c, currentUserId))
                .toList();
    }

    @Transactional
    public ConversationResponse createConversation(CreateConversationRequest request) {

        Long creatorId = getCurrentUser();
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if(ConvType.PRIVATE == request.getType()) {
            Long parterId = request.getParticipantIds().get(0);

            Optional<Conversation> existingConversation = conversationRepository.findPrivateChat(creatorId, parterId);
            if(existingConversation.isPresent()) {
                return mapToResponse(existingConversation.get(), creatorId);
            }

            Conversation conv = createEntity(ConvType.PRIVATE, null, null, creator);
            Conversation saved = conversationRepository.save(conv);

            memberService.addMember(new AddMemberRequest(saved, creator, MemberRole.ADMIN));

            User parter = userRepository.findById(parterId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            memberService.addMember(new AddMemberRequest(saved, parter, MemberRole.MEMBER));

            return mapToResponse(saved, creatorId);
        }

        // Chat Group
        Conversation group = createEntity(ConvType.GROUP, request.getName(), request.getAvatarUrl(), creator);
        Conversation savedGroup = conversationRepository.save(group);

        memberService.addMember(new AddMemberRequest(savedGroup, creator, MemberRole.ADMIN));

        if(request.getParticipantIds() != null) {
            for(Long userId : request.getParticipantIds()) {
                if(!userId.equals(creatorId)) {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                    memberService.addMember(new AddMemberRequest(savedGroup, user, MemberRole.MEMBER));
                }
            }
        }

        entityManager.flush();
        entityManager.refresh(savedGroup);
        return mapToResponse(savedGroup, creatorId);
    }

    private Conversation createEntity(ConvType type, String name, String avatar, User creator) {
        Conversation conversation = new Conversation();
        conversation.setType(type);
        conversation.setName(name);
        conversation.setAvatarUrl(avatar);
        conversation.setCreatedBy(creator);
        conversation.setLastMessageAt(OffsetDateTime.now());

        return conversation;
    }

    private ConversationResponse mapToResponse(Conversation conv, Long currentUserId) {
        String dispalyName = conv.getName();
        String displayAvatar = conv.getAvatarUrl();
        Long parterId = null;
        boolean onlineStatus = false;
        OffsetDateTime lastSeenAt = null;

        if (conv.getType() == ConvType.PRIVATE) {
            User parter = conv.getMembers().stream()
                    .filter(m -> !m.getUser().getId().equals(currentUserId))
                    .map(ConversationMember::getUser)
                    .findFirst().orElse(null);

            if (parter != null) {
                dispalyName = parter.getActualUsername();
                displayAvatar = parter.getAvatarUrl();
                parterId = parter.getId();

                onlineStatus = userStatusService.isUserOnline(parter.getId());

                if(!onlineStatus) {
                    lastSeenAt = parter.getLastSeen();
                }
            }
        }

        ConversationResponse response = ConversationResponse.builder()
                .id(conv.getId())
                .name(dispalyName)
                .avatarUrl(displayAvatar)
                .type(conv.getType())
                .partnerId(parterId)
                .isOnline(onlineStatus)
                .lastSeenAt(lastSeenAt)
                .lastMessageAt(conv.getLastMessageAt())
                .unreadCount(0L)
                .build();

        if(conv.getLastMessage() != null) {
            Message lastMessage = conv.getLastMessage();
            response.setLastMessage(lastMessage.getContent());
            response.setLastMessageSenderId(lastMessage.getSender().getId());

            if(lastMessage.getSender().getId().equals(currentUserId)) {
                response.setLastMessageSenderName("You");
            } else {
                response.setLastMessageSenderName(lastMessage.getSender().getUsername());
            }
        }

        if(conv.getType() == ConvType.GROUP) {
            response.setMemberCount(conv.getMembers().size());
        }

        return response;
    }

    private Long getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user.getId();
        }
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
}
