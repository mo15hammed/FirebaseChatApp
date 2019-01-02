package com.example.mo15h.firebasechatapp;

public class Friend {
    private String friend_date;
    private int request_type;

    public Friend(String friend_date, int request_type) {
        this.friend_date = friend_date;
        this.request_type = request_type;
    }

    public Friend() {
    }

    public String getFriend_date() {
        return friend_date;
    }

    public void setFriend_date(String friend_date) {
        this.friend_date = friend_date;
    }

    public int getRequest_type() {
        return request_type;
    }

    public void setRequest_type(int request_type) {
        this.request_type = request_type;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "friend_date='" + friend_date + '\'' +
                ", request_type=" + request_type +
                '}';
    }
}
