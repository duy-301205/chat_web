package com.example.chatWeb.service;

import com.example.chatWeb.dto.response.FriendResponse;
import com.example.chatWeb.entity.Friendship;
import com.example.chatWeb.entity.User;
import com.example.chatWeb.enums.FriendStatus;
import com.example.chatWeb.exception.AppException;
import com.example.chatWeb.exception.ErrorCode;
import com.example.chatWeb.repository.FriendshipRepository;
import com.example.chatWeb.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendShipService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final UserStatusService userStatusService;

    @Transactional
    public void sendFriendRequest(Long targetUserId) {

        User actionUser = getCurrentUser();
        Long actionUserId = actionUser.getId();

        if(actionUserId.equals(targetUserId)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Long id1 = Math.min(actionUserId, targetUserId);
        Long id2 = Math.max(actionUserId, targetUserId);

        Optional<Friendship> existingOpt = friendshipRepository.findBySubIds(id1, id2);
        if(existingOpt.isPresent()) return;

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User user1 = (actionUserId < targetUserId) ? actionUser : targetUser;
        User user2 = (actionUserId < targetUserId) ? targetUser : actionUser;

        Friendship newFriendship = Friendship.builder()
                .user(user1)
                .friend(user2)
                .status(FriendStatus.PENDING)
                .build();

        friendshipRepository.save(newFriendship);
    }

    @Transactional
    public void acceptFriendRequest(Long targetUserId) {
        User currentUser = getCurrentUser();
        Long actionUserId = currentUser.getId();

        Long id1 = Math.min(actionUserId, targetUserId);
        Long id2 = Math.max(actionUserId, targetUserId);

        Friendship friendship = friendshipRepository.findBySubIds(id1, id2)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));

        if(friendship.getStatus() == FriendStatus.PENDING) {
            friendship.setStatus(FriendStatus.ACCEPTED);
            friendshipRepository.save(friendship);
        }
    }

    @Transactional
    public void removeFriend(Long targetUserId) {
        User currentUser = getCurrentUser();
        Long actionUserId = currentUser.getId();

        Long id1 = Math.min(actionUserId, targetUserId);
        Long id2 = Math.max(actionUserId, targetUserId);

        Friendship friendship = friendshipRepository.findBySubIds(id1, id2)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));

        if (friendship.getStatus() != FriendStatus.BLOCKED) {
            friendshipRepository.delete(friendship);
        }
    }

    @Transactional
    public void blockUser(Long targetUserId) {
        User currentUser = getCurrentUser();
        Long actionUserId = currentUser.getId();

        if (actionUserId.equals(targetUserId)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Long id1 = Math.min(actionUserId, targetUserId);
        Long id2 = Math.max(actionUserId, targetUserId);

        Friendship friendship = friendshipRepository.findBySubIds(id1, id2).orElse(null);

        if (friendship == null) {
            User user1 = userRepository.findById(id1).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            User user2 = userRepository.findById(id2).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            friendship = Friendship.builder()
                    .user(user1)
                    .friend(user2)
                    .build();
        }

        friendship.setStatus(FriendStatus.BLOCKED);
        friendshipRepository.save(friendship);
    }

    public List<FriendResponse> getMyAcceptedFriends() {
        User currentUser = getCurrentUser();
        Long actionUserId = currentUser.getId();

        List<User> friends = friendshipRepository.findAcceptedFriendsByUserId(actionUserId);

        return friends.stream()
                .map(u -> FriendResponse.builder()
                        .id(u.getId())
                        .username(u.getActualUsername())
                        .nickName(null)
                        .avatarUrl(u.getAvatarUrl())
                        .online(userStatusService.isUserOnline(u.getId()))
                        .build())
                .toList();
    }


    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
}
