package org.example.chatrealtimeservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.example.chatrealtimeservice.models.ChatMessage;
import org.example.chatrealtimeservice.models.DeliveryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private static final String TOPIC_INCOMING = "messages-incoming";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Обработка нового сообщения (полученного из ChatController).
     */
    public void handleIncomingGroupMessage(@Valid ChatMessage chatMessage,
                                           SimpMessageHeaderAccessor headerAccessor) {
        try {
            // TODO: Можно добавить авторизацию (проверить, может ли senderId писать в conversationId)
            String destination = "/topic/conversation." + chatMessage.getConversationId();

            // Сериализация сообщения
            String payload = objectMapper.writeValueAsString(chatMessage);
            logger.info("сообщение сериализованно" + payload);

            // Отправляем сообщение в Kafka
            kafkaTemplate.send(TOPIC_INCOMING, payload);
            logger.info("сообщение отправлено в топик" + payload);

            // Рассылаем сообщение всем подписчикам
            simpMessagingTemplate.convertAndSend(destination, chatMessage);
            logger.info("сообщение разослано по webSocket" + payload);

            // Отправляем уведомление о доставке в Kafka
            kafkaTemplate.send("messages-delivery", objectMapper.writeValueAsString(
                    new DeliveryStatus(chatMessage.getMessageId(), chatMessage.getConversationId(), "DELIVERED")
            ));

        } catch (Exception e) {
            logger.error(e.getMessage());
            simpMessagingTemplate.convertAndSendToUser(chatMessage.getSenderId(), "/queue/confirmation",
                    "ERROR: " + e.getMessage());
        }
    }

    public void handleIncomingPrivateMessage(@Valid ChatMessage chatMessage,
                                             SimpMessageHeaderAccessor headerAccessor) {
        try {
            // Получаем ID сессии отправителя
            String senderSessionId = headerAccessor.getSessionId();
            String destination = "/topic/conversation." + chatMessage.getConversationId();

            // Отправляем сообщение в Kafka
            String payload = objectMapper.writeValueAsString(chatMessage);
            kafkaTemplate.send(TOPIC_INCOMING, payload);

            // Рассылаем сообщение всем клиентам
//            simpMessagingTemplate.convertAndSend(destination, chatMessage);
//            logger.info("сообщение разослано по webSocket" + payload);

            simpMessagingTemplate.convertAndSendToUser(chatMessage.getSenderId(), "/queue/confirmation",
                    chatMessage);
            logger.info("сообщение разослано по webSocket в private режиме" + payload);

        } catch (Exception e) {
            logger.error(e.getMessage());
            simpMessagingTemplate.convertAndSendToUser(chatMessage.getSenderId(), "/queue/confirmation",
                    "ERROR: " + e.getMessage());
        }

    }
}
