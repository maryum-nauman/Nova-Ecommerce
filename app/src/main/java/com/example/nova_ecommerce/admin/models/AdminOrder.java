package com.example.nova_ecommerce.admin.models;

import java.util.List;
import java.util.Map;

public class AdminOrder {

    private String orderId;
    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String postalCode;
    private String paymentMethod;
    private String status;
    private String timestamp;
    private double totalAmount;
    private List<Map<String, Object>> items;

    public AdminOrder() {}

    public String getOrderId()       { return orderId; }
    public String getUserId()        { return userId; }
    public String getFullName()      { return fullName; }
    public String getEmail()         { return email; }
    public String getPhone()         { return phone; }
    public String getAddress()       { return address; }
    public String getCity()          { return city; }
    public String getPostalCode()    { return postalCode; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus()        { return status; }
    public String getTimestamp()     { return timestamp; }
    public double getTotalAmount()   { return totalAmount; }
    public List<Map<String, Object>> getItems() { return items; }
    public void setOrderId(String orderId)         { this.orderId = orderId; }
    public void setUserId(String userId)           { this.userId = userId; }
    public void setFullName(String fullName)       { this.fullName = fullName; }
    public void setEmail(String email)             { this.email = email; }
    public void setPhone(String phone)             { this.phone = phone; }
    public void setAddress(String address)         { this.address = address; }
    public void setCity(String city)               { this.city = city; }
    public void setPostalCode(String postalCode)   { this.postalCode = postalCode; }
    public void setPaymentMethod(String p)         { this.paymentMethod = p; }
    public void setStatus(String status)           { this.status = status; }
    public void setTimestamp(String timestamp)     { this.timestamp = timestamp; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setItems(List<Map<String, Object>> items) { this.items = items; }
}