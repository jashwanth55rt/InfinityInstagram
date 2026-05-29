package com.infinity.app;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Application subclass. Used to enable Firebase Realtime Database disk persistence
 * once for the whole app, before any DatabaseReference is obtained.
 */
public class InfinityApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            // Cache data locally so the UI feels snappy even on flaky connections.
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception ignored) {
            // setPersistenceEnabled throws if called twice; safe to ignore.
        }
    }
}
