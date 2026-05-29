package com.infinity.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.infinity.app.R;
import com.infinity.app.adapters.NotificationAdapter;
import com.infinity.app.models.Notification;
import com.infinity.app.utils.Constants;
import com.infinity.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Activity / notifications tab.
 * Reads from /notifications/{currentUid} and renders newest-first.
 */
public class NotificationsFragment extends Fragment {

    private RecyclerView rv;
    private TextView tvEmpty;
    private final List<Notification> data = new ArrayList<>();
    private NotificationAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        rv = v.findViewById(R.id.rvNotifications);
        tvEmpty = v.findViewById(R.id.tvEmptyNotifs);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(getContext(), data);
        rv.setAdapter(adapter);

        load();
    }

    private void load() {
        String uid = FirebaseHelper.currentUid();
        if (uid == null) return;
        FirebaseHelper.db(Constants.DB_NOTIFICATIONS, uid).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        data.clear();
                        for (DataSnapshot c : snap.getChildren()) {
                            Notification n = c.getValue(Notification.class);
                            if (n != null) data.add(n);
                        }
                        Collections.sort(data,
                                (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    @Override public void onCancelled(DatabaseError error) { }
                });
    }
}
