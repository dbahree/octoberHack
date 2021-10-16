package com.example.abacserver;

public class Entity {
    private String message;
    private String from;
    private String to;
    private String timestamp;

    public Entity(String message, String from, String to, String timestamp) {
        this.message = message;
        this.from = from;
        this.to = to;
        this.timestamp = timestamp;
    }
}
