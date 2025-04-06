package com.example.expense;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.expense.databinding.ActivityHomeBinding;
import com.example.expense.fragment.AddExpenseFragment;
import com.example.expense.fragment.ExpenseListFragment;
import com.example.expense.fragment.HomeFragment;

public class HomeActivity extends AppCompatActivity {

   public ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                LoadFragment(new HomeFragment());
            } else if (itemId == R.id.nav_add) {
                LoadFragment(new AddExpenseFragment());
            } else if (itemId == R.id.nav_detail) {
                LoadFragment(new ExpenseListFragment());
            } else {
                return false;
            }
            return true;
        });

        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);

    }
    public void LoadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
    public void showProgressBar() {
        binding.loadingBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        binding.loadingBar.setVisibility(View.GONE);
    }

}
