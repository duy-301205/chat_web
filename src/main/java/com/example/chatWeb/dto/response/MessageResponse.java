package com.example.chatWeb.dto.response;

import com.example.chatWeb.enums.MsgType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {

    private Long id;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private MsgType type;
    private OffsetDateTime createdAt;
    private Long replyToId;
    private String replyToContent;
    private Boolean isEdited;
    private Boolean isDeleted;

}
