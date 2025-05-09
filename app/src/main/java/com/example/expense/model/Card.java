package com.example.expense.model;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.expense.util.ISO8601DateAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

public class Card {

    @SerializedName("id")
    private String id = "";

    @SerializedName("amount")
    private double amount = 0;

    @SerializedName("currency")
    private String currency = "USD";

    @SerializedName("category")
    private String category = "Other";

    @SerializedName("remark")
    private String remark = "";

    @SerializedName("createdBy")
    private String createdBy = "";

    @SerializedName("createdDate")
    @JsonAdapter(ISO8601DateAdapter.class)
    private Date createdDate = new Date();

    @SerializedName("imageUrl")
    private String imageUrl = "";

    public Card(String id, double amount, String currency, String category, String remark, String createdBy, Date createdDate, String imageUrl) {
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.category = category;
        this.remark = remark;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.imageUrl = imageUrl;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRemark() {
        return remark;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return createdDate != null ? sdf.format(createdDate) : "N/A";
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @NonNull
    @Override
    public String toString() {
        return "Card{id='" + id + "', amount=" + amount + ", currency='" + currency + "', category='" + category + "', remark='" + remark + "', createdBy='" + createdBy + "', createdDate=" + createdDate + "', imageUrl='" + imageUrl + "'}";
    }
}
