package com.example.chatWeb.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebSocketTypingRequest {
    private Long conversationId;
    private Long userId;
    private String username;

    @JsonProperty("isTyping")
    private boolean isTyping;
}
