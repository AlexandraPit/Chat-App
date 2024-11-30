package com.example.chatapp;

public class UserModel {
    String userId;
    String userName;
    String userEmail;
    String userPassword;

    public UserModel(String userName, String userId, String userEmail, String userPassword) {
        this.userName = userName;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    public UserModel() {
    }

    public String getUserPassword() {
        return userPassword;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }


}
