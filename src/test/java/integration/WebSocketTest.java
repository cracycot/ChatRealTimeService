package integration;

import org.example.chatrealtimeservice.models.ChatMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = org.example.chatrealtimeservice.ChatRealTimeServiceApplication.class
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WebSocketTest {

    @LocalServerPort
    private int port;

    @Value("${websocket.endpoint:/ws-chat}")
    private String websocketEndpoint;

    @Test
    public void testRealTimeMessaging() throws Exception {
        String url = "ws://localhost:" + port + websocketEndpoint;

        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        BlockingQueue<ChatMessage> messageQueue = new LinkedBlockingQueue<>();

        StompSession session = stompClient.connectAsync(url, new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);

        // Подписываемся на топик
        session.subscribe("/topic/conversation.123", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageQueue.add((ChatMessage) payload);
            }
        });

        // Отправляем сообщение
        ChatMessage chatMessage = new ChatMessage(
                "temp-12345",
                "123",
                "tester",
                "Hello, WebSocket!",
                System.currentTimeMillis()
        );
        session.send("/app/chat/group", chatMessage);

        // Ожидаем сообщение
        ChatMessage receivedMessage = messageQueue.poll(2, TimeUnit.SECONDS);

        // Проверяем, что сообщение доставлено
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getContent()).isEqualTo(chatMessage.getContent());
        assertThat(receivedMessage.getSenderId()).isEqualTo(chatMessage.getSenderId());
        assertThat(receivedMessage.getConversationId()).isEqualTo(chatMessage.getConversationId());
    }
}