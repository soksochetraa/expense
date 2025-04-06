package com.example.expense;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expense.databinding.ActivityWelcomeBinding;

public class WelcomeActivity extends AppCompatActivity {

    ActivityWelcomeBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnGetStart.setOnClickListener(v -> {
            Intent intent = new Intent(this, LogInActivity.class);
            startActivity(intent);
            finish();
        });
    }
}