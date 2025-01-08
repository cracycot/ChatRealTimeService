package org.example.chatrealtimeservice.controllers;

import jakarta.validation.Valid;
import org.example.chatrealtimeservice.models.ChatMessage;
import org.example.chatrealtimeservice.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    // @DestinationVariable - для получения аргументов из "пути"
    @MessageMapping("/chat/group")
    public void sendGroupMessage(@Valid ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        chatService.handleIncomingGroupMessage(chatMessage, headerAccessor);
    }

    @MessageMapping("/chat/private")
    public void sendPrivateMessage(@Valid ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        chatService.handleIncomingPrivateMessage(chatMessage, headerAccessor);
    }
}
