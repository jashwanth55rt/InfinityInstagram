package com.infinity.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.infinity.app.R;
import com.infinity.app.activities.UserProfileActivity;
import com.infinity.app.models.User;
import com.infinity.app.utils.Constants;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Adapter for the user-search results list.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {
    private final Context ctx;
    private final List<User> data;

    public UserAdapter(Context ctx, List<User> data) {
        this.ctx = ctx;
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        User u = data.get(position);
        h.tvUsername.setText(u.getUsername());
        h.tvFullName.setText(u.getFullName());

        if (u.getAvatarUrl() != null && !u.getAvatarUrl().isEmpty()) {
            Glide.with(ctx).load(u.getAvatarUrl())
                    .placeholder(R.drawable.placeholder_avatar)
                    .into(h.ivAvatar);
        } else {
            h.ivAvatar.setImageResource(R.drawable.placeholder_avatar);
        }

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(ctx, UserProfileActivity.class);
            i.putExtra(Constants.EXTRA_USER_ID, u.getUid());
            ctx.startActivity(i);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvUsername, tvFullName;
        VH(View v) {
            super(v);
            ivAvatar = v.findViewById(R.id.ivAvatar);
            tvUsername = v.findViewById(R.id.tvUsername);
            tvFullName = v.findViewById(R.id.tvFullName);
        }
    }
}
