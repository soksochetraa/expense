package com.example.expense.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.expense.AddCategoryActivity;
import com.example.expense.HomeActivity;
import com.example.expense.LogInActivity;
import com.example.expense.R;
import com.example.expense.dao.CategoryDao;
import com.example.expense.dao.CategoryDatabase;
import com.example.expense.databinding.FragmentAddExpenseBinding;
import com.example.expense.model.Card;
import com.example.expense.model.Category;
import com.example.expense.repository.CardRepository;
import com.example.expense.repository.IApiCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AddExpenseFragment extends Fragment {

    private FragmentAddExpenseBinding binding;
    private FirebaseAuth mAuth;
    private CardRepository cardRepository;
    HomeActivity homeActivity;
    boolean isLoading = false;
    CategoryDao categoryDao;

    CategoryDatabase categoryDatabase;

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
        ((HomeActivity) requireActivity()).showProgressBar();

        mAuth = FirebaseAuth.getInstance();
        cardRepository = new CardRepository();

        CategoryDatabase db = CategoryDatabase.getInstance(getContext());
        categoryDao = db.categoryDao();

        categoryDao = CategoryDatabase.getInstance(requireContext()).categoryDao();

        if (categoryDao.getAllCategories().isEmpty()) {
            categoryDao.insert(new Category("Food"));
            categoryDao.insert(new Category("Transport"));
            categoryDao.insert(new Category("Shopping"));
            categoryDao.insert(new Category("Entertainment"));
        }

        loadCategories();

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
            Activity activity = getActivity();
            if (activity != null) {
                activity.finish();
            }
        });

        binding.plusIcon.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddCategoryActivity.class);
            startActivity(intent);
        });

        binding.btnAddExpense.setOnClickListener(v -> onAddExpenseClicked());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCategories();
    }

    private void onAddExpenseClicked() {
        if (isLoading) return;
        isLoading = true;
        ((HomeActivity) requireActivity()).showProgressBar();

        String amountText = binding.etAmount.getText().toString().trim();
        int selectedRadioButtonId = binding.radioGroup.getCheckedRadioButtonId();

        if (amountText.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter an amount.", Toast.LENGTH_SHORT).show();
            resetLoading();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                Toast.makeText(requireContext(), "Amount must be greater than 0.", Toast.LENGTH_SHORT).show();
                resetLoading();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid amount format.", Toast.LENGTH_SHORT).show();
            resetLoading();
            return;
        }

        if (selectedRadioButtonId == -1) {
            Toast.makeText(requireContext(), "Please select a currency.", Toast.LENGTH_SHORT).show();
            resetLoading();
            return;
        }

        RadioButton selectedRadioButton = binding.radioGroup.findViewById(selectedRadioButtonId);
        String currency = selectedRadioButton.getText().toString();
        String category = binding.spinnerCategory.getSelectedItem().toString();
        String remark = binding.etRemark.getText().toString().trim();
        String createdBy = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "unknown";

        String id = UUID.randomUUID().toString();
        Date createdDate = new Date();
        Card card = new Card(id, amount, currency, category, remark, createdBy, createdDate);

        cardRepository.createCard(card, new IApiCallback<>() {
            @Override
            public void onSuccess(Card result) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Expense added successfully!", Toast.LENGTH_SHORT).show();
                    clearInputFields();
                    if (homeActivity != null) {
                        homeActivity.binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
                    }
                }
                resetLoading();
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), errorMessage != null ? errorMessage : "Failed to add expense", Toast.LENGTH_SHORT).show();
                }
                resetLoading();
            }
        });
    }

    private void resetLoading() {
        isLoading = false;
        if (isAdded()) {
            ((HomeActivity) requireActivity()).hideProgressBar();
        }
    }

    private void clearInputFields() {
        binding.etAmount.setText("");
        binding.etRemark.setText("");
        binding.radioGroup.clearCheck();
        binding.spinnerCategory.setSelection(0);
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
                        isLoading = false;
                        ((HomeActivity) requireActivity()).hideProgressBar();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void loadCategories() {
        List<Category> categories = categoryDao.getAllCategories();
        List<String> names = new ArrayList<>();
        for (Category cat : categories) {
            names.add(cat.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(adapter);
    }
}
