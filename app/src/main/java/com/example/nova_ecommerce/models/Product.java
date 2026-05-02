package com.example.nova_ecommerce.models;
import com.google.firebase.database.PropertyName;

public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private String imageURL;
    private String categoryId;    // e.g. "cat1"
    private String categoryName;  // e.g. "Electronics"
    private boolean isFeatured;
    private double rating;
    private int reviewCount;
    private int stock;
    private boolean isFavorite;   // local only, not in DB

    public Product() {}

    // ── Getters ───────────────────────────────────────────────
    public String getId()           { return id; }
    public String getName()         { return name; }
    public String getDescription()  { return description; }
    public double getPrice()        { return price; }
    public String getImageURL()     { return imageURL; }
    public String getCategoryId()   { return categoryId; }
    public String getCategoryName() { return categoryName; }
    @PropertyName("isFeatured")
    public boolean isFeatured()     { return isFeatured; }
    public double getRating()       { return rating; }
    public int getReviewCount()     { return reviewCount; }
    public int getStock()           { return stock; }
    public boolean isFavorite()     { return isFavorite; }

    // Legacy getter so old code calling getCategory() still compiles
    public String getCategory()     { return categoryName; }

    public String getFormattedPrice() {
        return "Rs. " + (int) price;
    }

    // ── Setters ───────────────────────────────────────────────
    public void setId(String id)                   { this.id = id; }
    public void setName(String name)               { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price)             { this.price = price; }
    public void setImageURL(String imageURL)       { this.imageURL = imageURL; }
    public void setCategoryId(String categoryId)   { this.categoryId = categoryId; }
    public void setCategoryName(String categoryName){ this.categoryName = categoryName; }
    @PropertyName("isFeatured")
    public void setFeatured(boolean featured)      { isFeatured = featured; }
    public void setRating(double rating)           { this.rating = rating; }
    public void setReviewCount(int reviewCount)    { this.reviewCount = reviewCount; }
    public void setStock(int stock)                { this.stock = stock; }
    public void setFavorite(boolean favorite)      { isFavorite = favorite; }
}