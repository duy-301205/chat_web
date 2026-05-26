package com.example.chatWeb.controller;

import com.example.chatWeb.dto.response.ApiResponse;
import com.example.chatWeb.dto.response.FriendResponse;
import com.example.chatWeb.service.FriendShipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friendships")
@RequiredArgsConstructor
public class FriendShipController {

    private final FriendShipService friendShipService;

    @PostMapping("/request/{targetUserId}")
    public ApiResponse<String> sendFriendRequest(@PathVariable Long targetUserId) {
        friendShipService.sendFriendRequest(targetUserId);
        return ApiResponse.<String>builder()
                .data("Đã gửi lời mời kết bạn thành công. ")
                .build();
    }

    @PostMapping("/accept/{targetUserId}")
    public ApiResponse<String> acceptFriendRequest(@PathVariable Long targetUserId) {
        friendShipService.acceptFriendRequest(targetUserId);
        return ApiResponse.<String>builder()
                .data("Đã đồng ý kết bạn. ")
                .build();
    }

    @DeleteMapping("/remove/{targetUserId}")
    public ApiResponse<String> removeFriend(@PathVariable Long targetUserId) {
        friendShipService.removeFriend(targetUserId);
        return ApiResponse.<String>builder()
                .data("Đã xóa quan hệ bạn bè. ")
                .build();
    }

    @PostMapping("/block/{targetUserId}")
    public ApiResponse<String> blockUser(@PathVariable Long targetUserId) {
        friendShipService.blockUser(targetUserId);
        return ApiResponse.<String>builder()
                .data("Đã chặn người dùng này. ")
                .build();
    }
    @GetMapping("/friends")
    public ApiResponse<List<FriendResponse>> getMyFriends() {
        List<FriendResponse> friends = friendShipService.getMyAcceptedFriends();
        return ApiResponse.<List<FriendResponse>>builder()
                .data(friends)
                .build();
    }

}
