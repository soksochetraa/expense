package com.example.expense;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import com.example.expense.databinding.ActivityWelcomeBinding;

public class WelcomeActivity extends BaseActivity {

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