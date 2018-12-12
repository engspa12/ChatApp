package com.example.dbm.chatapp;

public class ChatMessage {

    private String text;
    private String name;
    private String photoUrl;
    private String currentDateAndTime;

    public ChatMessage() {
    }

    public ChatMessage(String text, String name, String photoUrl, String currentDateAndTime) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.currentDateAndTime = currentDateAndTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getCurrentDateAndTime() {
        return currentDateAndTime;
    }

    public void setCurrentDateAndTime(String dateAndTime) { this.currentDateAndTime = dateAndTime; }
}

