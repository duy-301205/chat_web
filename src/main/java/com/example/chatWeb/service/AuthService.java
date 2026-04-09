package com.example.chatWeb.service;

import com.example.chatWeb.dto.request.LoginRequest;
import com.example.chatWeb.dto.request.RefreshTokenRequest;
import com.example.chatWeb.dto.request.RegisterRequest;
import com.example.chatWeb.dto.response.LoginResponse;
import com.example.chatWeb.dto.response.AuthResponse;
import com.example.chatWeb.entity.InvalidatedToken;
import com.example.chatWeb.entity.User;
import com.example.chatWeb.exception.AppException;
import com.example.chatWeb.exception.ErrorCode;
import com.example.chatWeb.repository.InvalidedTokenRepository;
import com.example.chatWeb.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final InvalidedTokenRepository invalidedTokenRepository;
    private final UserStatusService userStatusService;

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // Mặc định ảnh đại diện nếu không có
        if (user.getAvatarUrl() == null) {
            user.setAvatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=" + user.getUsername());
        }

        User savedUser = userRepository.save(user);

        return AuthResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .avatarUrl(savedUser.getAvatarUrl())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();
    }

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        user.setLastSeen(OffsetDateTime.now());
        userRepository.save(user);

        userStatusService.updateStatus(user.getId(), "ONLINE");

        return LoginResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        String tokenId = jwtService.extractId(refreshToken);
        if (invalidedTokenRepository.existsById(tokenId)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (jwtService.isTokenExpired(refreshToken)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String email = jwtService.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        logout(request);

        return LoginResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (token == null || token.isEmpty()) return;

        try {
            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            userStatusService.updateStatus(user.getId(), "OFFLINE");
            user.setLastSeen(OffsetDateTime.now());
            userRepository.save(user);

            String tokenId = jwtService.extractId(token);
            Date expiryDate = jwtService.extractExpiration(token);

            OffsetDateTime expiryTime = expiryDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toOffsetDateTime();

            if (invalidedTokenRepository.existsById(tokenId)) {
                log.info("Token {} đã nằm trong danh sách đen từ trước", tokenId);
                return;
            }

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(tokenId)
                    .expiryTime(expiryTime)
                    .build();

            invalidedTokenRepository.save(invalidatedToken);
            log.info("Token {} đã bị vô hiệu hóa thành công", tokenId);

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            String tokenId = e.getClaims().getId();
            if (tokenId != null && !invalidedTokenRepository.existsById(tokenId)) {
                InvalidatedToken expiredToken = InvalidatedToken.builder()
                        .id(tokenId)
                        .expiryTime(e.getClaims().getExpiration().toInstant()
                                .atZone(ZoneId.systemDefault()).toOffsetDateTime())
                        .build();
                invalidedTokenRepository.save(expiredToken);
                log.info("Token hết hạn {} đã được đưa vào danh sách đen", tokenId);
            }
        } catch (Exception e) {
            log.error("Lỗi hệ thống khi Logout: {}", e.getMessage());
        }
    }
}
