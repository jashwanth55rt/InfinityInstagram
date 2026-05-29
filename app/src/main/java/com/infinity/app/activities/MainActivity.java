package com.infinity.app.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.infinity.app.R;
import com.infinity.app.fragments.HomeFragment;
import com.infinity.app.fragments.NotificationsFragment;
import com.infinity.app.fragments.ProfileFragment;
import com.infinity.app.fragments.SearchFragment;

/**
 * Hosts the four bottom-nav fragments (home/search/notifications/profile).
 * The center "+" tab is a shortcut that launches UploadPostActivity instead of
 * swapping a fragment.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showFragment(new HomeFragment()); return true;
            } else if (id == R.id.nav_search) {
                showFragment(new SearchFragment()); return true;
            } else if (id == R.id.nav_add) {
                // Don't switch the fragment; just open the upload screen.
                startActivity(new Intent(this, UploadPostActivity.class));
                return false;
            } else if (id == R.id.nav_notifications) {
                showFragment(new NotificationsFragment()); return true;
            } else if (id == R.id.nav_profile) {
                showFragment(new ProfileFragment()); return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            showFragment(new HomeFragment());
        }
    }

    private void showFragment(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, f)
                .commit();
    }
}
