package com.example.nova_ecommerce.models;

public class Category {
    private String id;       // catId key from Firebase e.g. "cat1"
    private String name;
    private String imageURL;

    public Category() {}

    public Category(String id, String name, String imageURL) {
        this.id       = id;
        this.name     = name;
        this.imageURL = imageURL;
    }

    public String getId()       { return id; }
    public String getName()     { return name; }
    public String getImageURL() { return imageURL; }

    public void setId(String id)             { this.id = id; }
    public void setName(String name)         { this.name = name; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
}