package com.example.nova_ecommerce.user.models;

import com.google.firebase.database.PropertyName;

import java.util.List;

public class Order {
    private String orderId;
    private String status;
    private String timestamp;
    private String paymentMethod;
    private double totalAmount;
    private String address;
    private String city;
    private List<CartItem> items;

    public Order() {
        // Required for Firebase
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @PropertyName("timestamp")
    public String getTimestamp() { return timestamp; }
    @PropertyName("timestamp")
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    @PropertyName("paymentMethod")
    public String getPaymentMethod() { return paymentMethod; }
    @PropertyName("paymentMethod")
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    @PropertyName("totalAmount")
    public double getTotalAmount() { return totalAmount; }
    @PropertyName("totalAmount")
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
}
