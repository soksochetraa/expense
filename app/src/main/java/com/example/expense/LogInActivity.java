package com.example.expense;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expense.databinding.ActivityLogInBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LogInActivity extends AppCompatActivity {

    private ActivityLogInBinding binding;
    private FirebaseAuth mAuth;

    @SuppressLint({"ClickableViewAccessibility", "RestrictedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        binding.btnLogIn.setOnClickListener(v -> loginUser());
        binding.tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LogInActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email is required!");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password is required!");
            return;
        }

        binding.loadingBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    binding.loadingBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(LogInActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(LogInActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LogInActivity.this, "Login failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
