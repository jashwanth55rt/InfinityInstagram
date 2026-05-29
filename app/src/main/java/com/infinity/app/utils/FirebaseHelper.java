package com.infinity.app.utils;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Tiny convenience wrapper around the three Firebase services we use.
 * Using a helper keeps activities/fragments free of boilerplate and keeps the
 * Realtime Database URL configured in one place.
 */
public final class FirebaseHelper {
    private FirebaseHelper() {}

    public static FirebaseAuth auth() {
        return FirebaseAuth.getInstance();
    }

    /** Currently logged in user (may be null). */
    public static FirebaseUser currentUser() {
        return auth().getCurrentUser();
    }

    public static String currentUid() {
        FirebaseUser u = currentUser();
        return u == null ? null : u.getUid();
    }

    public static FirebaseDatabase database() {
        // The database URL is also baked into google-services.json, but we set it
        // explicitly so the helper works even if someone drops in a different config.
        return FirebaseDatabase.getInstance(
                "https://premium-eae4a-default-rtdb.firebaseio.com");
    }

    @NonNull
    public static DatabaseReference db(@NonNull String... pathSegments) {
        DatabaseReference ref = database().getReference();
        for (String s : pathSegments) ref = ref.child(s);
        return ref;
    }

    public static FirebaseStorage storage() {
        return FirebaseStorage.getInstance();
    }

    @NonNull
    public static StorageReference storageRef(@NonNull String... pathSegments) {
        StorageReference ref = storage().getReference();
        for (String s : pathSegments) ref = ref.child(s);
        return ref;
    }
}
