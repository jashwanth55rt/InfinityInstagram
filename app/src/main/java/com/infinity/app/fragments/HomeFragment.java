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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.infinity.app.R;
import com.infinity.app.activities.UploadStoryActivity;
import com.infinity.app.adapters.PostAdapter;
import com.infinity.app.adapters.StoryAdapter;
import com.infinity.app.models.Post;
import com.infinity.app.models.Story;
import com.infinity.app.utils.Constants;
import com.infinity.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Home tab. Two RecyclerViews stacked:
 *  - top: horizontal stories rail (last 24h)
 *  - bottom: vertical post feed (newest first)
 */
public class HomeFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvStories, rvFeed;
    private TextView tvEmptyFeed;
    private ImageView btnAddStoryHeader;

    private final List<Post> posts = new ArrayList<>();
    private final List<Story> stories = new ArrayList<>();
    private PostAdapter postAdapter;
    private StoryAdapter storyAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        swipeRefresh = v.findViewById(R.id.swipeRefresh);
        rvStories = v.findViewById(R.id.rvStories);
        rvFeed = v.findViewById(R.id.rvFeed);
        tvEmptyFeed = v.findViewById(R.id.tvEmptyFeed);
        btnAddStoryHeader = v.findViewById(R.id.btnAddStoryHeader);

        rvStories.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        storyAdapter = new StoryAdapter(getContext(), stories);
        rvStories.setAdapter(storyAdapter);

        rvFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        postAdapter = new PostAdapter(getContext(), posts);
        rvFeed.setAdapter(postAdapter);

        btnAddStoryHeader.setOnClickListener(view ->
                startActivity(new Intent(getContext(), UploadStoryActivity.class)));

        swipeRefresh.setOnRefreshListener(this::loadAll);

        loadAll();
    }

    private void loadAll() {
        loadStories();
        loadFeed();
    }

    private void loadStories() {
        FirebaseHelper.db(Constants.DB_STORIES).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        stories.clear();
                        long cutoff = System.currentTimeMillis() - Constants.STORY_TTL_MS;
                        for (DataSnapshot c : snap.getChildren()) {
                            Story s = c.getValue(Story.class);
                            if (s != null && s.getTimestamp() >= cutoff) stories.add(s);
                        }
                        // Newest first.
                        Collections.sort(stories,
                                (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                        storyAdapter.notifyDataSetChanged();
                    }
                    @Override public void onCancelled(DatabaseError error) { }
                });
    }

    private void loadFeed() {
        FirebaseHelper.db(Constants.DB_POSTS).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        posts.clear();
                        for (DataSnapshot c : snap.getChildren()) {
                            Post p = c.getValue(Post.class);
                            if (p != null) posts.add(p);
                        }
                        Collections.sort(posts,
                                (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                        postAdapter.notifyDataSetChanged();
                        tvEmptyFeed.setVisibility(posts.isEmpty() ? View.VISIBLE : View.GONE);
                        swipeRefresh.setRefreshing(false);
                    }
                    @Override public void onCancelled(DatabaseError error) {
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }
}
