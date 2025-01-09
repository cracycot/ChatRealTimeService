package org.example.chatrealtimeservice.models;

public class DeliveryStatus {
    private String messageId;
    private String conversationId;
    private String senderId;
    private String status;

    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DeliveryStatus(String messageId, String conversationId, String status) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.status = status;
    }
}
