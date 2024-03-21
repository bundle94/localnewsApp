package com.example.localnewsapp.model;

public class CreatedBy {
    private int id;
    private String fullName , email, createdAt;

    public CreatedBy(int id, String fullName, String email, String createdAt){
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}


