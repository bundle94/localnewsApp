package com.example.localnewsapp.model;

public class News {
    private int id;
    private String title , description , status, image;
    private CreatedBy createdBy;

    public News (int id, String title, String description, String status, String image, CreatedBy createdBy) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.image = image;
        this.createdBy = createdBy;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getImage() {
        return image;
    }

    public CreatedBy getCreatedBy() {
        return createdBy;
    }
}
