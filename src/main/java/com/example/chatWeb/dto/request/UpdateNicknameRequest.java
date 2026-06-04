package com.example.chatWeb.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateNicknameRequest {
    private Long conversationId;
    private Long userId;
    private String nickname;
}
