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
import com.infinity.app.R;
import com.infinity.app.utils.FirebaseHelper;

/**
 * Email + password login powered by FirebaseAuth.
 * On success we drop into MainActivity and clear the back stack so the user
 * cannot accidentally return to the login screen by pressing back.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progress = findViewById(R.id.progressLogin);
        TextView tvGoRegister = findViewById(R.id.tvGoRegister);

        btnLogin.setOnClickListener(v -> doLogin());
        tvGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String pwd = etPassword.getText().toString();

        // Cheap client-side validation before hitting Firebase.
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pwd)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        FirebaseHelper.auth().signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(task -> {
                    progress.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        Intent i = new Intent(this, MainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : getString(R.string.something_went_wrong);
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
