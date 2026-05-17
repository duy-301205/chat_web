package com.example.chatWeb.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebSocketSeenRequest {
    private Long conversationId;
    private Long messageId;
}
