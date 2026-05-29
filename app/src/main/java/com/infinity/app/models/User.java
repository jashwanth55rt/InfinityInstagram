package com.infinity.app.models;

/**
 * Plain-old Java object representing a user document in Realtime Database.
 * Field names must match the keys used in /users/{uid} for auto-deserialization.
 */
public class User {
    private String uid;
    private String username;
    private String fullName;
    private String email;
    private String bio;
    private String avatarUrl;

    public User() {
        // Required empty constructor for Firebase.
    }

    public User(String uid, String username, String fullName, String email, String bio, String avatarUrl) {
        this.uid = uid;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
