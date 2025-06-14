package com.example.movies_app.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.movies_app.Database.entity.User;
import com.example.movies_app.R;
import com.example.movies_app.service.UserManagementService;
import com.google.android.material.card.MaterialCardView;

public class UserDetailActivity extends AppCompatActivity {
    private int currentAdminId = -1;
    private ImageView imageViewAvatar;
    private TextView textViewUsername, textViewEmail, textViewFullName, textViewPhone;
    private TextView textViewRole, textViewStatus, textViewRegistrationDate, textViewLastLogin;
    private MaterialCardView cardViewStatus;
    
    private UserManagementService userService;
    private User currentUser;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        
        initViews();
        setupToolbar();
        getCurrentAdminId();
        userService = new UserManagementService(this);
        userId = getIntent().getIntExtra("user_id", -1);
        
        if (userId != -1) {
            loadUserDetail();
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void initViews() {
        imageViewAvatar = findViewById(R.id.imageViewAvatar);
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewFullName = findViewById(R.id.textViewFullName);
        textViewPhone = findViewById(R.id.textViewPhone);
        textViewRole = findViewById(R.id.textViewRole);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewRegistrationDate = findViewById(R.id.textViewRegistrationDate);
        textViewLastLogin = findViewById(R.id.textViewLastLogin);
        cardViewStatus = findViewById(R.id.cardViewStatus);
    }
    private void getCurrentAdminId() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        currentAdminId = prefs.getInt("user_id", -1);
    }
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết người dùng");
        }
    }
    
    private void loadUserDetail() {
        userService.getUserById(userId, new UserManagementService.UserOperationCallback() {
            @Override
            public void onSuccess(String message, User user) {
                runOnUiThread(() -> {
                    currentUser = user;
                    displayUserInfo(user);
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(UserDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
    
    private void displayUserInfo(User user) {
        // Basic info
        textViewUsername.setText(user.getUsername());
        textViewEmail.setText(user.getEmail());
        textViewFullName.setText(user.getFullName() != null ? user.getFullName() : "Chưa cập nhật");
        textViewPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "Chưa cập nhật");
        textViewRole.setText(user.getRole());
        textViewRegistrationDate.setText(user.getRegistrationDate());
        
        // Last login
        String lastLogin = user.getLastLoginDate();
        textViewLastLogin.setText(lastLogin != null ? lastLogin : "Chưa đăng nhập");
        
        // Status
        setupStatusDisplay(user);
        
        // Avatar
        setupAvatar(user);
    }
    
    private void setupStatusDisplay(User user) {
        int status = user.getAccountStatus();
        String statusText = UserManagementService.getStatusName(status);
        textViewStatus.setText(statusText);
        
        // Set status color
        int colorRes;
        switch (status) {
            case UserManagementService.STATUS_ACTIVE:
                colorRes = R.color.status_active;
                break;
            case UserManagementService.STATUS_BLOCKED:
                colorRes = R.color.status_blocked;
                break;
            case UserManagementService.STATUS_INACTIVE:
                colorRes = R.color.status_inactive;
                break;
            default:
                colorRes = R.color.status_inactive;
                break;
        }
        
        int color = ContextCompat.getColor(this, colorRes);
        textViewStatus.setTextColor(color);
        cardViewStatus.setCardBackgroundColor(ContextCompat.getColor(this, colorRes + 0x10000000)); // Add transparency
    }
    
    private void setupAvatar(User user) {
        String avatarUrl = user.getAvatarUrl();
        
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_user_placeholder)
                .error(R.drawable.ic_user_placeholder)
                .circleCrop();
        
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .apply(options)
                    .into(imageViewAvatar);
        } else {
            imageViewAvatar.setImageResource(R.drawable.ic_user_placeholder);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_detail, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_edit) {
            editUser();
            return true;
        } else if (itemId == R.id.action_toggle_status) {
            toggleUserStatus();
            return true;
        } else if (itemId == R.id.action_delete) {
            deleteUser();
            return true;
        } else if (itemId == R.id.action_refresh) {
            loadUserDetail();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void editUser() {
        Intent intent = new Intent(this, AddEditUserActivity.class);
        intent.putExtra("user_id", userId);
        intent.putExtra("edit_mode", true);
        startActivityForResult(intent, 1001);
    }
    
    private void toggleUserStatus() {
        if (currentUser == null) return;
        
        int newStatus;
        String action;
        
        if (currentUser.getAccountStatus() == UserManagementService.STATUS_BLOCKED) {
            newStatus = UserManagementService.STATUS_ACTIVE;
            action = "mở khóa";
        } else {
            newStatus = UserManagementService.STATUS_BLOCKED;
            action = "khóa";
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận " + action)
                .setMessage("Bạn có chắc chắn muốn " + action + " tài khoản " + currentUser.getUsername() + "?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    changeUserStatus(newStatus);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private void changeUserStatus(int newStatus) {
        userService.changeAccountStatus(userId, newStatus,currentAdminId, new UserManagementService.UserOperationCallback() {
            @Override
            public void onSuccess(String message, User user) {
                runOnUiThread(() -> {
                    Toast.makeText(UserDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    currentUser = user;
                    setupStatusDisplay(user);
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(UserDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void deleteUser() {
        if (currentUser == null) return;
        
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa người dùng " + currentUser.getUsername() + "?\n\nHành động này không thể hoàn tác!")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    performDeleteUser();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private void performDeleteUser() {
        userService.deleteUser(userId, new UserManagementService.UserOperationCallback() {
            @Override
            public void onSuccess(String message, User user) {
                runOnUiThread(() -> {
                    Toast.makeText(UserDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(UserDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK && requestCode == 1001) {
            // User was edited, reload data
            loadUserDetail();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userService != null) {
            userService.shutdown();
        }
    }
}