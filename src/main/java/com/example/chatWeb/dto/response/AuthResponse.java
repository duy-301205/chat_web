package com.example.chatWeb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private Long id;
    private String email;
    private String username;
    private String avatarUrl;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
