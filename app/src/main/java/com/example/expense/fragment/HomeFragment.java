package com.example.expense.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.expense.DetailActivity;
import com.example.expense.HomeActivity;
import com.example.expense.LogInActivity;
import com.example.expense.R;
import com.example.expense.adapter.CardAdapter;
import com.example.expense.databinding.FragmentHomeBinding;
import com.example.expense.model.Card;
import com.example.expense.repository.CardRepository;
import com.example.expense.repository.IApiCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    CardRepository cardRepository;
    CardAdapter cardAdapter;
    int currentPage = 1;
    boolean isLoading = false;
    FirebaseAuth mAuth;
    String latestCardId;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userID = user.getUid();
            getUsernameFromDatabase(userID);
        }

        HomeActivity homeActivity = (HomeActivity) getActivity();

        binding.tvSeeAll.setVisibility(View.GONE);
        binding.error.setVisibility(View.GONE);
        binding.rcvSomeOtherExpense.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rcvSomeOtherExpense.setNestedScrollingEnabled(false);
        cardRepository = new CardRepository();
        cardAdapter = new CardAdapter();
        binding.rcvSomeOtherExpense.setAdapter(cardAdapter);
        loadLatestCard();

        binding.btnAddExpenseNow.setOnClickListener(v->{
            if (homeActivity != null) {
                homeActivity.binding.bottomNavigation.setSelectedItemId(R.id.nav_add);
            }
        });

        binding.setting.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getContext(), LogInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            Activity activity = getActivity();
            if (activity != null) {
                activity.finish();
            }
        });

        binding.tvSeeAll.setOnClickListener(v -> {
            if (homeActivity != null) {
                homeActivity.binding.bottomNavigation.setSelectedItemId(R.id.nav_detail);
            }
        });

        binding.cardLastestExpense.setOnClickListener(view -> {
            if (latestCardId != null && !latestCardId.isEmpty()) {
                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.putExtra("CardId", latestCardId);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "No card data available", Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomeActivity) requireActivity()).showProgressBar();
        currentPage = 1;
        loadLatestCard();
        loadCards();
    }
    private void loadLatestCard() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            ((HomeActivity) requireActivity()).hideProgressBar();
            return;
        }

        String currentUserId = user.getUid();

        cardRepository.getCards(1, currentUserId, new IApiCallback<>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(List<Card> cards) {
                if (!cards.isEmpty()) {
                    Card latestCard = cards.get(0);
                    latestCardId = latestCard.getId();

                    String symbol = "$";
                    if (latestCard.getCurrency().equals("KHR")) {
                        symbol = "៛";
                    }

                    binding.tvAmount.setText(symbol + " " + latestCard.getAmount());
                    binding.tvCategoryDisplay.setText(latestCard.getCategory());
                    binding.tvDateDisplay.setText(formatDate(latestCard.getCreatedDate()));
                }
                isLoading = false;
                ((HomeActivity) requireActivity()).hideProgressBar();
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadCards() {
        isLoading = true;
        ((HomeActivity) requireActivity()).showProgressBar();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            ((HomeActivity) requireActivity()).hideProgressBar();
            return;
        }

        String currentUserId = user.getUid();
        cardRepository.getCards(currentPage, currentUserId, new IApiCallback<>() {
            @Override
            public void onSuccess(List<Card> cards) {
                if (!cards.isEmpty()) {
                    cardAdapter.setCards(cards);
                    currentPage++;
                    binding.error.setVisibility(View.GONE);
                    binding.tvSeeAll.setVisibility(View.VISIBLE);
                } else {
                    binding.error.setVisibility(View.VISIBLE);
                }
                isLoading = false;
                ((HomeActivity) requireActivity()).hideProgressBar();
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "No cards available", Toast.LENGTH_SHORT).show();
                }
                binding.tvSeeAll.setVisibility(View.GONE);
                isLoading = false;
                ((HomeActivity) requireActivity()).hideProgressBar();
            }
        });
    }


    private static final String TAG = "HomeFragment";

    private String formatDate(String createdDate) {
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

            Date date = inputFormat.parse(createdDate);

            assert date != null;
            return outputFormat.format(date);

        } catch (ParseException e) {
            Log.e(TAG, "Date parsing failed for input: " + createdDate, e);
            return createdDate;
        }
    }


    private void getUsernameFromDatabase(String userID) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("Users").child(userID);

        userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isAdded() || binding == null) return;

                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.getValue(String.class);
                    if (username != null) {
                        binding.username.setText(username);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Failed to fetch username: " + databaseError.getMessage());
            }
        });
    }

}

