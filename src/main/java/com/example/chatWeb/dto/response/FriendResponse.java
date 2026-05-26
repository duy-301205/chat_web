package com.example.chatWeb.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendResponse {

    private Long id;
    private String username;
    private String nickName;
    private String avatarUrl;
    private boolean online;
}
