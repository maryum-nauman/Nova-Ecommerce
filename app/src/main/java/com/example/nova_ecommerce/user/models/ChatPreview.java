package com.example.nova_ecommerce.user.models;

public class ChatPreview {
    private String chatKey;      // "{userId}_{adminId}"
    private String userId;
    private String userName;
    private String userEmail;
    private String lastMessage;
    private long   lastTimestamp;
    private int    unreadAdmin;

    public ChatPreview() {}

    public String getChatKey()       { return chatKey; }
    public String getUserId()        { return userId; }
    public String getUserName()      { return userName; }
    public String getUserEmail()     { return userEmail; }
    public String getLastMessage()   { return lastMessage; }
    public long   getLastTimestamp() { return lastTimestamp; }
    public int    getUnreadAdmin()   { return unreadAdmin; }

    public void setChatKey(String chatKey)           { this.chatKey = chatKey; }
    public void setUserId(String userId)             { this.userId = userId; }
    public void setUserName(String userName)         { this.userName = userName; }
    public void setUserEmail(String userEmail)       { this.userEmail = userEmail; }
    public void setLastMessage(String lastMessage)   { this.lastMessage = lastMessage; }
    public void setLastTimestamp(long lastTimestamp) { this.lastTimestamp = lastTimestamp; }
    public void setUnreadAdmin(int unreadAdmin)      { this.unreadAdmin = unreadAdmin; }
}