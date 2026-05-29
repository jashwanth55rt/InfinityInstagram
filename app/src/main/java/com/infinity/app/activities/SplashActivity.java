package com.infinity.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.infinity.app.R;
import com.infinity.app.utils.FirebaseHelper;

/**
 * Splash entry point. Shows the brand for ~1.2s while we check the Firebase
 * Auth session, then routes the user to either MainActivity (logged in) or
 * LoginActivity (logged out).
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_MS = 1200L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::route, SPLASH_MS);
    }

    private void route() {
        Class<?> dest = FirebaseHelper.currentUser() != null
                ? MainActivity.class
                : LoginActivity.class;
        startActivity(new Intent(this, dest));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
