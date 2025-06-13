package com.example.movies_app.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.R;

public class AdminSettingsActivity extends AppCompatActivity {
    private ImageView backButton;
    private CardView systemSettingsCard, securityCard, backupCard, maintenanceCard;
    private Switch notificationsSwitch, autoBackupSwitch, maintenanceModeSwitch;
    private EditText maxUsersEdit, sessionTimeoutEdit;
    private Button saveSettingsBtn, clearCacheBtn, exportDataBtn, resetSystemBtn;

    private AppDatabase database;
    private SharedPreferences adminPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_settings);

        database = AppDatabase.getInstance(this);
        adminPrefs = getSharedPreferences("admin_settings", MODE_PRIVATE);

        initViews();
        setupClickListeners();
        loadCurrentSettings();
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);

        // Setting cards
        systemSettingsCard = findViewById(R.id.systemSettingsCard);
        securityCard = findViewById(R.id.securityCard);
        backupCard = findViewById(R.id.backupCard);
        maintenanceCard = findViewById(R.id.maintenanceCard);

        // Switches
        notificationsSwitch = findViewById(R.id.notificationsSwitch);
        autoBackupSwitch = findViewById(R.id.autoBackupSwitch);
        maintenanceModeSwitch = findViewById(R.id.maintenanceModeSwitch);

        // Edit texts
        maxUsersEdit = findViewById(R.id.maxUsersEdit);
        sessionTimeoutEdit = findViewById(R.id.sessionTimeoutEdit);

        // Buttons
        saveSettingsBtn = findViewById(R.id.saveSettingsBtn);
        clearCacheBtn = findViewById(R.id.clearCacheBtn);
        exportDataBtn = findViewById(R.id.exportDataBtn);
        resetSystemBtn = findViewById(R.id.resetSystemBtn);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        saveSettingsBtn.setOnClickListener(v -> saveSettings());
        clearCacheBtn.setOnClickListener(v -> clearCache());
        exportDataBtn.setOnClickListener(v -> exportData());
        resetSystemBtn.setOnClickListener(v -> showResetConfirmation());

        // Card clicks for detailed settings
        systemSettingsCard.setOnClickListener(v -> showSystemSettingsDetail());
        securityCard.setOnClickListener(v -> showSecuritySettingsDetail());
        backupCard.setOnClickListener(v -> showBackupSettingsDetail());
        maintenanceCard.setOnClickListener(v -> showMaintenanceSettingsDetail());
    }

    private void loadCurrentSettings() {
        // Load current settings from SharedPreferences
        notificationsSwitch.setChecked(adminPrefs.getBoolean("notifications_enabled", true));
        autoBackupSwitch.setChecked(adminPrefs.getBoolean("auto_backup_enabled", false));
        maintenanceModeSwitch.setChecked(adminPrefs.getBoolean("maintenance_mode", false));

        maxUsersEdit.setText(String.valueOf(adminPrefs.getInt("max_users", 1000)));
        sessionTimeoutEdit.setText(String.valueOf(adminPrefs.getInt("session_timeout", 30)));
    }

    private void saveSettings() {
        try {
            SharedPreferences.Editor editor = adminPrefs.edit();

            // Save switch settings
            editor.putBoolean("notifications_enabled", notificationsSwitch.isChecked());
            editor.putBoolean("auto_backup_enabled", autoBackupSwitch.isChecked());
            editor.putBoolean("maintenance_mode", maintenanceModeSwitch.isChecked());

            // Save numeric settings
            int maxUsers = Integer.parseInt(maxUsersEdit.getText().toString());
            int sessionTimeout = Integer.parseInt(sessionTimeoutEdit.getText().toString());

            editor.putInt("max_users", maxUsers);
            editor.putInt("session_timeout", sessionTimeout);

            editor.apply();

            Toast.makeText(this, "Cài đặt đã được lưu!", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi lưu cài đặt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void clearCache() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa Cache")
                .setMessage("Bạn có chắc chắn muốn xóa tất cả cache?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // TODO: Implement cache clearing
                    Toast.makeText(this, "Đã xóa cache hệ thống", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void exportData() {
        new Thread(() -> {
            try {
                // TODO: Implement data export functionality
                runOnUiThread(() ->
                        Toast.makeText(this, "Đang xuất dữ liệu...", Toast.LENGTH_SHORT).show()
                );

                // Simulate export process
                Thread.sleep(3000);

                runOnUiThread(() ->
                        Toast.makeText(this, "Dữ liệu đã được xuất thành công!", Toast.LENGTH_SHORT).show()
                );

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi xuất dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void showResetConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Khôi phục hệ thống")
                .setMessage("CẢNH BÁO: Thao tác này sẽ xóa toàn bộ dữ liệu và khôi phục hệ thống về trạng thái ban đầu. Bạn có chắc chắn?")
                .setPositiveButton("Khôi phục", (dialog, which) -> resetSystem())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void resetSystem() {
        new Thread(() -> {
            try {
                // TODO: Implement system reset
                runOnUiThread(() ->
                        Toast.makeText(this, "Đang khôi phục hệ thống...", Toast.LENGTH_LONG).show()
                );

                // Simulate reset process
                Thread.sleep(5000);

                runOnUiThread(() ->
                        Toast.makeText(this, "Hệ thống đã được khôi phục!", Toast.LENGTH_SHORT).show()
                );

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi khôi phục hệ thống: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void showSystemSettingsDetail() {
        Toast.makeText(this, "Cài đặt hệ thống chi tiết", Toast.LENGTH_SHORT).show();
    }

    private void showSecuritySettingsDetail() {
        Toast.makeText(this, "Cài đặt bảo mật chi tiết", Toast.LENGTH_SHORT).show();
    }

    private void showBackupSettingsDetail() {
        Toast.makeText(this, "Cài đặt sao lưu chi tiết", Toast.LENGTH_SHORT).show();
    }

    private void showMaintenanceSettingsDetail() {
        Toast.makeText(this, "Cài đặt bảo trì chi tiết", Toast.LENGTH_SHORT).show();
    }
}