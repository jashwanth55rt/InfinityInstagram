package com.infinity.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.infinity.app.R;
import com.infinity.app.activities.EditProfileActivity;
import com.infinity.app.activities.LoginActivity;
import com.infinity.app.adapters.ProfilePostAdapter;
import com.infinity.app.models.Post;
import com.infinity.app.models.User;
import com.infinity.app.utils.Constants;
import com.infinity.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Profile tab for the *current* user.
 * Shows: avatar, username, full name, bio, post count, follower/following counts,
 * and a 3-column grid of the user's posts. Includes Edit Profile + Logout actions.
 */
public class ProfileFragment extends Fragment {

    private TextView tvUsernameTop, tvFullName, tvBio, tvPostCount, tvFollowerCount,
            tvFollowingCount, tvEmpty;
    private ImageView ivAvatar, btnLogout;
    private MaterialButton btnEditProfile;
    private RecyclerView rvProfilePosts;

    private final List<Post> posts = new ArrayList<>();
    private ProfilePostAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        tvUsernameTop = v.findViewById(R.id.tvUsernameTop);
        tvFullName = v.findViewById(R.id.tvFullName);
        tvBio = v.findViewById(R.id.tvBio);
        tvPostCount = v.findViewById(R.id.tvPostCount);
        tvFollowerCount = v.findViewById(R.id.tvFollowerCount);
        tvFollowingCount = v.findViewById(R.id.tvFollowingCount);
        tvEmpty = v.findViewById(R.id.tvEmptyProfile);
        ivAvatar = v.findViewById(R.id.ivAvatar);
        btnLogout = v.findViewById(R.id.btnLogout);
        btnEditProfile = v.findViewById(R.id.btnEditProfile);
        rvProfilePosts = v.findViewById(R.id.rvProfilePosts);

        rvProfilePosts.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new ProfilePostAdapter(getContext(), posts);
        rvProfilePosts.setAdapter(adapter);

        btnEditProfile.setOnClickListener(view ->
                startActivity(new Intent(getContext(), EditProfileActivity.class)));

        btnLogout.setOnClickListener(view -> {
            FirebaseHelper.auth().signOut();
            Intent i = new Intent(getContext(), LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh on every resume so edits propagate immediately.
        loadProfile();
        loadCounts();
        loadMyPosts();
    }

    private void loadProfile() {
        String uid = FirebaseHelper.currentUid();
        if (uid == null) return;
        FirebaseHelper.db(Constants.DB_USERS, uid).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        User u = snap.getValue(User.class);
                        if (u == null || getContext() == null) return;
                        tvUsernameTop.setText(u.getUsername());
                        tvFullName.setText(u.getFullName());
                        tvBio.setText(u.getBio());
                        if (u.getAvatarUrl() != null && !u.getAvatarUrl().isEmpty()) {
                            Glide.with(ProfileFragment.this)
                                    .load(u.getAvatarUrl())
                                    .placeholder(R.drawable.placeholder_avatar)
                                    .into(ivAvatar);
                        }
                    }
                    @Override public void onCancelled(DatabaseError error) { }
                });
    }

    private void loadCounts() {
        String uid = FirebaseHelper.currentUid();
        if (uid == null) return;
        FirebaseHelper.db(Constants.DB_FOLLOWERS, uid).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        tvFollowerCount.setText(String.valueOf(snap.getChildrenCount()));
                    }
                    @Override public void onCancelled(DatabaseError error) { }
                });
        FirebaseHelper.db(Constants.DB_FOLLOWS, uid).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        tvFollowingCount.setText(String.valueOf(snap.getChildrenCount()));
                    }
                    @Override public void onCancelled(DatabaseError error) { }
                });
    }

    private void loadMyPosts() {
        String uid = FirebaseHelper.currentUid();
        if (uid == null) return;
        FirebaseHelper.db(Constants.DB_POSTS).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        posts.clear();
                        for (DataSnapshot c : snap.getChildren()) {
                            Post p = c.getValue(Post.class);
                            if (p != null && uid.equals(p.getAuthorId())) posts.add(p);
                        }
                        Collections.sort(posts,
                                (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                        adapter.notifyDataSetChanged();
                        tvPostCount.setText(String.valueOf(posts.size()));
                        tvEmpty.setVisibility(posts.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    @Override public void onCancelled(DatabaseError error) { }
                });
    }
}
