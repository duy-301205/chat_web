package com.example.chatWeb.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebSocketEditMessageRequest {
    private Long messageId;
    private Long conversationId;
    private String content;
}
