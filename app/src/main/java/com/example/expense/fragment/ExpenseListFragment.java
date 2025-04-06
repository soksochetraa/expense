package com.example.expense.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.expense.LogInActivity;
import com.example.expense.adapter.CardAdapter;
import com.example.expense.databinding.FragmentExpenseListBinding;
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

import java.util.List;

public class ExpenseListFragment extends Fragment {

    FragmentExpenseListBinding binding;
    CardRepository cardRepository;
    CardAdapter cardAdapter;
    private FirebaseAuth mAuth;

    boolean isLoading = false;

    public ExpenseListFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentExpenseListBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userID = user.getUid();
            getUsernameFromDatabase(userID);
        }

        binding.listAllRcv.setLayoutManager(new LinearLayoutManager(getContext()));
        cardAdapter = new CardAdapter();
        binding.listAllRcv.setAdapter(cardAdapter);

        loadAllCards();

        binding.setting.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getContext(), LogInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.setting.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getContext(), LogInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    private void loadAllCards() {
        FirebaseUser user = mAuth.getCurrentUser();
        isLoading = true;
        showProgressBar();
        if (user == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = user.getUid();
        cardRepository = new CardRepository();

        cardRepository.getAllCards(currentUserId, new IApiCallback<List<Card>>() {
            @Override
            public void onSuccess(List<Card> cards) {
                if (cards != null && !cards.isEmpty()) {
                    isLoading = false;
                    hideProgressBar();
                    cardAdapter.setCards(cards);
                } else {
                    Toast.makeText(getContext(), "No cards available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "Error loading cards: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUsernameFromDatabase(String userID) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("Users").child(userID);

        userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.getValue(String.class);

                    if (username != null) {
                        binding.username.setText(username);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllCards();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void showProgressBar() {
        binding.loadingBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        binding.loadingBar.setVisibility(View.GONE);
    }
}
