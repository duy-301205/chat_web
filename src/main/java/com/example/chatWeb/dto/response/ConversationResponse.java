package com.example.chatWeb.dto.response;

import com.example.chatWeb.enums.ConvType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationResponse {

    private Long id;
    private String name;
    private String avatarUrl;
    private ConvType type;
    private String lastMessage;
    private Long lastMessageSenderId;
    private String lastMessageSenderName;
    private OffsetDateTime lastMessageAt;
    private boolean isOnline;
    private Long unreadCount;

    private Long partnerId;
    private Integer memberCount;
    private OffsetDateTime lastSeenAt;
}
