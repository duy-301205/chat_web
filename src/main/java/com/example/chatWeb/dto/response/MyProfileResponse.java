package com.example.chatWeb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyProfileResponse {

    private Long id;
    private String email;
    private String username;
    private String avatarUrl;
    private String status;
    private OffsetDateTime lastSeen;
}
