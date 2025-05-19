package com.example.expense.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;

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
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AddExpenseFragment extends Fragment {

    private FragmentAddExpenseBinding binding;
    private FirebaseAuth mAuth;
    private CardRepository cardRepository;
    private HomeActivity homeActivity;
    private boolean isLoading = false;
    private CategoryDao categoryDao;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};
    private Uri imageUrl;

    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    private InterstitialAd mInterstitialAd;

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
        categoryDao = CategoryDatabase.getInstance(requireContext()).categoryDao();

        if (categoryDao.getAllCategories().isEmpty()) {
            categoryDao.insert(new Category("Food"));
            categoryDao.insert(new Category("Transport"));
            categoryDao.insert(new Category("Shopping"));
            categoryDao.insert(new Category("Entertainment"));
        }

        homeActivity = (HomeActivity) getActivity();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            getUsernameFromDatabase(user.getUid());
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

        binding.btnAddImageBefore.setOnClickListener(v -> {
            if (hasPermissions()) {
                showImagePickerDialog();
            } else {
                permissionLauncher.launch(REQUIRED_PERMISSIONS);
            }
        });

        binding.btnCancelImage.setOnClickListener(v -> {
            binding.imagePreviewContainer.setVisibility(View.GONE);
            binding.btnAddImageBefore.setVisibility(View.VISIBLE);
            imageUrl = null;
        });

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean granted = true;
                    for (Boolean b : result.values()) {
                        if (!b) {
                            granted = false;
                            break;
                        }
                    }
                    if (granted) {
                        showImagePickerDialog();
                    } else {
                        showPermissionSettingsDialog();
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && imageUrl != null) {
                        binding.imagePreview.setImageURI(imageUrl);
                        binding.imagePreviewContainer.setVisibility(View.VISIBLE);
                        binding.btnAddImageBefore.setVisibility(View.GONE);
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUrl = result.getData().getData();
                        binding.imagePreview.setImageURI(imageUrl);
                        binding.imagePreviewContainer.setVisibility(View.VISIBLE);
                        binding.btnAddImageBefore.setVisibility(View.GONE);
                    }
                });

        loadCategories();
        loadInterstitialAd();
    }

    private void onAddExpenseClicked() {
        if (isLoading) return;

        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    mInterstitialAd = null;
                    loadInterstitialAd();
                    continueAddExpense();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    mInterstitialAd = null;
                    continueAddExpense();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    mInterstitialAd = null;
                }
            });
            mInterstitialAd.show(requireActivity());
        } else {
            continueAddExpense();
        }
    }

    private void continueAddExpense() {
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
        String imageUrlString = imageUrl != null ? imageUrl.toString() : "";

        Card card = new Card(id, amount, currency, category, remark, createdBy, createdDate, imageUrlString);

        if (imageUrl != null) {
            String userId = mAuth.getCurrentUser().getUid();
            StorageReference storageRef = FirebaseStorage.getInstance()
                    .getReference("receipts/" + userId + "/" + UUID.randomUUID());

            storageRef.putFile(imageUrl)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        card.setImageUrl(uri.toString());
                        uploadCard(card);
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        resetLoading();
                    });
        } else {
            uploadCard(card);
        }
    }

    private void uploadCard(Card card) {
        cardRepository.createCard(card, new IApiCallback<>() {
            @Override
            public void onSuccess(Card result) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Expense added successfully!", Toast.LENGTH_SHORT).show();
                    if (homeActivity != null) {
                        if (result.getCurrency().equals("USD") && result.getAmount() > 100) {
                            homeActivity.showBasicNotification(result.getRemark());
                        } else if (result.getCurrency().equals("KHR") && result.getAmount() > 400000) {
                            homeActivity.showBasicNotification(result.getRemark());
                        }
                    }
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
        binding.imagePreview.setVisibility(View.GONE);
        binding.btnAddImageBefore.setVisibility(View.VISIBLE);
        imageUrl = null;
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
        int selectedPosition = binding.spinnerCategory.getSelectedItemPosition();

        List<Category> categories = categoryDao.getAllCategories();
        List<String> names = new ArrayList<>();
        for (Category cat : categories) {
            names.add(cat.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(adapter);

        if (selectedPosition >= 0 && selectedPosition < names.size()) {
            binding.spinnerCategory.setSelection(selectedPosition);
        } else {
            binding.spinnerCategory.setSelection(0);
        }
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        imageUrl = requireActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUrl);
        cameraLauncher.launch(cameraIntent);
    }

    private boolean hasPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void showPermissionSettingsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Permission Required")
                .setMessage("Camera and storage access are needed. Please grant them manually:\n\n" +
                        "1. Open Settings\n\n" +
                        "2. Tap Apps\n\n" +
                        "3. Find this app\n\n" +
                        "4. Tap Permissions\n\n" +
                        "5. Enable Camera and Storage")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(
                requireContext(),
                "ca-app-pub-5339112118654088/7859283453",
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        mInterstitialAd = null;
                    }
                });
    }

}
