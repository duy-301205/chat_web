package com.example.chatWeb.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchUserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private String status;
}
