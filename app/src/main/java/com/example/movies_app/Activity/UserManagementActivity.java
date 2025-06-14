package com.example.movies_app.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies_app.Adapter.UserManagementAdapter;
import com.example.movies_app.Database.entity.User;
import com.example.movies_app.R;
import com.example.movies_app.service.UserManagementService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends AppCompatActivity implements UserManagementAdapter.UserActionListener {
    
    private RecyclerView recyclerViewUsers;
    private UserManagementAdapter userAdapter;
    private UserManagementService userService;
    private EditText editTextSearch;
    private FloatingActionButton fabAddUser;
    
    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();
    
    private static final int REQUEST_ADD_USER = 1001;
    private static final int REQUEST_EDIT_USER = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearchFunction();
        
        userService = new UserManagementService(this);
        loadUsers();
    }
    
    private void initViews() {
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        editTextSearch = findViewById(R.id.editTextSearch);
        fabAddUser = findViewById(R.id.fabAddUser);
        
        fabAddUser.setOnClickListener(v -> openAddUserDialog());
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý người dùng");
        }
    }
    
    private void setupRecyclerView() {
        userAdapter = new UserManagementAdapter(this, filteredUsers, this);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userAdapter);
    }
    
    private void setupSearchFunction() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void loadUsers() {
        userService.getAllUsers(new UserManagementService.UserListCallback() {
            @Override
            public void onSuccess(List<User> users) {
                runOnUiThread(() -> {
                    allUsers.clear();
                    allUsers.addAll(users);
                    filterUsers(editTextSearch.getText().toString());
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(UserManagementActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void filterUsers(String searchText) {
        filteredUsers.clear();
        
        if (searchText.isEmpty()) {
            filteredUsers.addAll(allUsers);
        } else {
            String searchLower = searchText.toLowerCase();
            for (User user : allUsers) {
                if (user.getUsername().toLowerCase().contains(searchLower) ||
                    user.getEmail().toLowerCase().contains(searchLower) ||
                    (user.getFullName() != null && user.getFullName().toLowerCase().contains(searchLower))) {
                    filteredUsers.add(user);
                }
            }
        }
        
        userAdapter.notifyDataSetChanged();
    }
    
    private void openAddUserDialog() {
        Intent intent = new Intent(this, AddEditUserActivity.class);
        startActivityForResult(intent, REQUEST_ADD_USER);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ADD_USER || requestCode == REQUEST_EDIT_USER) {
                loadUsers(); // Reload danh sách người dùng
            }
        }
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
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteUser(user);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    @Override
    public void onToggleUserStatus(User user) {
        int newStatus;
        String action;
        
        if (user.getAccountStatus() == UserManagementService.STATUS_BLOCKED) {
            newStatus = UserManagementService.STATUS_ACTIVE;
            action = "mở khóa";
        } else {
            newStatus = UserManagementService.STATUS_BLOCKED;
            action = "khóa";
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận " + action)
                .setMessage("Bạn có chắc chắn muốn " + action + " tài khoản " + user.getUsername() + "?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    changeUserStatus(user, newStatus);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    @Override
    public void onViewUserDetails(User user) {
        Intent intent = new Intent(this, UserDetailActivity.class);
        intent.putExtra("user_id", user.getUserId());
        startActivity(intent);
    }
    
    // ========== Helper Methods ==========
    
    private void deleteUser(User user) {
        userService.deleteUser(user.getUserId(), new UserManagementService.UserOperationCallback() {
            @Override
            public void onSuccess(String message, User user) {
                runOnUiThread(() -> {
                    Toast.makeText(UserManagementActivity.this, message, Toast.LENGTH_SHORT).show();
                    loadUsers();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(UserManagementActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void changeUserStatus(User user, int newStatus) {
        userService.changeAccountStatus(user.getUserId(), newStatus, new UserManagementService.UserOperationCallback() {
            @Override
            public void onSuccess(String message, User updatedUser) {
                runOnUiThread(() -> {
                    Toast.makeText(UserManagementActivity.this, message, Toast.LENGTH_SHORT).show();
                    loadUsers();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(UserManagementActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
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
        } else if (itemId == R.id.action_filter_all) {
            showAllUsers();
            return true;
        } else if (itemId == R.id.action_filter_active) {
            showUsersByStatus(UserManagementService.STATUS_ACTIVE);
            return true;
        } else if (itemId == R.id.action_filter_blocked) {
            showUsersByStatus(UserManagementService.STATUS_BLOCKED);
            return true;
        } else if (itemId == R.id.action_filter_admins) {
            showUsersByRole(UserManagementService.ROLE_ADMIN);
            return true;
        } else if (itemId == R.id.action_filter_users) {
            showUsersByRole(UserManagementService.ROLE_USER);
            return true;
        } else if (itemId == R.id.action_refresh) {
            loadUsers();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void showAllUsers() {
        loadUsers();
    }
    
    private void showUsersByStatus(int status) {
        userService.getUsersByStatus(status, new UserManagementService.UserListCallback() {
            @Override
            public void onSuccess(List<User> users) {
                runOnUiThread(() -> {
                    allUsers.clear();
                    allUsers.addAll(users);
                    filterUsers(editTextSearch.getText().toString());
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(UserManagementActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void showUsersByRole(String role) {
        userService.getUsersByRole(role, new UserManagementService.UserListCallback() {
            @Override
            public void onSuccess(List<User> users) {
                runOnUiThread(() -> {
                    allUsers.clear();
                    allUsers.addAll(users);
                    filterUsers(editTextSearch.getText().toString());
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(UserManagementActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userService != null) {
            userService.shutdown();
        }
    }
}