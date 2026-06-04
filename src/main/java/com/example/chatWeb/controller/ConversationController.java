package com.example.chatWeb.controller;

import com.example.chatWeb.dto.request.AddGroupMembersRequest;
import com.example.chatWeb.dto.request.CreateConversationRequest;
import com.example.chatWeb.dto.request.RemoveMembersRequest;
import com.example.chatWeb.dto.request.UpdateNicknameRequest;
import com.example.chatWeb.dto.response.ApiResponse;
import com.example.chatWeb.dto.response.ConversationResponse;
import com.example.chatWeb.dto.response.MemberResponse;
import com.example.chatWeb.service.ConversationService;
import com.example.chatWeb.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final MemberService memberService;

    @GetMapping
    public ApiResponse<List<ConversationResponse>> getMyConversations(){
        return ApiResponse.<List<ConversationResponse>>builder()
                .data(conversationService.getMyConversations())
                .build();
    }
    @PostMapping
    public ApiResponse<ConversationResponse> createConversation(@RequestBody CreateConversationRequest request) {
        return ApiResponse.<ConversationResponse>builder()
                .data(conversationService.createConversation(request))
                .build();
    }

    @GetMapping("/{id}/members")
    public ApiResponse<List<MemberResponse>> getMembers(@PathVariable Long id) {
        return ApiResponse.<List<MemberResponse>>builder()
                .data(conversationService.getConversationMembers(id))
                .build();
    }

    @PostMapping("/private/{targetUserId}")
    public ApiResponse<Long> findOrCreatePrivateChat(@PathVariable Long targetUserId) {
        Long conversationId = conversationService.findOrCreatePrivateConversation(targetUserId);
        return ApiResponse.<Long>builder()
                .data(conversationId)
                .build();
    }

    @PostMapping("/{id}/members")
    public ApiResponse<Void> addMember(@PathVariable Long id, @RequestBody AddGroupMembersRequest request) {
        conversationService.addMemberToConversation(id, request);
        return ApiResponse.<Void>builder()
                .message("Member added successfully")
                .build();
    }

    @DeleteMapping("{id}/members")
    public ApiResponse<Void> removeMembers(@PathVariable Long id,
                                           @RequestBody RemoveMembersRequest request) {
        conversationService.removeMembersFromGroup(id, request);
        return ApiResponse.<Void>builder()
                .message("Member removed successfully")
                .build();
    }

    @PostMapping("/{id}/leave")
    public ApiResponse<Void> leaveGroup(@PathVariable Long id) {
        conversationService.leaveGroup(id);
        return ApiResponse.<Void>builder()
                .message("You left the conversation")
                .build();
    }

    @PutMapping("/member/nickname")
    public ApiResponse<Void> updateNickname(@RequestBody UpdateNicknameRequest request) {
        memberService.updateNickname(request);
        return ApiResponse.<Void>builder()
                .message("Thay đổi biệt danh thành công!")
                .build();
    }
}
