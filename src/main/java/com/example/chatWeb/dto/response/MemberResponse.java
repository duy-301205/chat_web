package com.example.chatWeb.dto.response;

import com.example.chatWeb.enums.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberResponse {
    private Long id;
    private String username;
    private String nickName;
    private String avatarUrl;
    private MemberRole role;
    private boolean isOnline;
    private OffsetDateTime joinedAt;
}
