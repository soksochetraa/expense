package com.example.expense;

import static android.app.PendingIntent.getActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.expense.databinding.ActivityDetailBinding;
import com.example.expense.model.Card;
import com.example.expense.repository.CardRepository;
import com.example.expense.repository.IApiCallback;

public class DetailActivity extends AppCompatActivity {

    ActivityDetailBinding binding;
    CardRepository cardRepository;


    boolean isLoading = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cardRepository = new CardRepository();
        String cardId = getIntent().getStringExtra("CardId");
        cardRepository.getCard(String.valueOf(cardId), new IApiCallback<Card>() {
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

        binding.delete.setOnClickListener(view -> {
            if (cardId == null) {
                Toast.makeText(view.getContext(), "Card ID is missing", Toast.LENGTH_SHORT).show();
                return;
            }

            isLoading = true;
            showProgressBar();

            cardRepository.deleteCard(cardId, new IApiCallback<String>() {
                @Override
                public void onSuccess(String message) {
                    isLoading = false;
                    hideProgressBar();
                    Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }

                @Override
                public void onError(String errorMessage) {
                    isLoading = false;
                    hideProgressBar();
                    Toast.makeText(view.getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });


        binding.backButton.setOnClickListener(view -> onBackPressed());
    }

    private void showProgressBar() {
        binding.loadingBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        binding.loadingBar.setVisibility(View.GONE);
    }
}
