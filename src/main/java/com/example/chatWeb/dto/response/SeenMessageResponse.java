package com.example.chatWeb.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class SeenMessageResponse {
    private Long conversationId;
    private Long messageId;
    private Long userId;
    private OffsetDateTime seenAt;
}
