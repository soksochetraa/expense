package com.example.expense.repository;

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

        // Make an initial call to load cards
        loadCardsRecursively(createdBy, currentPage, allCards, callback);
    }

    public void getCard(String cardId, final IApiCallback<Card> callback) {
        cardService.getCard(cardId).enqueue(new Callback<Card>() {
            @Override
            public void onResponse(Call<Card> call, Response<Card> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<Card> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    public void getCards(int page, String createdBy, final IApiCallback<List<Card>> callback) {
        Call<List<Card>> call = cardService.getCards(createdBy, page, PAGE_SIZE);

        call.enqueue(new Callback<List<Card>>() {
            @Override
            public void onResponse(Call<List<Card>> call, Response<List<Card>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<Card>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    public void deleteCard(String cardId, final IApiCallback<String> callback) {
        cardService.deleteCard(cardId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("Card deleted successfully");
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void createCard(Card card, final IApiCallback<Card> callback) {
        cardService.createCard(card).enqueue(new Callback<Card>() {
            @Override
            public void onResponse(Call<Card> call, Response<Card> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Card> call, Throwable t) {
                String error = t.getMessage() != null ? t.getMessage() : "Unknown error occurred";
                callback.onError("Error: " + error);
            }
        });
    }

    private <T> void handleResponse(Call<T> call, Response<T> response, IApiCallback<T> callback) {
        if (response.isSuccessful() && response.body() != null) {
            callback.onSuccess(response.body());
        } else {
            callback.onError(getErrorMessage(response));
        }
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
        cardService.getCards(createdBy, currentPage, PAGE_SIZE).enqueue(new Callback<List<Card>>() {
            @Override
            public void onResponse(Call<List<Card>> call, Response<List<Card>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Card> cards = response.body();
                    allCards.addAll(cards);

                    // If the current page contains fewer cards than the page size, we've reached the last page
                    if (cards.size() < PAGE_SIZE) {
                        // Callback with all the loaded cards
                        callback.onSuccess(allCards);
                    } else {
                        // Load the next page
                        loadCardsRecursively(createdBy, currentPage + 1, allCards, callback);
                    }
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<Card>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}
