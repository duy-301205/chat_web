package com.example.chatWeb.service;

import com.example.chatWeb.dto.request.AddGroupMembersRequest;
import com.example.chatWeb.dto.request.AddMemberRequest;
import com.example.chatWeb.dto.request.CreateConversationRequest;
import com.example.chatWeb.dto.request.RemoveMembersRequest;
import com.example.chatWeb.dto.response.ConversationResponse;
import com.example.chatWeb.dto.response.MemberResponse;
import com.example.chatWeb.entity.Conversation;
import com.example.chatWeb.entity.ConversationMember;
import com.example.chatWeb.entity.Message;
import com.example.chatWeb.entity.User;
import com.example.chatWeb.enums.ConvType;
import com.example.chatWeb.enums.MemberRole;
import com.example.chatWeb.enums.MsgType;
import com.example.chatWeb.exception.AppException;
import com.example.chatWeb.exception.ErrorCode;
import com.example.chatWeb.repository.ConversationMemberRepository;
import com.example.chatWeb.repository.ConversationRepository;
import com.example.chatWeb.repository.MessageRepository;
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
    private final ConversationRepository conversationRepository;
    private final UserStatusService userStatusService;
    private final UserRepository userRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final MemberService memberService;
    private final EntityManager entityManager;
    private final MessageRepository messageRepository;

    @Transactional
    public void leaveGroup(Long conversationId) {
        Long currentId = getCurrentUser();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        if (conversation.getType() == ConvType.PRIVATE) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        ConversationMember me = conversationMemberRepository.findByConversationIdAndUserId(conversationId, currentId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_IN_CONVERSATION));

        User leaver = me.getUser();
        String leaverUsername = leaver.getActualUsername();

        if(me.getRole() == MemberRole.ADMIN) {
            handleAdminLeave(conversation, currentId);
        }
        conversationMemberRepository.delete(me);

        conversation.getMembers().removeIf(m -> m.getUser().getId().equals(currentId));

        Message leaveNotice = Message.builder()
                .conversation(conversation)
                .sender(leaver)
                .content(leaverUsername + " đã rời khỏi nhóm trò chuyện.")
                .type(MsgType.SYSTEM)
                .isDeleted(false)
                .isEdited(false)
                .createdAt(OffsetDateTime.now())
                .build();
        messageRepository.save(leaveNotice);
    }

    private void handleAdminLeave(Conversation conversation, Long adminId) {
        List<ConversationMember> otherMembers = conversation.getMembers().stream()
                .filter(m -> !m.getUser().getId().equals(adminId))
                .toList();

        if(!otherMembers.isEmpty()) {
            ConversationMember newAdmin = otherMembers.get(0);
            newAdmin.setRole(MemberRole.ADMIN);
            conversationMemberRepository.save(newAdmin);
        } else {
            conversationRepository.delete(conversation);
        }
    }

    @Transactional
    public void removeMembersFromGroup(Long conversationId, RemoveMembersRequest request) {
        Long currentUserId = getCurrentUser();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        if (conversation.getType() == ConvType.PRIVATE) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        List<Long> targetIds = request.getUserIds().stream().distinct().toList();

        for(Long targetId : targetIds) {
            if(!isMember(conversation, targetId)) {
                continue;
            }

            if(!currentUserId.equals(targetId)) {
                ConversationMember member = conversationMemberRepository.findByConversationIdAndUserId(conversationId, currentUserId)
                        .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

                if(member.getRole() != MemberRole.ADMIN) {
                    throw new AppException(ErrorCode.UNAUTHORIZED);
                }
            }
            conversationMemberRepository.deleteByConversationIdAndUserId(conversationId, targetId);
        }
    }

    @Transactional
    public Long findOrCreatePrivateConversation(Long targetUserId) {
        CreateConversationRequest request = new CreateConversationRequest();
        request.setType(ConvType.PRIVATE);
        request.setParticipantIds(List.of(targetUserId));

        ConversationResponse response = this.createConversation(request);

        return response.getId();
    }

    public List<MemberResponse> getConversationMembers(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        return conversation.getMembers().stream()
                .map(m -> MemberResponse.builder()
                        .id(m.getId())
                        .userId(m.getUser().getId())
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

        if (ConvType.PRIVATE == request.getType()) {
            Long partnerId = request.getParticipantIds().get(0);

            if (creatorId.equals(partnerId)) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }

            Long userA = Math.min(creatorId, partnerId);
            Long userB = Math.max(creatorId, partnerId);
            String privateKey = userA + "_" + userB;

            Optional<Conversation> existingConversation =
                    conversationRepository.findByPrivateKey(privateKey);

            if (existingConversation.isPresent()) {
                return mapToResponse(existingConversation.get(), creatorId);
            }

            User partner = userRepository.findById(partnerId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            Conversation conv = createEntity(ConvType.PRIVATE, null, null, creator);
            conv.setPrivateKey(privateKey);

            Conversation saved = conversationRepository.save(conv);

            memberService.addMember(new AddMemberRequest(saved, creator, MemberRole.ADMIN));
            memberService.addMember(new AddMemberRequest(saved, partner, MemberRole.MEMBER));

            entityManager.flush();
            entityManager.refresh(saved);

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
        String displayName = conv.getName();
        String displayAvatar = conv.getAvatarUrl();
        Long parterId = null;
        boolean onlineStatus = false;
        OffsetDateTime lastSeenAt = null;

        if (conv.getType() == ConvType.PRIVATE) {
            ConversationMember parterMember = conv.getMembers().stream()
                    .filter(m -> !m.getUser().getId().equals(currentUserId))
                    .findFirst().orElse(null);

            if (parterMember != null) {
                User parter = parterMember.getUser();
                displayAvatar = parter.getAvatarUrl();
                parterId = parter.getId();

                onlineStatus = userStatusService.isUserOnline(parter.getId());

                if(!onlineStatus) {
                    lastSeenAt = parter.getLastSeen();
                }

                if (parterMember.getNickname() != null && !parterMember.getNickname().trim().isEmpty()) {
                    displayName = parterMember.getNickname();
                } else {
                    displayName = parter.getActualUsername();
                }
            }
        }

        Long unreadCount = calculateUnreadCount(conv, currentUserId);

        ConversationResponse response = ConversationResponse.builder()
                .id(conv.getId())
                .name(displayName)
                .avatarUrl(displayAvatar)
                .type(conv.getType())
                .partnerId(parterId)
                .isOnline(onlineStatus)
                .lastSeenAt(lastSeenAt)
                .lastMessageAt(conv.getLastMessageAt())
                .unreadCount(unreadCount)
                .build();

        if(conv.getLastMessage() != null) {
            Message lastMessage = conv.getLastMessage();
            response.setLastMessage(lastMessage.getContent());
            response.setLastMessageSenderId(lastMessage.getSender().getId());

            if(lastMessage.getSender().getId().equals(currentUserId)) {
                response.setLastMessageSenderName("You");
            } else {
                Long senderId = lastMessage.getSender().getId();
                String senderName = conv.getMembers().stream()
                        .filter(m -> m.getUser().getId().equals(senderId))
                                .map(ConversationMember::getNickname)
                                        .filter(nickname -> nickname != null && !nickname.trim().isEmpty())
                                                .findFirst()
                                                        .orElse(lastMessage.getSender().getActualUsername());
                response.setLastMessageSenderName(senderName);
            }
        }

        if(conv.getType() == ConvType.GROUP) {
            response.setMemberCount(conv.getMembers().size());
        }

        return response;
    }

    private Long calculateUnreadCount(Conversation conversation, Long currentUserId) {
        ConversationMember member = conversation.getMembers().stream()
                .filter(m -> m.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElse(null);

        if (member == null) {
            return 0L;
        }

        Message lastSeenMessage = member.getLastSeenMessage();

        if (lastSeenMessage == null) {
            return messageRepository.countUnreadWhenNoLastSeen(
                    conversation.getId(),
                    currentUserId
            );
        }

        return messageRepository.countUnreadAfterLastSeen(
                conversation.getId(),
                lastSeenMessage.getId(),
                currentUserId
        );
    }
    
    public List<Long> getMemberUserIdsByConversationId(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        return conversation.getMembers().stream()
                .map(m -> m.getUser().getId())
                .toList();
    }

    private Long getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user.getId();
        }
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
}
