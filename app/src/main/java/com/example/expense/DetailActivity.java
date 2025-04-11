package com.example.expense;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import com.example.expense.databinding.ActivityDetailBinding;
import com.example.expense.model.Card;
import com.example.expense.repository.CardRepository;
import com.example.expense.repository.IApiCallback;

public class DetailActivity extends BaseActivity {

    ActivityDetailBinding binding;
    CardRepository cardRepository;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        showProgressBar();

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
                hideProgressBar();
            }

            @Override
            public void onError(String errorMessage) {
                binding.tvAmount.setText("Error loading data");
                hideProgressBar();
            }
        });
        binding.backButton.setOnClickListener(view -> onBackPressed());
    }
    public void showProgressBar() {
        binding.loadingBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        binding.loadingBar.setVisibility(View.GONE);
    }
}