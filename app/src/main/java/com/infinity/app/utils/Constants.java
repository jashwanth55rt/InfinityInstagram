package com.infinity.app.utils;

/**
 * Centralized constants for Realtime Database paths and Storage folders.
 * Using constants here keeps every screen/adapter consistent and easy to refactor.
 */
public final class Constants {
    private Constants() {}

    // Realtime Database top-level nodes.
    public static final String DB_USERS = "users";
    public static final String DB_POSTS = "posts";
    public static final String DB_STORIES = "stories";
    public static final String DB_FOLLOWS = "follows";          // /follows/{follower}/{following} = true
    public static final String DB_FOLLOWERS = "followers";      // /followers/{user}/{follower} = true
    public static final String DB_LIKES = "likes";              // sub-node under each post
    public static final String DB_NOTIFICATIONS = "notifications";

    // Storage folders.
    public static final String STORAGE_AVATARS = "avatars";
    public static final String STORAGE_POSTS = "posts";
    public static final String STORAGE_STORIES = "stories";

    // Story TTL (24 hours).
    public static final long STORY_TTL_MS = 24L * 60L * 60L * 1000L;

    // Intent extras.
    public static final String EXTRA_USER_ID = "extra_user_id";
}
