package com.example.chatWeb.controller;

import com.example.chatWeb.dto.request.*;
import com.example.chatWeb.service.WebSocketChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final WebSocketChatService webSocketChatService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(WebSocketMessageRequest messageRequest, Principal principal) {
        webSocketChatService.sendMessage(messageRequest, principal.getName());
    }

    @MessageMapping("/chat.seen")
    public void seenMessage(WebSocketSeenRequest request, Principal principal) {
        webSocketChatService.seenMessage(request, principal.getName());
    }

    @MessageMapping("/chat.editMessage")
    public void editMessage(WebSocketEditMessageRequest request, Principal principal) {
        webSocketChatService.editMessage(request, principal.getName());
    }

    @MessageMapping("/chat.recallMessage")
    public void recallMessage(WebSocketRecallRequest recallRequest, Principal principal) {
        webSocketChatService.recallMessage(recallRequest, principal.getName());
    }

    @MessageMapping("/chat.typing")
    public void typing(WebSocketTypingRequest typingRequest, Principal principal) {
        webSocketChatService.handleTypingStatus(typingRequest, principal.getName());
    }
}
