package com.infinity.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.infinity.app.R;
import com.infinity.app.adapters.ProfilePostAdapter;
import com.infinity.app.models.Post;
import com.infinity.app.models.User;
import com.infinity.app.utils.Constants;
import com.infinity.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Read-only profile of *another* user. Shows their stats, bio, post grid and
 * a follow/unfollow button. The follow relationship is mirrored in two paths:
 *   /follows/{me}/{them}     = true  (people I follow)
 *   /followers/{them}/{me}   = true  (their followers)
 * This denormalization makes follower/following counts O(1) reads.
 */
public class UserProfileActivity extends AppCompatActivity {

    private TextView tvUsernameTop, tvFullName, tvBio, tvPostCount, tvFollowerCount, tvFollowingCount;
    private ImageView ivAvatar;
    private MaterialButton btnFollow;
    private RecyclerView rvProfilePosts;

    private String targetUid;
    private boolean isFollowing = false;
    private final List<Post> posts = new ArrayList<>();
    private ProfilePostAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        targetUid = getIntent().getStringExtra(Constants.EXTRA_USER_ID);
        if (targetUid == null) {
            Toast.makeText(this, "Missing user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvUsernameTop = findViewById(R.id.tvUsernameTop);
        tvFullName = findViewById(R.id.tvFullName);
        tvBio = findViewById(R.id.tvBio);
        tvPostCount = findViewById(R.id.tvPostCount);
        tvFollowerCount = findViewById(R.id.tvFollowerCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        ivAvatar = findViewById(R.id.ivAvatar);
        btnFollow = findViewById(R.id.btnFollow);
        rvProfilePosts = findViewById(R.id.rvProfilePosts);

        rvProfilePosts.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new ProfilePostAdapter(this, posts);
        rvProfilePosts.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnFollow.setOnClickListener(v -> toggleFollow());

        // Hide the follow button if user is on their own profile (defensive).
        String me = FirebaseHelper.currentUid();
        if (me != null && me.equals(targetUid)) {
            btnFollow.setVisibility(View.GONE);
        }

        loadUser();
        loadPosts();
        loadFollowState();
        loadCounts();
    }

    private void loadUser() {
        FirebaseHelper.db(Constants.DB_USERS, targetUid).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        User u = snap.getValue(User.class);
                        if (u == null) return;
                        tvUsernameTop.setText(u.getUsername());
                        tvFullName.setText(u.getFullName());
                        tvBio.setText(u.getBio());
                        if (u.getAvatarUrl() != null && !u.getAvatarUrl().isEmpty()) {
                            Glide.with(UserProfileActivity.this)
                                    .load(u.getAvatarUrl())
                                    .placeholder(R.drawable.placeholder_avatar)
                                    .into(ivAvatar);
                        }
                    }
                    @Override public void onCancelled(DatabaseError error) {}
                });
    }

    private void loadPosts() {
        FirebaseHelper.db(Constants.DB_POSTS).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        posts.clear();
                        for (DataSnapshot c : snap.getChildren()) {
                            Post p = c.getValue(Post.class);
                            if (p != null && targetUid.equals(p.getAuthorId())) posts.add(p);
                        }
                        Collections.sort(posts, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                        adapter.notifyDataSetChanged();
                        tvPostCount.setText(String.valueOf(posts.size()));
                    }
                    @Override public void onCancelled(DatabaseError error) {}
                });
    }

    private void loadCounts() {
        FirebaseHelper.db(Constants.DB_FOLLOWERS, targetUid).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        tvFollowerCount.setText(String.valueOf(snap.getChildrenCount()));
                    }
                    @Override public void onCancelled(DatabaseError error) {}
                });
        FirebaseHelper.db(Constants.DB_FOLLOWS, targetUid).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        tvFollowingCount.setText(String.valueOf(snap.getChildrenCount()));
                    }
                    @Override public void onCancelled(DatabaseError error) {}
                });
    }

    private void loadFollowState() {
        String me = FirebaseHelper.currentUid();
        if (me == null) return;
        FirebaseHelper.db(Constants.DB_FOLLOWS, me, targetUid).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        isFollowing = snap.exists();
                        btnFollow.setText(isFollowing ? R.string.following : R.string.follow);
                    }
                    @Override public void onCancelled(DatabaseError error) {}
                });
    }

    private void toggleFollow() {
        String me = FirebaseHelper.currentUid();
        if (me == null) return;

        // Optimistic UI flip; we revert if the write fails.
        boolean willFollow = !isFollowing;
        btnFollow.setEnabled(false);

        Runnable revert = () -> {
            btnFollow.setEnabled(true);
            btnFollow.setText(isFollowing ? R.string.following : R.string.follow);
            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
        };

        if (willFollow) {
            FirebaseHelper.db(Constants.DB_FOLLOWS, me, targetUid).setValue(true)
                    .addOnSuccessListener(v -> FirebaseHelper.db(Constants.DB_FOLLOWERS, targetUid, me).setValue(true)
                            .addOnSuccessListener(v2 -> {
                                isFollowing = true;
                                btnFollow.setEnabled(true);
                                btnFollow.setText(R.string.following);
                                writeNotification(me);
                                loadCounts();
                            })
                            .addOnFailureListener(e -> revert.run()))
                    .addOnFailureListener(e -> revert.run());
        } else {
            FirebaseHelper.db(Constants.DB_FOLLOWS, me, targetUid).removeValue()
                    .addOnSuccessListener(v -> FirebaseHelper.db(Constants.DB_FOLLOWERS, targetUid, me).removeValue()
                            .addOnSuccessListener(v2 -> {
                                isFollowing = false;
                                btnFollow.setEnabled(true);
                                btnFollow.setText(R.string.follow);
                                loadCounts();
                            })
                            .addOnFailureListener(e -> revert.run()))
                    .addOnFailureListener(e -> revert.run());
        }
    }

    private void writeNotification(String me) {
        // Push a "follow" notification to the followee's notification feed.
        String notifId = FirebaseHelper.db(Constants.DB_NOTIFICATIONS, targetUid).push().getKey();
        if (notifId == null) return;
        com.infinity.app.models.Notification n = new com.infinity.app.models.Notification(
                notifId, me, "follow", null, System.currentTimeMillis());
        FirebaseHelper.db(Constants.DB_NOTIFICATIONS, targetUid, notifId).setValue(n);
    }
}
