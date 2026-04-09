package com.example.chatWeb.controller;

import com.example.chatWeb.dto.response.ApiResponse;
import com.example.chatWeb.dto.response.MyProfileResponse;
import com.example.chatWeb.dto.response.UserResponse;
import com.example.chatWeb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<MyProfileResponse> getMyProfile() {
        return ApiResponse.<MyProfileResponse>builder()
                .data(userService.getMyProfile())
                .build();
    }

    @GetMapping("/user/{id}")
    public ApiResponse<UserResponse> getUserProfile(@PathVariable Long id) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.getProfileByUserId(id))
                .build();
    }

    @PatchMapping("/status")
    public ApiResponse<Void> updateStatus(@RequestBody Map<String, String> request) {
        userService.updateMyStatus(request.get("status"));
        return ApiResponse.<Void>builder()
                .message("Updating status of user")
                .build();
    }
}
