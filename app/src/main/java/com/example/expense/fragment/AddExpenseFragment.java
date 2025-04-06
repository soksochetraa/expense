package com.example.expense.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.expense.HomeActivity;
import com.example.expense.LogInActivity;
import com.example.expense.R;
import com.example.expense.databinding.FragmentAddExpenseBinding;
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

import java.util.Date;
import java.util.UUID;

public class AddExpenseFragment extends Fragment {

    private FragmentAddExpenseBinding binding;
    private FirebaseAuth mAuth;
    private CardRepository cardRepository;
    HomeActivity homeActivity;
    boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddExpenseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isLoading = true;
        showProgressBar();
        mAuth = FirebaseAuth.getInstance();
        cardRepository = new CardRepository();
        homeActivity = (HomeActivity) getActivity();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userID = user.getUid();
            getUsernameFromDatabase(userID);

        }

        binding.setting.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getContext(), LogInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        binding.btnAddExpense.setOnClickListener(v -> onAddExpenseClicked());
    }

    private void onAddExpenseClicked() {
        isLoading = true;
        showProgressBar();
        String amount = binding.etAmount.getText().toString();
        int selectedRadioButtonId = binding.radioGroup.getCheckedRadioButtonId();

        if (amount.trim().isEmpty() || selectedRadioButtonId == -1) {
            Toast.makeText(requireContext(), "Invalid input: amount empty or no currency selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = binding.radioGroup.findViewById(selectedRadioButtonId);
        String currency = selectedRadioButton.getText().toString();

        String category = binding.spinnerCategory.getSelectedItem().toString();
        String remark = binding.etRemark.getText().toString();
        String createdBy = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "null";

        Date createdDate = new Date();
        String id = UUID.randomUUID().toString();
        Card card = new Card(id, Double.parseDouble(amount), currency, category, remark, createdBy, createdDate);

        cardRepository.createCard(card, new IApiCallback<Card>() {
            @Override
            public void onSuccess(Card result) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Card created successfully", Toast.LENGTH_SHORT).show();
                    isLoading = false;
                    hideProgressBar();
                    if (homeActivity != null) {
                        homeActivity.binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(requireContext(), errorMessage != null ? errorMessage : "Unknown error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getUsernameFromDatabase(String userID) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("Users").child(userID);

        Log.d("FirebaseDebug", "Fetching data for user: " + userID);

        userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.getValue(String.class);

                    if (username != null) {
                        binding.username.setText(username);
                        isLoading = false;
                        hideProgressBar();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void showProgressBar() {
        binding.loadingBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        binding.loadingBar.setVisibility(View.GONE);
    }
}
