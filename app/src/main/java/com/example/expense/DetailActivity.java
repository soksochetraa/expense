package com.example.expense;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.expense.databinding.ActivityDetailBinding;
import com.example.expense.model.Card;
import com.example.expense.repository.CardRepository;
import com.example.expense.repository.IApiCallback;

public class DetailActivity extends AppCompatActivity {

    ActivityDetailBinding binding;
    CardRepository cardRepository;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cardRepository = new CardRepository();
        String cardId = getIntent().getStringExtra("CardId");
        cardRepository.getCard(String.valueOf(cardId), new IApiCallback<>() {
            @Override
            public void onSuccess(Card card) {
                String currency = "$";
                if ("KHR".equals(card.getCurrency())) {
                    currency = "៛";
                }
                binding.tvAmount.setText(card.getAmount() + " " + currency);
                binding.tvCategory.setText(card.getCategory());
                binding.tvDate.setText(card.getCreatedDate());
                binding.tvRemark.setText(card.getRemark());
            }

            @Override
            public void onError(String errorMessage) {
                binding.tvAmount.setText("Error loading data");
            }
        });
        binding.backButton.setOnClickListener(view -> onBackPressed());
    }
}