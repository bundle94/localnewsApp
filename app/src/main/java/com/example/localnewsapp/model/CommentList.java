package com.example.localnewsapp.model;

public class CommentList {

    private String name, comment;
    private boolean anonymous;

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public boolean isAnonymous(){
        return anonymous;
    }

    public CommentList(String name, String comment, boolean anonymous) {
        this.name = name;
        this.comment = comment;
        this.anonymous = anonymous;
    }
}
