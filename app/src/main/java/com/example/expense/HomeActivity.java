package com.example.expense;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.expense.databinding.ActivityHomeBinding;
import com.example.expense.fragment.AddExpenseFragment;
import com.example.expense.fragment.ExpenseListFragment;
import com.example.expense.fragment.HomeFragment;
import com.example.expense.fragment.SettingFragment;
import com.google.firebase.messaging.FirebaseMessaging;

public class HomeActivity extends BaseActivity {

    private static final String CHANNEL_ID = "expense";
    public ActivityHomeBinding binding;

    private static final int REQUEST_CODE_NOTIFICATION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        createNotificationChannel();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                LoadFragment(new HomeFragment());
            } else if (itemId == R.id.nav_add) {
                LoadFragment(new AddExpenseFragment());
            } else if (itemId == R.id.nav_detail) {
                LoadFragment(new ExpenseListFragment());
            } else if (itemId == R.id.nav_setting) {
                LoadFragment(new SettingFragment());
            } else {
                return false;
            }
            return true;
        });

        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        } else {
            binding.bottomNavigation.setSelectedItemId(savedInstanceState.getInt("selectedItemId"));
        }

        checkAndRequestNotificationPermission();
        initFcmAndSubscribeTopic();

    }

    public void LoadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION}, REQUEST_CODE_NOTIFICATION);
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "expense";
            String description = "Channel for Task Notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showBasicNotification(String remark) {
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.custom_notification);
        notificationLayout.setTextViewText(R.id.notification_title, "Budget Warning!");
        notificationLayout.setTextViewText(R.id.notification_text, "You have exceeded your budget for \"" + remark + "\"");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.expense)
                .setCustomContentView(notificationLayout)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true);

        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat manager = NotificationManagerCompat.from(this);
            manager.notify(1, builder.build());
        }
    }

    private void initFcmAndSubscribeTopic() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.d("FCM", "Device Token: " + token);
                });

        // Subscribe to topic for broadcast
        FirebaseMessaging.getInstance().subscribeToTopic("all")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FCM", "Subscribed to topic: all");
                    } else {
                        Log.d("FCM", "Topic subscription failed.");
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);

    }

    public void showProgressBar() {
        binding.loadingBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        binding.loadingBar.setVisibility(View.GONE);
    }
}
