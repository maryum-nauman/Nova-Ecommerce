package com.example.nova_ecommerce.models;

public class User {
    public String fullName, email, role;

    public User() {} // Required for Firebase

    public User(String fullName, String email, String role) {
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }
}