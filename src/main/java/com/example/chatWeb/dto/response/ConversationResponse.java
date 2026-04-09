package com.example.chatWeb.dto.response;

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
    private String type;
    private String lastMessage;
    private OffsetDateTime lastMessageAt;
    private boolean isOnline;
    private Long unreadCount;
}
