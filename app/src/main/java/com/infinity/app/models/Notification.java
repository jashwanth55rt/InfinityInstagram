package com.infinity.app.models;

/**
 * Notification entry stored at /notifications/{recipientUid}/{notifId}.
 * `type` is one of: "like", "follow", "comment".
 */
public class Notification {
    private String notifId;
    private String fromUid;
    private String type;       // "like", "follow", etc.
    private String postId;     // optional - present for like/comment
    private long timestamp;

    public Notification() { }

    public Notification(String notifId, String fromUid, String type, String postId, long timestamp) {
        this.notifId = notifId;
        this.fromUid = fromUid;
        this.type = type;
        this.postId = postId;
        this.timestamp = timestamp;
    }

    public String getNotifId() { return notifId; }
    public void setNotifId(String notifId) { this.notifId = notifId; }

    public String getFromUid() { return fromUid; }
    public void setFromUid(String fromUid) { this.fromUid = fromUid; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
