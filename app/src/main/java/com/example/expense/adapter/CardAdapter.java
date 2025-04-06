package com.example.expense.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense.DetailActivity;
import com.example.expense.databinding.ExpenseCardBinding;
import com.example.expense.model.Card;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    List<Card> cards;

    public CardAdapter() {
        this.cards = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ExpenseCardBinding binding = ExpenseCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Card card = cards.get(position);
        String currency = "KHR".equals(card.getCurrency()) ? "៛" : "$";
        String formattedDate = formatDate(card.getCreatedDate());

        holder.binding.tvAmountTitle.setText(card.getAmount() + " " + currency);
        holder.binding.tvCategoryTitle.setText(card.getCategory());
        holder.binding.tvDateTitle.setText(formattedDate);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(holder.itemView.getContext(), DetailActivity.class);
            intent.putExtra("CardId", card.getId());
            holder.itemView.getContext().startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setCards(List<Card> newCards) {
        cards.clear();
        cards.addAll(newCards);
        notifyDataSetChanged();
    }

    public void addCards(List<Card> newCards) {
        int startPosition = cards.size();
        cards.addAll(newCards);
        notifyItemRangeInserted(startPosition, newCards.size());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        protected ExpenseCardBinding binding;

        public ViewHolder(@NonNull ExpenseCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private String formatDate(String isoDate) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            return displayFormat.format(isoFormat.parse(isoDate));
        } catch (ParseException e) {
            return isoDate;
        }
    }

}
