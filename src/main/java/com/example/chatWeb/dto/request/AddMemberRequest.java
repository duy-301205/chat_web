package com.example.chatWeb.dto.request;

import com.example.chatWeb.entity.Conversation;
import com.example.chatWeb.entity.User;
import com.example.chatWeb.enums.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMemberRequest {

    private Conversation conversation;
    private User user;
    private MemberRole role;
}
