package com.example.mo15h.firebasechatapp;

public class Message {
    private String message, type, from;
    private long timestamp;
    private boolean seen;

    public Message() {
    }

    public Message(String message, String type, String from, long timestamp, boolean seen) {
        this.message = message;
        this.type = type;
        this.from = from;
        this.timestamp = timestamp;
        this.seen = seen;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", type='" + type + '\'' +
                ", from='" + from + '\'' +
                ", timestamp=" + timestamp +
                ", seen=" + seen +
                '}';
    }
}
