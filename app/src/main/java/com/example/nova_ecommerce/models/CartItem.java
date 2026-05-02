package com.example.nova_ecommerce.models;

public class CartItem {

    private String docId;       // SQLite row id
    private String productId;
    private String categoryId;  // ← new
    private String name;
    private double price;
    private String imageUrl;
    private int    quantity;

    // ── Empty constructor (needed for getAllItems()) ───────────
    public CartItem() {}

    // ── Full constructor ──────────────────────────────────────
    public CartItem(String productId, String name,
                    double price, String imageUrl, int quantity) {
        this.productId = productId;
        this.name      = name;
        this.price     = price;
        this.imageUrl  = imageUrl;
        this.quantity  = quantity;
    }

    // ── Getters ───────────────────────────────────────────────
    public String getDocId()      { return docId; }
    public String getProductId()  { return productId; }
    public String getCategoryId() { return categoryId; }  // ← new
    public String getName()       { return name; }
    public double getPrice()      { return price; }
    public String getImageUrl()   { return imageUrl; }
    public int    getQuantity()   { return quantity; }

    public String getFormattedPrice() {
        return "Rs. " + (int)(price * quantity);
    }

    // ── Setters ───────────────────────────────────────────────
    public void setDocId(String docId)           { this.docId = docId; }
    public void setProductId(String productId)   { this.productId = productId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; } // ← new
    public void setName(String name)             { this.name = name; }
    public void setPrice(double price)           { this.price = price; }
    public void setImageUrl(String imageUrl)     { this.imageUrl = imageUrl; }
    public void setQuantity(int quantity)        { this.quantity = quantity; }
}