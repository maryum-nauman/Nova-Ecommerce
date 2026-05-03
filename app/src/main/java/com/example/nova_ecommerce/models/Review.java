package com.example.nova_ecommerce.models;

public class Review {
    private String reviewId;
    private String userId;
    private String userName;
    private String productId;
    private String productName;
    private String categoryId;
    private double rating;
    private String comment;
    private String timestamp;

    public Review() {}

    public String getReviewId()    { return reviewId; }
    public String getUserId()      { return userId; }
    public String getUserName()    { return userName; }
    public String getProductId()   { return productId; }
    public String getProductName() { return productName; }
    public String getCategoryId()  { return categoryId; }
    public double getRating()      { return rating; }
    public String getComment()     { return comment; }
    public String getTimestamp()   { return timestamp; }

    public void setReviewId(String reviewId)       { this.reviewId = reviewId; }
    public void setUserId(String userId)           { this.userId = userId; }
    public void setUserName(String userName)       { this.userName = userName; }
    public void setProductId(String productId)     { this.productId = productId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setCategoryId(String categoryId)   { this.categoryId = categoryId; }
    public void setRating(double rating)           { this.rating = rating; }
    public void setComment(String comment)         { this.comment = comment; }
    public void setTimestamp(String timestamp)     { this.timestamp = timestamp; }
}