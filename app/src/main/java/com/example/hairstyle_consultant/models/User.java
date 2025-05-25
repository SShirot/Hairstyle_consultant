package com.example.hairstyle_consultant.models;

public class User {
    private String userId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String hairStyle;
    private String hairQuality;
    private String hairLength;
    private String hairColor;
    private String hairTexture;
    private String hairConcerns;

    // Default constructor required for Firebase
    public User() {
    }

    public User(String userId, String email, String fullName, String phoneNumber) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getHairStyle() {
        return hairStyle;
    }

    public void setHairStyle(String hairStyle) {
        this.hairStyle = hairStyle;
    }

    public String getHairQuality() {
        return hairQuality;
    }

    public void setHairQuality(String hairQuality) {
        this.hairQuality = hairQuality;
    }

    public String getHairLength() {
        return hairLength;
    }

    public void setHairLength(String hairLength) {
        this.hairLength = hairLength;
    }

    public String getHairColor() {
        return hairColor;
    }

    public void setHairColor(String hairColor) {
        this.hairColor = hairColor;
    }

    public String getHairTexture() {
        return hairTexture;
    }

    public void setHairTexture(String hairTexture) {
        this.hairTexture = hairTexture;
    }

    public String getHairConcerns() {
        return hairConcerns;
    }

    public void setHairConcerns(String hairConcerns) {
        this.hairConcerns = hairConcerns;
    }
} 