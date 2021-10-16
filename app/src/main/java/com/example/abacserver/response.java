package com.example.abacserver;

public class response {
    private long timestamp;
    private String message;
    private Entity entity;

    public response(long timestamp, String message, Entity entity) {
        this.timestamp = timestamp;
        this.message = message;
        this.entity = entity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
