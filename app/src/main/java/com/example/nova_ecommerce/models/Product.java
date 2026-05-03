package com.example.nova_ecommerce.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private String imageURL;
    private String categoryId;
    private String categoryName;
    private boolean isFeatured;
    private double rating;
    private int reviewCount;
    private int stock;
    private boolean isFavorite;   // local only — never goes to Firebase

    public Product() {}

    // ── Getters ───────────────────────────────────────────────
    @Exclude
    public String getId()           { return id; }

    public String getName()         { return name; }
    public String getDescription()  { return description; }
    public double getPrice()        { return price; }
    public String getImageURL()     { return imageURL; }

    @Exclude
    public String getCategoryId()   { return categoryId; }

    @Exclude
    public String getCategoryName() { return categoryName; }

    @PropertyName("isFeatured")
    public boolean isFeatured()     { return isFeatured; }

    public double getRating()       { return rating; }
    public int getReviewCount()     { return reviewCount; }
    public int getStock()           { return stock; }

    @Exclude
    public boolean isFavorite()     { return isFavorite; }

    @Exclude
    public String getCategory()     { return categoryName; }

    public String getFormattedPrice() {
        return "Rs. " + (int) price;
    }

    // ── Setters ───────────────────────────────────────────────
    @Exclude
    public void setId(String id)                    { this.id = id; }

    public void setName(String name)                { this.name = name; }
    public void setDescription(String d)            { this.description = d; }
    public void setPrice(double price)              { this.price = price; }
    public void setImageURL(String imageURL)        { this.imageURL = imageURL; }

    @Exclude
    public void setCategoryId(String categoryId)    { this.categoryId = categoryId; }

    @Exclude
    public void setCategoryName(String n)           { this.categoryName = n; }

    @PropertyName("isFeatured")
    public void setFeatured(boolean featured)       { isFeatured = featured; }

    public void setRating(double rating)            { this.rating = rating; }
    public void setReviewCount(int reviewCount)     { this.reviewCount = reviewCount; }
    public void setStock(int stock)                 { this.stock = stock; }

    @Exclude
    public void setFavorite(boolean favorite)       { isFavorite = favorite; }
}