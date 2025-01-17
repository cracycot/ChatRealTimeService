package org.example.chatrealtimeservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.chatrealtimeservice.models.DeliveryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeliveryNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryNotificationService.class);

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @KafkaListener(topics = "messages-delivery", groupId = "chat-group")
    public void onMessageDelivery(String message) {
        try {
            logger.info("доставлено сообщение" + message);
            // Десериализация уведомления
            DeliveryStatus deliveryStatus = new ObjectMapper().readValue(message, DeliveryStatus.class);
            logger.info("сообщение десериализованно" + deliveryStatus);
            // Отправляем уведомление пользователю
            String destination = "/queue/delivery." + deliveryStatus.getConversationId();
            String user = deliveryStatus.getSenderId();
            simpMessagingTemplate.convertAndSendToUser(user, destination, deliveryStatus);
            logger.info("отправлено в диалог");

        } catch (Exception e) {
            logger.error("Ошибка при обработке сообщения: {}", message, e);
        }
    }
}
