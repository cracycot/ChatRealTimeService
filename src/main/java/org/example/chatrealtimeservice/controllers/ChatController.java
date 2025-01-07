package org.example.chatrealtimeservice.controllers;

import jakarta.validation.Valid;
import org.example.chatrealtimeservice.models.ChatMessage;
import org.example.chatrealtimeservice.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Valid ChatMessage chatMessage) {
        // Вызвать бизнес-логику сервиса
        System.out.println(2);
        chatService.handleIncomingMessage(chatMessage);
    }
}
