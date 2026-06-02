package com.example.chatWeb.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebSocketRecallRequest {
    private Long messageId;
    private Long conversationId;
    private boolean isRecalled = true;
}
