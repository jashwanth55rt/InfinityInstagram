package com.infinity.app.activities;

import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.infinity.app.R;
import com.infinity.app.models.Post;
import com.infinity.app.utils.Constants;
import com.infinity.app.utils.FirebaseHelper;
import com.infinity.app.utils.ImageUtils;

/**
 * Lets the user pick an image from the gallery, write a caption, and publish a post.
 * Steps:
 *   1) Pick image (system picker via ActivityResult API)
 *   2) Compress on-device (smaller upload, faster delivery)
 *   3) Upload to Firebase Storage at /posts/{postId}.jpg
 *   4) Write the metadata document to /posts/{postId}
 */
public class UploadPostActivity extends AppCompatActivity {

    private ImageView ivPreview;
    private EditText etCaption;
    private MaterialButton btnPickImage, btnPost;
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
        setContentView(R.layout.activity_upload_post);

        ivPreview = findViewById(R.id.ivPreview);
        etCaption = findViewById(R.id.etCaption);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnPost = findViewById(R.id.btnPost);
        progress = findViewById(R.id.progress);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnPickImage.setOnClickListener(v -> picker.launch("image/*"));
        btnPost.setOnClickListener(v -> uploadPost());
    }

    private void uploadPost() {
        if (pickedImage == null) {
            Toast.makeText(this, "Pick an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = FirebaseHelper.currentUid();
        if (uid == null) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);
        btnPost.setEnabled(false);

        try {
            // Compress: 1080px longest side, ~80% JPEG quality.
            byte[] bytes = ImageUtils.compress(this, pickedImage, 1080, 80);

            // Allocate a postId via push() and use the same id for the storage filename.
            DatabaseReference postsRef = FirebaseHelper.db(Constants.DB_POSTS);
            String postId = postsRef.push().getKey();
            if (postId == null) throw new IllegalStateException("Could not allocate postId");

            StorageReference storageRef =
                    FirebaseHelper.storageRef(Constants.STORAGE_POSTS, postId + ".jpg");

            storageRef.putBytes(bytes)
                    .continueWithTask(t -> {
                        if (!t.isSuccessful() && t.getException() != null) throw t.getException();
                        return storageRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(downloadUri -> {
                        Post post = new Post(
                                postId,
                                uid,
                                downloadUri.toString(),
                                etCaption.getText().toString().trim(),
                                System.currentTimeMillis());
                        postsRef.child(postId).setValue(post)
                                .addOnCompleteListener(t -> {
                                    progress.setVisibility(View.GONE);
                                    btnPost.setEnabled(true);
                                    if (t.isSuccessful()) {
                                        Toast.makeText(this, "Posted!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, MainActivity.class)
                                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        finish();
                                    } else {
                                        Toast.makeText(this,
                                                getString(R.string.something_went_wrong),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        progress.setVisibility(View.GONE);
                        btnPost.setEnabled(true);
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            progress.setVisibility(View.GONE);
            btnPost.setEnabled(true);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
