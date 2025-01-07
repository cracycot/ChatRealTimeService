package org.example.chatrealtimeservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.example.chatrealtimeservice.models.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
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
    public void handleIncomingMessage(@Valid ChatMessage chatMessage) {
        try {
            // TODO: Можно добавить авторизацию (проверить, может ли senderId писать в conversationId)

            // Сериализуем в JSON
            String payload = objectMapper.writeValueAsString(chatMessage);

            // Отправляем в Kafka-топик "messages-incoming"
            System.out.println(chatMessage);
            kafkaTemplate.send(TOPIC_INCOMING, payload);

            // (Опционально) можно сразу "черновым" образом уведомить всех, что сообщение пришло
            // Но с точки зрения строгой надёжности лучше дождаться "messages-saved"
            // Однако для демонстрации сделаем мгновенную рассылку:
            String destination = "/topic/conversation." + chatMessage.getConversationId();
            simpMessagingTemplate.convertAndSend(destination, chatMessage);

        } catch (Exception e) {
            e.printStackTrace();
            // Можно логировать, отправлять ответ об ошибке и т.д.
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
