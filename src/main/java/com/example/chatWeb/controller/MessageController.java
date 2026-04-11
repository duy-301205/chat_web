package com.example.chatWeb.controller;

import com.example.chatWeb.dto.request.MessageRequest;
import com.example.chatWeb.dto.response.ApiResponse;
import com.example.chatWeb.dto.response.MessageResponse;
import com.example.chatWeb.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ApiResponse<MessageResponse> sendMessage(MessageRequest message) {
        return ApiResponse.<MessageResponse>builder()
                .data(messageService.sendMessage(message))
                .build();
    }

    @GetMapping("/conversation/{conversationId}")
    public ApiResponse<Page<MessageResponse>> getMessages(@PathVariable Long conversationId,
                                                          @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<Page<MessageResponse>>builder()
                .data(messageService.getMessages(conversationId, pageable))
                .build();
    }

    @PostMapping("/{messageId}/recall")
    public ApiResponse<Void> recallMessage(@PathVariable Long messageId) {
        messageService.recallMessage(messageId);
        return ApiResponse.<Void>builder()
                .message("Tin nhắn đã được thu hồi")
                .build();
    }
}
