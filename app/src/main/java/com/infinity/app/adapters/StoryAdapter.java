package com.infinity.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.infinity.app.R;
import com.infinity.app.models.Story;
import com.infinity.app.models.User;
import com.infinity.app.utils.Constants;
import com.infinity.app.utils.FirebaseHelper;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Horizontal carousel of "story" circles. Each item resolves the author's
 * username + avatar by reading /users/{authorId} on the fly.
 */
public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.VH> {
    private final Context ctx;
    private final List<Story> stories;

    public StoryAdapter(Context ctx, List<Story> stories) {
        this.ctx = ctx;
        this.stories = stories;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_story, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Story s = stories.get(position);

        // Pretty entrance animation on first bind.
        h.itemView.startAnimation(AnimationUtils.loadAnimation(ctx, R.anim.scale_pop));

        // Default fallbacks while we resolve user data.
        h.tvStoryUser.setText("");
        h.ivAvatar.setImageResource(R.drawable.placeholder_avatar);

        FirebaseHelper.db(Constants.DB_USERS, s.getAuthorId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        User u = snap.getValue(User.class);
                        if (u == null) return;
                        h.tvStoryUser.setText(u.getUsername());
                        // We prefer the actual story image inside the ring, but fall back to avatar.
                        String img = s.getImageUrl();
                        if (img != null && !img.isEmpty()) {
                            Glide.with(ctx).load(img)
                                    .placeholder(R.drawable.placeholder_avatar)
                                    .into(h.ivAvatar);
                        } else if (u.getAvatarUrl() != null && !u.getAvatarUrl().isEmpty()) {
                            Glide.with(ctx).load(u.getAvatarUrl())
                                    .placeholder(R.drawable.placeholder_avatar)
                                    .into(h.ivAvatar);
                        }
                    }
                    @Override public void onCancelled(DatabaseError error) { }
                });
    }

    @Override public int getItemCount() { return stories.size(); }

    static class VH extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvStoryUser;
        VH(View v) {
            super(v);
            ivAvatar = v.findViewById(R.id.ivStoryAvatar);
            tvStoryUser = v.findViewById(R.id.tvStoryUser);
        }
    }
}
