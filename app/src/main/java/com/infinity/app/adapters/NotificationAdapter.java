package com.infinity.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.infinity.app.R;
import com.infinity.app.models.Notification;
import com.infinity.app.models.Post;
import com.infinity.app.models.User;
import com.infinity.app.utils.Constants;
import com.infinity.app.utils.FirebaseHelper;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Renders notifications. Each row resolves:
 *   - the actor's username/avatar (from /users/{fromUid})
 *   - if it's a post-related notif, a small thumbnail (from /posts/{postId})
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {
    private final Context ctx;
    private final List<Notification> data;

    public NotificationAdapter(Context ctx, List<Notification> data) {
        this.ctx = ctx;
        this.data = data;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Notification n = data.get(position);
        h.ivAvatar.setImageResource(R.drawable.placeholder_avatar);
        h.ivThumb.setVisibility(n.getPostId() != null ? View.VISIBLE : View.GONE);
        h.tvText.setText("");

        FirebaseHelper.db(Constants.DB_USERS, n.getFromUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        User u = snap.getValue(User.class);
                        String name = u != null ? u.getUsername() : "Someone";
                        String action;
                        switch (n.getType() == null ? "" : n.getType()) {
                            case "like":   action = "liked your post"; break;
                            case "follow": action = "started following you"; break;
                            case "comment": action = "commented on your post"; break;
                            default: action = "interacted with you";
                        }
                        h.tvText.setText(name + " " + action);
                        if (u != null && u.getAvatarUrl() != null && !u.getAvatarUrl().isEmpty()) {
                            Glide.with(ctx).load(u.getAvatarUrl())
                                    .placeholder(R.drawable.placeholder_avatar)
                                    .into(h.ivAvatar);
                        }
                    }
                    @Override public void onCancelled(DatabaseError error) { }
                });

        if (n.getPostId() != null) {
            FirebaseHelper.db(Constants.DB_POSTS, n.getPostId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(DataSnapshot snap) {
                            Post p = snap.getValue(Post.class);
                            if (p != null && p.getImageUrl() != null) {
                                Glide.with(ctx).load(p.getImageUrl())
                                        .placeholder(R.drawable.placeholder_image)
                                        .into(h.ivThumb);
                            }
                        }
                        @Override public void onCancelled(DatabaseError error) { }
                    });
        }
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        ImageView ivThumb;
        TextView tvText;
        VH(View v) {
            super(v);
            ivAvatar = v.findViewById(R.id.ivAvatar);
            ivThumb = v.findViewById(R.id.ivThumb);
            tvText = v.findViewById(R.id.tvText);
        }
    }
}
