package com.infinity.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.infinity.app.R;
import com.infinity.app.models.Post;

import java.util.List;

/**
 * Square-thumbnail grid used on the profile page (3 columns).
 */
public class ProfilePostAdapter extends RecyclerView.Adapter<ProfilePostAdapter.VH> {
    private final Context ctx;
    private final List<Post> data;

    public ProfilePostAdapter(Context ctx, List<Post> data) {
        this.ctx = ctx;
        this.data = data;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_profile_post, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Post p = data.get(position);
        Glide.with(ctx).load(p.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .centerCrop()
                .into(h.iv);
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView iv;
        VH(View v) { super(v); iv = v.findViewById(R.id.ivThumb); }
    }
}
