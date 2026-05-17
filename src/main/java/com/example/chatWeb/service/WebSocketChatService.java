package com.example.chatWeb.service;

import com.example.chatWeb.dto.request.MessageRequest;
import com.example.chatWeb.dto.request.SeenMessageRequest;
import com.example.chatWeb.dto.request.WebSocketMessageRequest;
import com.example.chatWeb.dto.request.WebSocketSeenRequest;
import com.example.chatWeb.dto.response.MessageResponse;
import com.example.chatWeb.dto.response.SeenMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WebSocketChatService {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessage(WebSocketMessageRequest request) {

        MessageRequest messageRequest = new MessageRequest();

        messageRequest.setConversationId(request.getConversationId());
        messageRequest.setContent(request.getContent());
        messageRequest.setType(request.getType());
        messageRequest.setReplyToId(request.getReplyToId());

        MessageResponse response = messageService.sendMessage(messageRequest, List.of());

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + request.getConversationId(),
                response);
    }

    public void seenMessage(WebSocketSeenRequest request) {
        SeenMessageRequest seenMessageRequest = new SeenMessageRequest();
        seenMessageRequest.setConversationId(request.getConversationId());
        seenMessageRequest.setMessageId(request.getMessageId());

        SeenMessageResponse response = messageService.seenMessage(seenMessageRequest);

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + request.getConversationId() + "/seen",
                response
        );
    }
}
