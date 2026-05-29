package com.infinity.app.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.infinity.app.R;
import com.infinity.app.models.User;
import com.infinity.app.utils.Constants;
import com.infinity.app.utils.FirebaseHelper;
import com.infinity.app.utils.ImageUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Edit profile screen.
 * - Loads current user's data from /users/{uid}
 * - Optionally uploads a new avatar to /avatars/{uid}.jpg
 * - Saves fullName, username, bio (and avatarUrl if changed) back to /users/{uid}
 */
public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivAvatar, btnPickAvatar;
    private EditText etFullName, etUsername, etBio;
    private MaterialButton btnSave;
    private ProgressBar progress;

    private Uri newAvatar;
    private String currentAvatarUrl;

    private final ActivityResultLauncher<String> picker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    newAvatar = uri;
                    Glide.with(this).load(uri).into(ivAvatar);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ivAvatar = findViewById(R.id.ivAvatar);
        btnPickAvatar = findViewById(R.id.btnPickAvatar);
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etBio = findViewById(R.id.etBio);
        btnSave = findViewById(R.id.btnSave);
        progress = findViewById(R.id.progress);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnPickAvatar.setOnClickListener(v -> picker.launch("image/*"));
        btnSave.setOnClickListener(v -> save());

        loadCurrent();
    }

    private void loadCurrent() {
        String uid = FirebaseHelper.currentUid();
        if (uid == null) { finish(); return; }
        FirebaseHelper.db(Constants.DB_USERS, uid).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snap) {
                        User u = snap.getValue(User.class);
                        if (u == null) return;
                        etFullName.setText(u.getFullName());
                        etUsername.setText(u.getUsername());
                        etBio.setText(u.getBio());
                        currentAvatarUrl = u.getAvatarUrl();
                        if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty()) {
                            Glide.with(EditProfileActivity.this)
                                    .load(currentAvatarUrl)
                                    .placeholder(R.drawable.placeholder_avatar)
                                    .into(ivAvatar);
                        }
                    }
                    @Override public void onCancelled(DatabaseError error) { /* no-op */ }
                });
    }

    private void save() {
        String uid = FirebaseHelper.currentUid();
        if (uid == null) return;

        progress.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        Runnable writeFields = () -> {
            // We do partial updates so we don't overwrite avatarUrl unintentionally.
            Map<String, Object> updates = new HashMap<>();
            updates.put("fullName", etFullName.getText().toString().trim());
            updates.put("username", etUsername.getText().toString().trim().toLowerCase());
            updates.put("bio", etBio.getText().toString().trim());
            if (currentAvatarUrl != null) updates.put("avatarUrl", currentAvatarUrl);

            FirebaseHelper.db(Constants.DB_USERS, uid)
                    .updateChildren(updates)
                    .addOnCompleteListener(t -> {
                        progress.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        if (t.isSuccessful()) {
                            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this,
                                    getString(R.string.something_went_wrong),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        };

        if (newAvatar == null) {
            // Nothing to upload: just update text fields.
            writeFields.run();
            return;
        }

        // Otherwise: compress + upload, then update text fields with the new url.
        try {
            byte[] bytes = ImageUtils.compress(this, newAvatar, 512, 80);
            StorageReference ref = FirebaseHelper
                    .storageRef(Constants.STORAGE_AVATARS, uid + ".jpg");
            ref.putBytes(bytes)
                    .continueWithTask(t -> {
                        if (!t.isSuccessful() && t.getException() != null) throw t.getException();
                        return ref.getDownloadUrl();
                    })
                    .addOnSuccessListener(uri -> {
                        currentAvatarUrl = uri.toString();
                        writeFields.run();
                    })
                    .addOnFailureListener(e -> {
                        progress.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            progress.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
