package org.example.chatrealtimeservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.example.chatrealtimeservice.models.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
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
    public void handleIncomingGroupMessage(@Valid ChatMessage chatMessage,  SimpMessageHeaderAccessor headerAccessor) {
        try {
            // TODO: Можно добавить авторизацию (проверить, может ли senderId писать в conversationId)

            // Сериализуем в JSON
            String payload = objectMapper.writeValueAsString(chatMessage);

            // Отправляем в Kafka-топик "messages-incoming"
            kafkaTemplate.send(TOPIC_INCOMING, payload);

            // Исключение отправителя из рассылки
            String senderSessionId = headerAccessor.getSessionId();
            String destination = "/topic/conversation." + chatMessage.getConversationId();

            // Рассылка сообщения всем подписчикам
            simpMessagingTemplate.convertAndSend(destination, chatMessage, headers -> {
                if (!senderSessionId.equals(headers.getHeaders().get("simpSessionId"))) {
                    // Только для других сессий
                    return headers;
                }
                return null; // Игнорируем отправителя
            });

        } catch (Exception e) {
            e.printStackTrace();
            // Можно логировать, отправлять ответ об ошибке и т.д.
        }
    }

    public void handleIncomingPrivateMessage(@Valid ChatMessage chatMessage,  SimpMessageHeaderAccessor headerAccessor) {
        try {
            // Получаем ID сессии отправителя
            String senderSessionId = headerAccessor.getSessionId();
            String destination = "/topic/conversation." + chatMessage.getConversationId();

            // Отправляем сообщение в Kafka
            String payload = objectMapper.writeValueAsString(chatMessage);
            kafkaTemplate.send(TOPIC_INCOMING, payload);

            // Рассылаем сообщение всем клиентам
            simpMessagingTemplate.convertAndSend(destination, chatMessage);

            // (Опционально) дополнительно отправляем сообщение только отправителю с подтверждением
            simpMessagingTemplate.convertAndSendToUser(senderSessionId, "/queue/confirmation", chatMessage);

        } catch (Exception e) {
            e.printStackTrace();
            // Логируем ошибку
        }

    }

    /** TODO реализовать consumer для подтверждения доставки
     * Этот метод вызывается, когда получаем событие "messages-saved" из Kafka (см. KafkaMessageListener).
     * Здесь мы можем уведомить клиенты, что сообщение успешно сохранено и "доставлено".
     *
     */
    public void handleMessageSaved(SavedMessageEvent savedMessageEvent) {
        // Пример: отправляем всем подписчикам /topic/conversation.{conversationId}
        // событие, что сообщение messageId = savedMessageEvent.messageId теперь "официально" в БД.

        String destination = "/topic/conversation." + savedMessageEvent.conversationId;
        simpMessagingTemplate.convertAndSend(destination, savedMessageEvent);

        // В клиентском коде можно обработать это событие: обновить статус сообщения на "Доставлено".
    }

    /**
     * Вспомогательный класс для десериализации события "messages-saved" из Kafka.
     */
    public static class SavedMessageEvent {
        public String messageId;
        public String conversationId;
        // Можно добавить timestamp, senderId, любые нужные поля
    }
}
