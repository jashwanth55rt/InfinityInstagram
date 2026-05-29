package com.infinity.app.models;

/**
 * Represents a story in /stories/{storyId}.
 * Stories are kept short-lived; clients filter them by timestamp (e.g. last 24h).
 */
public class Story {
    private String storyId;
    private String authorId;
    private String imageUrl;
    private long timestamp;

    public Story() { }

    public Story(String storyId, String authorId, String imageUrl, long timestamp) {
        this.storyId = storyId;
        this.authorId = authorId;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public String getStoryId() { return storyId; }
    public void setStoryId(String storyId) { this.storyId = storyId; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
