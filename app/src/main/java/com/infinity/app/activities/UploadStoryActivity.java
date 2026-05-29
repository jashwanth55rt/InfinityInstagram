package com.infinity.app.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.infinity.app.R;
import com.infinity.app.models.Story;
import com.infinity.app.utils.Constants;
import com.infinity.app.utils.FirebaseHelper;
import com.infinity.app.utils.ImageUtils;

/**
 * Same flow as UploadPostActivity but writes to /stories instead of /posts.
 * Stories are filtered by the home feed to only show entries from the past 24h.
 */
public class UploadStoryActivity extends AppCompatActivity {

    private ImageView ivPreview;
    private MaterialButton btnPickImage, btnUpload;
    private ProgressBar progress;
    private Uri pickedImage;

    private final ActivityResultLauncher<String> picker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    pickedImage = uri;
                    Glide.with(this).load(uri).into(ivPreview);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_story);

        ivPreview = findViewById(R.id.ivPreview);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnUpload = findViewById(R.id.btnUpload);
        progress = findViewById(R.id.progress);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnPickImage.setOnClickListener(v -> picker.launch("image/*"));
        btnUpload.setOnClickListener(v -> upload());
    }

    private void upload() {
        if (pickedImage == null) {
            Toast.makeText(this, "Pick an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = FirebaseHelper.currentUid();
        if (uid == null) return;

        progress.setVisibility(View.VISIBLE);
        btnUpload.setEnabled(false);

        try {
            byte[] bytes = ImageUtils.compress(this, pickedImage, 1080, 80);

            DatabaseReference storiesRef = FirebaseHelper.db(Constants.DB_STORIES);
            String storyId = storiesRef.push().getKey();
            if (storyId == null) throw new IllegalStateException("Could not allocate storyId");

            StorageReference storageRef =
                    FirebaseHelper.storageRef(Constants.STORAGE_STORIES, storyId + ".jpg");

            storageRef.putBytes(bytes)
                    .continueWithTask(t -> {
                        if (!t.isSuccessful() && t.getException() != null) throw t.getException();
                        return storageRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(uri -> {
                        Story story = new Story(storyId, uid, uri.toString(),
                                System.currentTimeMillis());
                        storiesRef.child(storyId).setValue(story)
                                .addOnCompleteListener(t -> {
                                    progress.setVisibility(View.GONE);
                                    btnUpload.setEnabled(true);
                                    if (t.isSuccessful()) {
                                        Toast.makeText(this, "Story uploaded",
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        progress.setVisibility(View.GONE);
                        btnUpload.setEnabled(true);
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            progress.setVisibility(View.GONE);
            btnUpload.setEnabled(true);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
