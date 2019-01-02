package com.example.mo15h.firebasechatapp;

public class Conv {

    private boolean seen;
    private long timestamp;

    public Conv() {
    }

    public Conv(boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Conv{" +
                "seen=" + seen +
                ", timestamp=" + timestamp +
                '}';
    }
}
