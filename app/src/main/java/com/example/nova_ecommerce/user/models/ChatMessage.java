package com.example.nova_ecommerce.user.models;

import com.google.firebase.database.PropertyName;

public class ChatMessage {
    private String  messageId;
    private String  senderId;
    private String  senderName;
    private String  message;
    private long    timestamp;
    private boolean isAdmin;

    public ChatMessage() {}

    public ChatMessage(String senderId, String senderName,
                       String message, long timestamp,
                       boolean isAdmin) {
        this.senderId   = senderId;
        this.senderName = senderName;
        this.message    = message;
        this.timestamp  = timestamp;
        this.isAdmin    = isAdmin;
    }

    public String  getMessageId()  { return messageId; }
    public String  getSenderId()   { return senderId; }
    public String  getSenderName() { return senderName; }
    public String  getMessage()    { return message; }
    public long    getTimestamp()  { return timestamp; }
    @PropertyName("isAdmin")
    public boolean isAdmin()       { return isAdmin; }

    public void setMessageId(String messageId)   { this.messageId = messageId; }
    public void setSenderId(String senderId)     { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setMessage(String message)       { this.message = message; }
    public void setTimestamp(long timestamp)     { this.timestamp = timestamp; }
    @PropertyName("isAdmin")
    public void setAdmin(boolean admin)          { isAdmin = admin; }
}