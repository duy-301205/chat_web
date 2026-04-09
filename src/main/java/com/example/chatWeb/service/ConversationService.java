package com.example.chatWeb.service;

import com.example.chatWeb.dto.response.ConversationResponse;
import com.example.chatWeb.entity.User;
import com.example.chatWeb.exception.AppException;
import com.example.chatWeb.exception.ErrorCode;
import com.example.chatWeb.repository.ConversationMemberRepository;
import com.example.chatWeb.repository.ConversationRepository;
import com.example.chatWeb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private static final String DEFAULT_GROUP_AVATAR = "https://api.dicebear.com/7.x/identicon/svg?seed=group";

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final UserStatusService userStatusService;
    private final UserRepository userRepository;
    

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
}
