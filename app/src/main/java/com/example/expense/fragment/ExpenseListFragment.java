package com.example.expense.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expense.HomeActivity;
import com.example.expense.LogInActivity;
import com.example.expense.R;
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
    HomeActivity homeActivity;

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

        homeActivity = (HomeActivity) getActivity();
        isLoading = false;
        ((HomeActivity) requireActivity()).hideProgressBar();

        binding.error.setVisibility(View.GONE);
        binding.listAllRcv.setLayoutManager(new LinearLayoutManager(getContext()));
        cardAdapter = new CardAdapter();
        binding.listAllRcv.setAdapter(cardAdapter);

        loadAllCards();

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

        binding.btnAddExpenseNow.setOnClickListener(v -> {
            if (homeActivity != null) {
                homeActivity.binding.bottomNavigation.setSelectedItemId(R.id.nav_add);
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.listAllRcv);

        return binding.getRoot();
    }

    private void loadAllCards() {
        FirebaseUser user = mAuth.getCurrentUser();
        isLoading = true;
        ((HomeActivity) requireActivity()).showProgressBar();
        if (user == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = user.getUid();
        cardRepository = new CardRepository();

        cardRepository.getAllCards(currentUserId, new IApiCallback<>() {
            @Override
            public void onSuccess(List<Card> cards) {
                if (cards != null && !cards.isEmpty()) {
                    binding.error.setVisibility(View.GONE);
                    cardAdapter.setCards(cards);
                } else {
                    binding.error.setVisibility(View.VISIBLE);
                }
                isLoading = false;
                ((HomeActivity) requireActivity()).hideProgressBar();
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

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            Card card = cardAdapter.getCardAt(position);

            if (card.getId() == null) {
                Toast.makeText(viewHolder.itemView.getContext(), "Card ID is missing", Toast.LENGTH_SHORT).show();
                cardAdapter.notifyItemChanged(position);
                return;
            }

            ((HomeActivity) requireActivity()).showProgressBar();

            cardRepository.deleteCard(card.getId(), new IApiCallback<>() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(viewHolder.itemView.getContext(), message, Toast.LENGTH_SHORT).show();
                    loadAllCards();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(viewHolder.itemView.getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    cardAdapter.notifyItemChanged(position);
                }
            });
        }

        @Override
        public void onChildDraw(@NonNull Canvas c,
                                @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
            View foregroundView = ((CardAdapter.ViewHolder) viewHolder).binding.foregroundView;
            foregroundView.setTranslationX(dX);
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };
}
