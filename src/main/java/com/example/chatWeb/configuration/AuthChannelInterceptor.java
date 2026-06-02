package com.example.chatWeb.configuration;

import com.example.chatWeb.repository.InvalidedTokenRepository;
import com.example.chatWeb.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final InvalidedTokenRepository invalidedTokenRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    String tokenId = jwtService.extractId(token);
                    if (invalidedTokenRepository.existsById(tokenId)) {
                        log.warn("[WebSocket] Kết nối bị chặn: Token đã bị vô hiệu hóa (Blacklist).");
                        return null;
                    }

                    String userEmail = jwtService.extractEmail(token);

                    if (userEmail != null && jwtService.validateToken(token, userEmail)) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userEmail, null, null);

                        accessor.setUser(authentication);
                        log.info(" [WebSocket] Xác thực thành công cho tài khoản: {}", userEmail);
                    } else {
                        log.warn(" [WebSocket] Token không hợp lệ cho tài khoản: {}", userEmail);
                    }
                } catch (Exception e) {
                    log.error(" [WebSocket] Lỗi nghiêm trọng khi xác thực JWT: {}", e.getMessage());
                }
            } else {
                log.warn(" [WebSocket] Kết nối bị từ chối do thiếu Header Authorization hợp lệ.");
            }
        }
        return message;
    }
}