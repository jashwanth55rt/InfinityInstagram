package com.infinity.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.infinity.app.R;
import com.infinity.app.activities.UserProfileActivity;
import com.infinity.app.models.Post;
import com.infinity.app.models.User;
import com.infinity.app.utils.Constants;
import com.infinity.app.utils.FirebaseHelper;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Adapter for the home feed. Responsibilities per row:
 *  - resolve the post author (avatar + username)
 *  - load the post image with Glide
 *  - render & toggle the like button (writes /posts/{postId}/likes/{uid})
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.VH> {
    private final Context ctx;
    private final List<Post> data;

    public PostAdapter(Context ctx, List<Post> data) {
        this.ctx = ctx;
        this.data = data;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_post, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        final Post post = data.get(position);

        h.itemView.startAnimation(AnimationUtils.loadAnimation(ctx, R.anim.fade_in));

        // Reset state - this view is recycled.
        h.tvAuthorName.setText("");
        h.ivAuthorAvatar.setImageResource(R.drawable.placeholder_avatar);

        // Author info.
        FirebaseHelper.db(Constants.DB_USERS, post.getAuthorId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        User u = snap.getValue(User.class);
                        if (u == null) return;
                        h.tvAuthorName.setText(u.getUsername());
                        if (u.getAvatarUrl() != null && !u.getAvatarUrl().isEmpty()) {
                            Glide.with(ctx).load(u.getAvatarUrl())
                                    .placeholder(R.drawable.placeholder_avatar)
                                    .into(h.ivAuthorAvatar);
                        }
                    }
                    @Override public void onCancelled(DatabaseError error) { }
                });

        // Open author profile when tapping the avatar/name.
        View.OnClickListener openProfile = v -> {
            Intent i = new Intent(ctx, UserProfileActivity.class);
            i.putExtra(Constants.EXTRA_USER_ID, post.getAuthorId());
            ctx.startActivity(i);
        };
        h.ivAuthorAvatar.setOnClickListener(openProfile);
        h.tvAuthorName.setOnClickListener(openProfile);

        // Caption.
        if (post.getCaption() != null && !post.getCaption().isEmpty()) {
            h.tvCaption.setVisibility(View.VISIBLE);
            h.tvCaption.setText(post.getCaption());
        } else {
            h.tvCaption.setVisibility(View.GONE);
        }

        // Post image.
        Glide.with(ctx).load(post.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .centerCrop()
                .into(h.ivPost);

        // Likes: read current count + my-like state, then bind toggle.
        bindLikes(h, post);
    }

    private void bindLikes(VH h, Post post) {
        final String me = FirebaseHelper.currentUid();
        final String postId = post.getPostId();

        FirebaseHelper.db(Constants.DB_POSTS, postId, Constants.DB_LIKES)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        long count = snap.getChildrenCount();
                        boolean liked = me != null && snap.hasChild(me);
                        h.tvLikeCount.setText(count + " likes");
                        h.btnLike.setImageResource(liked
                                ? R.drawable.ic_heart_filled
                                : R.drawable.ic_heart);
                    }
                    @Override public void onCancelled(DatabaseError error) { }
                });

        h.btnLike.setOnClickListener(v -> {
            if (me == null) return;
            // Heart pop animation.
            h.btnLike.startAnimation(AnimationUtils.loadAnimation(ctx, R.anim.scale_pop));

            FirebaseHelper.db(Constants.DB_POSTS, postId, Constants.DB_LIKES, me)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(DataSnapshot snap) {
                            if (snap.exists()) {
                                // Already liked -> unlike.
                                FirebaseHelper.db(Constants.DB_POSTS, postId,
                                        Constants.DB_LIKES, me).removeValue();
                            } else {
                                FirebaseHelper.db(Constants.DB_POSTS, postId,
                                        Constants.DB_LIKES, me).setValue(true);
                                // Send a notification to the post author (skip self-likes).
                                if (!me.equals(post.getAuthorId())) {
                                    String notifId = FirebaseHelper.db(Constants.DB_NOTIFICATIONS,
                                            post.getAuthorId()).push().getKey();
                                    if (notifId != null) {
                                        com.infinity.app.models.Notification n =
                                                new com.infinity.app.models.Notification(
                                                        notifId, me, "like", postId,
                                                        System.currentTimeMillis());
                                        FirebaseHelper.db(Constants.DB_NOTIFICATIONS,
                                                post.getAuthorId(), notifId).setValue(n);
                                    }
                                }
                            }
                            // Refresh counts for this row.
                            bindLikes(h, post);
                        }
                        @Override public void onCancelled(DatabaseError error) { }
                    });
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        CircleImageView ivAuthorAvatar;
        TextView tvAuthorName, tvCaption, tvLikeCount;
        ImageView ivPost, btnLike;

        VH(View v) {
            super(v);
            ivAuthorAvatar = v.findViewById(R.id.ivAuthorAvatar);
            tvAuthorName = v.findViewById(R.id.tvAuthorName);
            tvCaption = v.findViewById(R.id.tvCaption);
            tvLikeCount = v.findViewById(R.id.tvLikeCount);
            ivPost = v.findViewById(R.id.ivPost);
            btnLike = v.findViewById(R.id.btnLike);
        }
    }
}
