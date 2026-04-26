package com.example.nova_ecommerce.models;

public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private String category;
    private String imageURL;
    private double rating;
    private int reviewCount;
    private boolean isFeatured;
    private boolean isFavorite;

    public Product() {}

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public String getImageURL() { return imageURL; }
    public double getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }
    public boolean isFeatured() { return isFeatured; }
    public boolean isFavorite() { return isFavorite; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setCategory(String category) { this.category = category; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
    public void setRating(double rating) { this.rating = rating; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public void setFeatured(boolean featured) { isFeatured = featured; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public String getFormattedPrice() {
        return "Rs. " + String.format("%,.0f", price);
    }
}