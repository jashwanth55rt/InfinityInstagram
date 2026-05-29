package com.infinity.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;
import com.infinity.app.R;
import com.infinity.app.models.User;
import com.infinity.app.utils.Constants;
import com.infinity.app.utils.FirebaseHelper;

/**
 * Sign-up flow: creates a FirebaseAuth account and a matching /users/{uid}
 * document in the Realtime Database, then navigates to MainActivity.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etUsername, etEmail, etPassword;
    private MaterialButton btnRegister;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progress = findViewById(R.id.progressRegister);
        TextView tvGoLogin = findViewById(R.id.tvGoLogin);

        btnRegister.setOnClickListener(v -> doRegister());
        tvGoLogin.setOnClickListener(v -> finish());
    }

    private void doRegister() {
        final String fullName = etFullName.getText().toString().trim();
        final String username = etUsername.getText().toString().trim().toLowerCase();
        final String email = etEmail.getText().toString().trim();
        final String pwd = etPassword.getText().toString();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(username)
                || TextUtils.isEmpty(email) || TextUtils.isEmpty(pwd)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pwd.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        FirebaseHelper.auth()
                .createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        progress.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : getString(R.string.something_went_wrong);
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        return;
                    }
                    // Auth account created; now write the user profile in RTDB.
                    FirebaseUser fu = task.getResult().getUser();
                    if (fu == null) return;
                    User user = new User(fu.getUid(), username, fullName, email, "", "");
                    FirebaseHelper.db(Constants.DB_USERS, fu.getUid())
                            .setValue(user)
                            .addOnCompleteListener(t2 -> {
                                progress.setVisibility(View.GONE);
                                btnRegister.setEnabled(true);
                                if (t2.isSuccessful()) {
                                    Intent i = new Intent(this, MainActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i);
                                    finish();
                                } else {
                                    Toast.makeText(this,
                                            getString(R.string.something_went_wrong),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                });
    }
}
