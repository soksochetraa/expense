package com.example.expense.service;

import com.example.expense.model.Card;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CardService {

    @GET("expenses?_expand=category&_sort=createdDate&_order=desc")
    Call<List<Card>> getCards(@Query("createdBy") String createdBy, @Query("_page") int page, @Query("_limit") int limit);

    @GET("expenses/{id}")
    Call<Card> getCard(@Path("id") String id);

    @POST("expenses/")
    Call<Card> createCard(@Body Card card);

    @DELETE("expenses/{id}")
    Call<Void> deleteCard(@Path("id") String cardId);
}
