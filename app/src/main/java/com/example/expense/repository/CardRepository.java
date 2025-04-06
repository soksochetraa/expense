package com.example.expense.repository;

import androidx.annotation.NonNull;

import com.example.expense.model.Card;
import com.example.expense.service.CardService;
import com.example.expense.util.RetrofitClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardRepository {
    CardService cardService;
    private static final int PAGE_SIZE = 5;

    public CardRepository() {
        cardService = RetrofitClient.getClient().create(CardService.class);
    }

    public void getAllCards(String createdBy, final IApiCallback<List<Card>> callback) {
        List<Card> allCards = new ArrayList<>();
        int currentPage = 1;
        loadCardsRecursively(createdBy, currentPage, allCards, callback);
    }

    public void getCard(String cardId, final IApiCallback<Card> callback) {
        cardService.getCard(cardId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Card> call, @NonNull Response<Card> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Card> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getCards(int page, String createdBy, final IApiCallback<List<Card>> callback) {
        Call<List<Card>> call = cardService.getCards(createdBy, page, PAGE_SIZE);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Card>> call, @NonNull Response<List<Card>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Card>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void deleteCard(String cardId, final IApiCallback<String> callback) {
        cardService.deleteCard(cardId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("Card deleted successfully");
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void createCard(Card card, final IApiCallback<Card> callback) {
        cardService.createCard(card).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Card> call, @NonNull Response<Card> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Card> call, @NonNull Throwable t) {
                String error = t.getMessage() != null ? t.getMessage() : "Unknown error occurred";
                callback.onError("Error: " + error);
            }
        });
    }

    private String getErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                return "Error: " + response.code() + " - " + errorBody;
            }
            return "Error: " + response.code() + " - " + response.message();
        } catch (IOException e) {
            return "Error: " + response.code() + " (failed to read error body)";
        }
    }

    private void loadCardsRecursively(final String createdBy, final int currentPage, final List<Card> allCards, final IApiCallback<List<Card>> callback) {
        cardService.getCards(createdBy, currentPage, PAGE_SIZE).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Card>> call, @NonNull Response<List<Card>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Card> cards = response.body();
                    allCards.addAll(cards);
                    if (cards.size() < PAGE_SIZE) {
                        callback.onSuccess(allCards);
                    } else {
                        loadCardsRecursively(createdBy, currentPage + 1, allCards, callback);
                    }
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Card>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}
