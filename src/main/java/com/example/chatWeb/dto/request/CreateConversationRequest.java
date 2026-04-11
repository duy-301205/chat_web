package com.example.chatWeb.dto.request;

import com.example.chatWeb.enums.ConvType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateConversationRequest {
    private String name;
    private ConvType type;
    private List<Long> participantIds;
    private String avatarUrl;
}
