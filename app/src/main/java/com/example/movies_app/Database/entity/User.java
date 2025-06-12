package com.example.movies_app.Database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "users",
        indices = {
                @Index(value = {"email"}, unique = true),
                @Index(value = {"username"}, unique = true)
        }
)
public class User {
    @PrimaryKey(autoGenerate = true)
    private int userId;

    @NonNull
    private String email;

    @NonNull
    private String username;

    @NonNull
    private String passwordHash;

    private String fullName;

    private String phoneNumber;

    private String registrationDate;

    private String lastLoginDate;

    private int accountStatus;  // 0: Chưa kích hoạt, 1: Đã kích hoạt, 2: Bị khóa

    private String avatarUrl;

    // Constructor
    public User(@NonNull String email, @NonNull String username,
                @NonNull String passwordHash, String fullName, String phoneNumber,
                String registrationDate) {
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.registrationDate = registrationDate;
        this.accountStatus = 1;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    @NonNull
    public String getEmail() { return email; }
    public void setEmail(@NonNull String email) { this.email = email; }

    @NonNull
    public String getUsername() { return username; }
    public void setUsername(@NonNull String username) { this.username = username; }

    @NonNull
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(@NonNull String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }

    public String getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(String lastLoginDate) { this.lastLoginDate = lastLoginDate; }

    public int getAccountStatus() { return accountStatus; }
    public void setAccountStatus(int accountStatus) { this.accountStatus = accountStatus; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}