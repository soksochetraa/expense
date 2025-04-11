package com.example.expense;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.expense.dao.CategoryDao;
import com.example.expense.dao.CategoryDatabase;
import com.example.expense.databinding.ActivityAddCategoryBinding;
import com.example.expense.model.Category;

public class AddCategoryActivity extends AppCompatActivity {

    ActivityAddCategoryBinding binding;
    CategoryDao categoryDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        binding = ActivityAddCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        categoryDao = CategoryDatabase.getInstance(this).categoryDao();

        binding.backButton.setOnClickListener(view -> onBackPressed());

        binding.btnAddCategory.setOnClickListener(view -> {
            String name = binding.etAddCategory.getText().toString().trim();
            if (!name.isEmpty()) {
                executorService.execute(() -> {
                    categoryDao.insert(new Category(name));
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    });
                });
            } else {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
