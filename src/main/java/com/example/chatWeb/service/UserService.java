package com.example.chatWeb.service;

import com.example.chatWeb.dto.request.SearchUserRequest;
import com.example.chatWeb.dto.response.MyProfileResponse;
import com.example.chatWeb.dto.response.SearchUserResponse;
import com.example.chatWeb.dto.response.UserResponse;
import com.example.chatWeb.entity.User;
import com.example.chatWeb.exception.AppException;
import com.example.chatWeb.exception.ErrorCode;
import com.example.chatWeb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserStatusService userStatusService;

    public List<SearchUserResponse> searchUsers(SearchUserRequest request) {
        User currentUser = getCurrentUser();

        if (request.getKeyword() == null
                        || request.getKeyword().trim().isEmpty()) {
            return List.of();
        }

        return userRepository.searchUsers(
                        request.getKeyword().trim(),
                        currentUser.getId()
                )
                .stream()
                .map(user -> SearchUserResponse.builder()
                        .id(user.getId())
                        .username(user.getActualUsername())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .status(userStatusService.getStatus(user.getId()))
                        .build())
                .toList();
    }

    public MyProfileResponse getMyProfile() {
        User currentUser = getCurrentUser();
        String status = userStatusService.getStatus(currentUser.getId());
        return mapToResponse(currentUser, status);
    }

    public UserResponse getProfileByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String status = userStatusService.getStatus(user.getId());

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .status(status)
                .lastSeen(user.getLastSeen())
                .build();
    }

    public void updateMyStatus(String status) {
        User currentUser = getCurrentUser();
        Long userId = currentUser.getId();

        userStatusService.updateStatus(userId, status);
        if("OFFLINE".equalsIgnoreCase(status)) {
            OffsetDateTime now = OffsetDateTime.now();

            currentUser.setLastSeen(now);
            userRepository.save(currentUser);
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

    private MyProfileResponse mapToResponse(User user, String status) {
        return MyProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .status(status)
                .avatarUrl(user.getAvatarUrl())
                .lastSeen(user.getLastSeen())
                .build();
    }
}
