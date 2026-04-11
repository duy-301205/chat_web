package com.example.chatWeb.dto.request;

import com.example.chatWeb.enums.MsgType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageRequest {
    private Long conversationId;
    private String content;
    private MsgType type;
    private Long replyToId;
}
