package com.example.chatWeb.controller;

import com.example.chatWeb.dto.request.WebSocketMessageRequest;
import com.example.chatWeb.dto.request.WebSocketSeenRequest;
import com.example.chatWeb.service.WebSocketChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final WebSocketChatService webSocketChatService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(WebSocketMessageRequest messageRequest) {
        webSocketChatService.sendMessage(messageRequest);
    }

    @MessageMapping("/chat.seen")
    public void seenMessage(WebSocketSeenRequest request) {
        webSocketChatService.seenMessage(request);
    }
}
