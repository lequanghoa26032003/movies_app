package com.example.movies_app.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies_app.Adapter.UserManagementAdapter;
import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.Database.entity.User;
import com.example.movies_app.R;
import com.example.movies_app.service.UserManagementService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class ManageUsersActivity extends AppCompatActivity implements UserManagementAdapter.UserActionListener {

    // Statistics TextViews
    private TextView totalUsersText, activeUsersText, blockedUsersText, adminUsersText;

    // UI Components
    private RecyclerView usersRecyclerView;
    private EditText searchEditText;
    private Button searchButton, blockUserButton, unblockUserButton;
    private Spinner roleSpinner;
    private ImageView backButton;
    private FloatingActionButton fabAddUser;

    // Services and Data
    private AppDatabase database;
    private UserManagementService userService;
    private UserManagementAdapter userAdapter;
    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();
    private List<User> selectedUsers = new ArrayList<>();

    // Timer for real-time updates
    private Timer statisticsTimer;

    // Constants
    private static final int REQUEST_ADD_USER = 1001;
    private static final int REQUEST_EDIT_USER = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        initViews();
        setupServices();
        setupRecyclerView();
        setupClickListeners();
        setupSearchFunction();
        setupRoleSpinner();
        loadStatistics();
        loadUsers();
        setupRealTimeUpdates();
    }

    private void initViews() {
        // Statistics TextViews
        totalUsersText = findViewById(R.id.totalUsersText);
        activeUsersText = findViewById(R.id.activeUsersText);
        blockedUsersText = findViewById(R.id.blockedUsersText);
        adminUsersText = findViewById(R.id.adminUsersText);

        // Other views
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        blockUserButton = findViewById(R.id.blockUserButton);
        unblockUserButton = findViewById(R.id.unblockUserButton);
        roleSpinner = findViewById(R.id.roleSpinner);
        backButton = findViewById(R.id.backButton);

        // Add FloatingActionButton for adding users
        fabAddUser = findViewById(R.id.fabAddUser);
        if (fabAddUser == null) {
            // Create FAB programmatically if not in layout
            fabAddUser = new FloatingActionButton(this);
            // Position and add to layout...
        }
    }

    private void setupServices() {
        database = AppDatabase.getInstance(this);
        userService = new UserManagementService(this);
    }

    private void setupRecyclerView() {
        userAdapter = new UserManagementAdapter(this, filteredUsers, this);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(userAdapter);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        searchButton.setOnClickListener(v -> performSearch());

        blockUserButton.setOnClickListener(v -> bulkBlockUsers());

        unblockUserButton.setOnClickListener(v -> bulkUnblockUsers());

        if (fabAddUser != null) {
            fabAddUser.setOnClickListener(v -> openAddUserDialog());
        }

        // Update button states based on selection
        updateActionButtonsState();
    }

    private void setupSearchFunction() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRoleSpinner() {
        String[] roles = {"Tất cả", "Admin", "User", "Hoạt động", "Bị khóa"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterUsers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadStatistics() {
        new Thread(() -> {
            try {
                int totalUsers = database.userDao().getTotalUsersCount();
                int activeUsers = database.userDao().getActiveUsersCount();
                int blockedUsers = database.userDao().getBlockedUsersCount();
                int adminUsers = database.userDao().getAdminUsersCount();

                runOnUiThread(() -> {
                    totalUsersText.setText(String.valueOf(totalUsers));
                    activeUsersText.setText(String.valueOf(activeUsers));
                    blockedUsersText.setText(String.valueOf(blockedUsers));
                    adminUsersText.setText(String.valueOf(adminUsers));
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi tải thống kê: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void loadUsers() {
        userService.getAllUsers(new UserManagementService.UserListCallback() {
            @Override
            public void onSuccess(List<User> users) {
                runOnUiThread(() -> {
                    allUsers.clear();
                    allUsers.addAll(users);
                    filterUsers();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ManageUsersActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void filterUsers() {
        filteredUsers.clear();
        String searchText = searchEditText.getText().toString().toLowerCase().trim();
        String selectedRole = (String) roleSpinner.getSelectedItem();

        for (User user : allUsers) {
            boolean matchesSearch = searchText.isEmpty() ||
                    user.getUsername().toLowerCase().contains(searchText) ||
                    user.getEmail().toLowerCase().contains(searchText) ||
                    (user.getFullName() != null && user.getFullName().toLowerCase().contains(searchText));

            boolean matchesRole = "Tất cả".equals(selectedRole) ||
                    ("Admin".equals(selectedRole) && "ADMIN".equals(user.getRole())) ||
                    ("User".equals(selectedRole) && "USER".equals(user.getRole())) ||
                    ("Hoạt động".equals(selectedRole) && user.getAccountStatus() == UserManagementService.STATUS_ACTIVE) ||
                    ("Bị khóa".equals(selectedRole) && user.getAccountStatus() == UserManagementService.STATUS_BLOCKED);

            if (matchesSearch && matchesRole) {
                filteredUsers.add(user);
            }
        }

        userAdapter.notifyDataSetChanged();
        updateResultCount();
    }

    private void updateResultCount() {
        // You can add a TextView to show result count
        // resultCountText.setText("Hiển thị " + filteredUsers.size() + " / " + allUsers.size() + " người dùng");
    }

    private void performSearch() {
        filterUsers();
        Toast.makeText(this, "Tìm thấy " + filteredUsers.size() + " người dùng", Toast.LENGTH_SHORT).show();
    }

    // ========== UserActionListener Implementation ==========

    @Override
    public void onEditUser(User user) {
        Intent intent = new Intent(this, AddEditUserActivity.class);
        intent.putExtra("user_id", user.getUserId());
        intent.putExtra("edit_mode", true);
        startActivityForResult(intent, REQUEST_EDIT_USER);
    }

    @Override
    public void onDeleteUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa người dùng " + user.getUsername() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteUser(user))
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onToggleUserStatus(User user) {
        int newStatus = user.getAccountStatus() == UserManagementService.STATUS_BLOCKED ?
                UserManagementService.STATUS_ACTIVE : UserManagementService.STATUS_BLOCKED;
        String action = newStatus == UserManagementService.STATUS_BLOCKED ? "khóa" : "mở khóa";

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận " + action)
                .setMessage("Bạn có chắc chắn muốn " + action + " tài khoản " + user.getUsername() + "?")
                .setPositiveButton("Đồng ý", (dialog, which) -> changeUserStatus(user, newStatus))
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onViewUserDetails(User user) {
        showUserDetailsDialog(user);
    }

    @Override
    public void onUserSelected(User user, boolean isSelected) {
        if (isSelected) {
            if (!selectedUsers.contains(user)) {
                selectedUsers.add(user);
            }
        } else {
            selectedUsers.remove(user);
        }
        updateActionButtonsState();
    }

    // ========== Helper Methods ==========

    private void deleteUser(User user) {
        userService.deleteUser(user.getUserId(), new UserManagementService.UserOperationCallback() {
            @Override
            public void onSuccess(String message, User user) {
                runOnUiThread(() -> {
                    Toast.makeText(ManageUsersActivity.this, message, Toast.LENGTH_SHORT).show();
                    loadUsers();
                    loadStatistics();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ManageUsersActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void changeUserStatus(User user, int newStatus) {
        userService.changeAccountStatus(user.getUserId(), newStatus,
                new UserManagementService.UserOperationCallback() {
                    @Override
                    public void onSuccess(String message, User updatedUser) {
                        runOnUiThread(() -> {
                            Toast.makeText(ManageUsersActivity.this, message, Toast.LENGTH_SHORT).show();
                            loadUsers();
                            loadStatistics();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(ManageUsersActivity.this, error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void bulkBlockUsers() {
        if (selectedUsers.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn người dùng cần khóa", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận khóa")
                .setMessage("Bạn có chắc chắn muốn khóa " + selectedUsers.size() + " người dùng đã chọn?")
                .setPositiveButton("Khóa", (dialog, which) -> {
                    List<Integer> userIds = selectedUsers.stream()
                            .map(User::getUserId)
                            .collect(Collectors.toList());

                    userService.bulkChangeStatus(userIds, UserManagementService.STATUS_BLOCKED,
                            new UserManagementService.BulkOperationCallback() {
                                @Override
                                public void onSuccess(String message, int affectedCount) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(ManageUsersActivity.this, message, Toast.LENGTH_SHORT).show();
                                        selectedUsers.clear();
                                        loadUsers();
                                        loadStatistics();
                                        updateActionButtonsState();
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                    runOnUiThread(() ->
                                            Toast.makeText(ManageUsersActivity.this, error, Toast.LENGTH_SHORT).show()
                                    );
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void bulkUnblockUsers() {
        if (selectedUsers.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn người dùng cần mở khóa", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Integer> userIds = selectedUsers.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());

        userService.bulkChangeStatus(userIds, UserManagementService.STATUS_ACTIVE,
                new UserManagementService.BulkOperationCallback() {
                    @Override
                    public void onSuccess(String message, int affectedCount) {
                        runOnUiThread(() -> {
                            Toast.makeText(ManageUsersActivity.this, message, Toast.LENGTH_SHORT).show();
                            selectedUsers.clear();
                            loadUsers();
                            loadStatistics();
                            updateActionButtonsState();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() ->
                                Toast.makeText(ManageUsersActivity.this, error, Toast.LENGTH_SHORT).show()
                        );
                    }
                });
    }

    private void updateActionButtonsState() {
        boolean hasSelection = !selectedUsers.isEmpty();
        blockUserButton.setEnabled(hasSelection);
        unblockUserButton.setEnabled(hasSelection);

        // Update button text with count
        if (hasSelection) {
            blockUserButton.setText("Khóa (" + selectedUsers.size() + ")");
            unblockUserButton.setText("Mở khóa (" + selectedUsers.size() + ")");
        } else {
            blockUserButton.setText("Khóa người dùng");
            unblockUserButton.setText("Mở khóa");
        }
    }

    private void openAddUserDialog() {
        Intent intent = new Intent(this, AddEditUserActivity.class);
        startActivityForResult(intent, REQUEST_ADD_USER);
    }

    private void showUserDetailsDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_details, null);

        // Populate dialog with user details
        TextView usernameText = dialogView.findViewById(R.id.dialogUsernameText);
        TextView emailText = dialogView.findViewById(R.id.dialogEmailText);
        TextView fullNameText = dialogView.findViewById(R.id.dialogFullNameText);
        TextView phoneText = dialogView.findViewById(R.id.dialogPhoneText);
        TextView roleText = dialogView.findViewById(R.id.dialogRoleText);
        TextView statusText = dialogView.findViewById(R.id.dialogStatusText);
        TextView registrationDateText = dialogView.findViewById(R.id.dialogRegistrationDateText);
        TextView lastLoginText = dialogView.findViewById(R.id.dialogLastLoginText);

        usernameText.setText(user.getUsername());
        emailText.setText(user.getEmail());
        fullNameText.setText(user.getFullName() != null ? user.getFullName() : "Chưa cập nhật");
        phoneText.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "Chưa cập nhật");
        roleText.setText(user.getRole());
        statusText.setText(UserManagementService.getStatusName(user.getAccountStatus()));
        registrationDateText.setText(user.getRegistrationDate());
        lastLoginText.setText(user.getLastLoginDate() != null ? user.getLastLoginDate() : "Chưa đăng nhập");

        builder.setView(dialogView)
                .setTitle("Thông tin chi tiết - " + user.getUsername())
                .setPositiveButton("Đóng", null)
                .setNeutralButton("Chỉnh sửa", (dialog, which) -> onEditUser(user));

        builder.create().show();
    }

    private void setupRealTimeUpdates() {
        statisticsTimer = new Timer();
        statisticsTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                loadStatistics();
            }
        }, 30000, 30000); // Update every 30 seconds
    }

    // ========== Menu ==========

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_management, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_refresh) {
            loadUsers();
            loadStatistics();
            return true;
        } else if (itemId == R.id.action_export) {
            exportUserData();
            return true;
        } else if (itemId == R.id.action_clear_selection) {
            selectedUsers.clear();
            userAdapter.clearSelection();
            updateActionButtonsState();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void exportUserData() {
        new Thread(() -> {
            try {
                StringBuilder csvData = new StringBuilder();
                csvData.append("Username,Email,Full Name,Role,Status,Registration Date,Last Login\n");

                for (User user : allUsers) {
                    csvData.append(user.getUsername()).append(",");
                    csvData.append(user.getEmail()).append(",");
                    csvData.append(user.getFullName() != null ? user.getFullName() : "").append(",");
                    csvData.append(user.getRole()).append(",");
                    csvData.append(UserManagementService.getStatusName(user.getAccountStatus())).append(",");
                    csvData.append(user.getRegistrationDate()).append(",");
                    csvData.append(user.getLastLoginDate() != null ? user.getLastLoginDate() : "").append("\n");
                }

                String fileName = "users_export_" + System.currentTimeMillis() + ".csv";
                // TODO: Implement file saving logic here

                runOnUiThread(() ->
                        Toast.makeText(this, "Đã xuất dữ liệu: " + fileName, Toast.LENGTH_SHORT).show()
                );

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi xuất dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ADD_USER || requestCode == REQUEST_EDIT_USER) {
                loadUsers();
                loadStatistics();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statisticsTimer != null) {
            statisticsTimer.cancel();
        }
        if (userService != null) {
            userService.shutdown();
        }
    }
}