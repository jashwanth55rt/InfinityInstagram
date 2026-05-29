package com.infinity.app.models;

/**
 * Represents a single post in /posts/{postId}.
 * Likes are stored as a sub-node /posts/{postId}/likes/{uid} = true.
 */
public class Post {
    private String postId;
    private String authorId;
    private String imageUrl;
    private String caption;
    private long timestamp;
    private long likeCount;

    public Post() { }

    public Post(String postId, String authorId, String imageUrl, String caption, long timestamp) {
        this.postId = postId;
        this.authorId = authorId;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.timestamp = timestamp;
        this.likeCount = 0;
    }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public long getLikeCount() { return likeCount; }
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }
}
