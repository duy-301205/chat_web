package com.example.chatWeb.service;

import com.example.chatWeb.dto.request.*;
import com.example.chatWeb.dto.response.MessageResponse;
import com.example.chatWeb.dto.response.SeenMessageResponse;
import com.example.chatWeb.entity.User;
import com.example.chatWeb.exception.AppException;
import com.example.chatWeb.exception.ErrorCode;
import com.example.chatWeb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WebSocketChatService {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ConversationService conversationService;

    public void sendMessage(WebSocketMessageRequest request,String email) {

        MessageRequest messageRequest = new MessageRequest();

        messageRequest.setConversationId(request.getConversationId());
        messageRequest.setContent(request.getContent());
        messageRequest.setType(request.getType());
        messageRequest.setReplyToId(request.getReplyToId());

        MessageResponse response = messageService.sendMessage(messageRequest,email, List.of());

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + request.getConversationId(),
                response);

        try {
            List<Long> memberIds = conversationService.getMemberUserIdsByConversationId(request.getConversationId());

            if(memberIds != null) {
                for(Long userId : memberIds) {
                    String destination = "/topic/user/" + userId + "/sidebar";

                    messagingTemplate.convertAndSend(destination, response);
                }
                
            }
        } catch (Exception e) {
            System.err.println("Lỗi luồng đẩy tin nhắn lên Sidebar: " + e.getMessage());
        }
    }

    public void seenMessage(WebSocketSeenRequest request, String email) {
        SeenMessageRequest seenMessageRequest = new SeenMessageRequest();
        seenMessageRequest.setConversationId(request.getConversationId());
        seenMessageRequest.setMessageId(request.getMessageId());

        SeenMessageResponse response = messageService.seenMessage(seenMessageRequest, email);

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + request.getConversationId() + "/seen",
                response
        );
    }

    public void editMessage(WebSocketEditMessageRequest request, String email) {
        EditMessageRequest editMessageRequest = new EditMessageRequest();
        editMessageRequest.setMessageId(request.getMessageId());
        editMessageRequest.setContent(request.getContent());

        MessageResponse response = messageService.editMessage(editMessageRequest, email);

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + request.getConversationId(),
                response
        );
    }

    public void recallMessage(WebSocketRecallRequest request, String email) {
        messageService.recallMessage(request.getMessageId(), email);

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + request.getConversationId(),
                request
        );
    }

    public void handleTypingStatus(WebSocketTypingRequest request, String email) {

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        request.setUserId(currentUser.getId());
        request.setUsername(currentUser.getActualUsername());

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + request.getConversationId() + "/typing",
                request
        );
    }
 }
