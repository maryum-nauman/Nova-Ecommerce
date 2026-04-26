package com.example.nova_ecommerce.models;

public class CartItem {
    private String docId;      // SQLite row id or Firestore doc id
    private String productId;
    private String name;
    private double price;
    private String imageUrl;
    private int quantity;

    public CartItem() {}

    public CartItem(String productId, String name,
                    double price, String imageUrl, int quantity) {
        this.productId = productId;
        this.name      = name;
        this.price     = price;
        this.imageUrl  = imageUrl;
        this.quantity  = quantity;
    }

    // Getters
    public String getDocId()     { return docId; }
    public String getProductId() { return productId; }
    public String getName()      { return name; }
    public double getPrice()     { return price; }
    public String getImageUrl()  { return imageUrl; }
    public int    getQuantity()  { return quantity; }

    // Setters
    public void setDocId(String docId)         { this.docId = docId; }
    public void setProductId(String productId) { this.productId = productId; }
    public void setName(String name)           { this.name = name; }
    public void setPrice(double price)         { this.price = price; }
    public void setImageUrl(String imageUrl)   { this.imageUrl = imageUrl; }
    public void setQuantity(int quantity)      { this.quantity = quantity; }

    public String getFormattedPrice() {
        return "Rs. " + String.format("%,.0f", price * quantity);
    }
}