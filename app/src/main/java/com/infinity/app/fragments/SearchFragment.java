package com.infinity.app.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.infinity.app.R;
import com.infinity.app.adapters.UserAdapter;
import com.infinity.app.models.User;
import com.infinity.app.utils.Constants;
import com.infinity.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Search tab. Loads all users once and filters them client-side as the user types.
 * For very large user counts you'd switch this to a Firebase query
 * (orderByChild("username").startAt(prefix).endAt(prefix + "\uf8ff")).
 */
public class SearchFragment extends Fragment {

    private EditText etSearch;
    private RecyclerView rv;
    private final List<User> all = new ArrayList<>();
    private final List<User> filtered = new ArrayList<>();
    private UserAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        etSearch = v.findViewById(R.id.etSearch);
        rv = v.findViewById(R.id.rvUsers);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserAdapter(getContext(), filtered);
        rv.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadUsers();
    }

    private void loadUsers() {
        FirebaseHelper.db(Constants.DB_USERS).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        all.clear();
                        String me = FirebaseHelper.currentUid();
                        for (DataSnapshot c : snap.getChildren()) {
                            User u = c.getValue(User.class);
                            // Hide myself from search results.
                            if (u != null && (me == null || !me.equals(u.getUid()))) {
                                all.add(u);
                            }
                        }
                        applyFilter(etSearch.getText().toString());
                    }
                    @Override public void onCancelled(DatabaseError error) { }
                });
    }

    private void applyFilter(String query) {
        filtered.clear();
        String q = query == null ? "" : query.trim().toLowerCase();
        for (User u : all) {
            String un = u.getUsername() == null ? "" : u.getUsername().toLowerCase();
            String fn = u.getFullName() == null ? "" : u.getFullName().toLowerCase();
            if (q.isEmpty() || un.contains(q) || fn.contains(q)) filtered.add(u);
        }
        adapter.notifyDataSetChanged();
    }
}
