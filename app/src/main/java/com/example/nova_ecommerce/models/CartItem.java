package com.example.nova_ecommerce.models;

import com.google.firebase.database.PropertyName;

public class CartItem {

    private String docId;
    private String productId;
    private String categoryId;
    private String name;
    private double price;
    private String imageUrl;
    private int    quantity;

    public CartItem() {}

    public CartItem(String productId, String name,
                    double price, String imageUrl, int quantity) {
        this.productId = productId;
        this.name      = name;
        this.price     = price;
        this.imageUrl  = imageUrl;
        this.quantity  = quantity;
    }

    public String getDocId()      { return docId; }
    public String getProductId()  { return productId; }
    public String getCategoryId() { return categoryId; }
    public String getName()       { return name; }
    public double getPrice()      { return price; }

    @PropertyName("imageURL")
    public String getImageUrl()   { return imageUrl; }
    @PropertyName("imageURL")
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int    getQuantity()   { return quantity; }

    public String getFormattedPrice() {
        return "Rs. " + (int)(price * quantity);
    }

    public void setDocId(String docId)           { this.docId = docId; }
    public void setProductId(String productId)   { this.productId = productId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public void setName(String name)             { this.name = name; }
    public void setPrice(double price)           { this.price = price; }
    public void setQuantity(int quantity)        { this.quantity = quantity; }
}