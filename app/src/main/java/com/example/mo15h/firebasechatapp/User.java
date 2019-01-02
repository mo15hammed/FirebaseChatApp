package com.example.mo15h.firebasechatapp;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class User implements Serializable {
    private String deviceToken;
    private String name;
    private String email;
    private String image;
    private String thumbImage;
    private String status;
    private String online;
    private String uid;
    private String date;
    private String password;

    public User() {
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public User(String deviceToken, String name, String email, String password, String image, String thumbImage, String status) {
        this.deviceToken = deviceToken;
        this.name = name;
        this.email = email;
        this.password = password;
        this.image = image;
        this.thumbImage = thumbImage;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        if (image == null)
            return "default";
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumbImage() {
        if (thumbImage == null)
            return "default";
        return thumbImage;
    }

    public void setThumbImage(String thumbImage) {
        this.thumbImage = thumbImage;
    }

    public String getStatus() {
        if (status == null)
            return "Hi there, I am using Awesome Chat.";
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Exclude
    public String getOnline() {
        return online;
    }

    @Exclude
    public void setOnline(String online) {
        this.online = online;
    }

    @Exclude
    public String getUid() {
        return uid;
    }

    @Exclude
    public void setUid(String uid) {
        this.uid = uid;
    }

    @Exclude
    public String getDate() {
        return date;
    }

    @Exclude
    public void setDate(String date) {
        this.date = date;
    }

    @Exclude
    public String getPassword() {
        return password;
    }

    @Exclude
    public void setPassword(String password) {
        this.password = password;
    }


    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }


    @Override
    public String toString() {
        return "User{" +
                "deviceToken='" + deviceToken + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", image='" + image + '\'' +
                ", thumbImage='" + thumbImage + '\'' +
                ", status='" + status + '\'' +
                ", online='" + online + '\'' +
                ", uid='" + uid + '\'' +
                ", date='" + date + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
