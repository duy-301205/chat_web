package com.example.chatWeb.dto.request;

import com.example.chatWeb.enums.MsgType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebSocketMessageRequest {
    private Long conversationId;
    private String content;
    private MsgType type;
    private Long replyToId;
}
