package com.example.chatWeb.controller;

import com.example.chatWeb.dto.request.EditMessageRequest;
import com.example.chatWeb.dto.request.MessageRequest;
import com.example.chatWeb.dto.request.SeenMessageRequest;
import com.example.chatWeb.dto.response.ApiResponse;
import com.example.chatWeb.dto.response.MessageResponse;
import com.example.chatWeb.dto.response.SeenMessageResponse;
import com.example.chatWeb.exception.AppException;
import com.example.chatWeb.exception.ErrorCode;
import com.example.chatWeb.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.List;


@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MessageResponse> sendMessage(
            @RequestPart("data") String data,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        try {
            MessageRequest message = objectMapper.readValue(data, MessageRequest.class);

            return ApiResponse.<MessageResponse>builder()
                    .data(messageService.sendMessage(message, files))
                    .build();

        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
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

    @PutMapping("/edit")
    public ApiResponse<MessageResponse> editMessage(@RequestBody EditMessageRequest request) {
        return ApiResponse.<MessageResponse>builder()
                .data(messageService.editMessage(request))
                .build();
    }

    @PutMapping("/seen")
    public ApiResponse<SeenMessageResponse> seenMessage(@RequestBody SeenMessageRequest request) {
        return ApiResponse.<SeenMessageResponse>builder()
                .message("Seen message successfully")
                .data(messageService.seenMessage(request))
                .build();
    }
}
