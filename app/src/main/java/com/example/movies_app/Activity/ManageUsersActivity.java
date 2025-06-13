package com.example.movies_app.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.Database.entity.User;
import com.example.movies_app.R;

import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {
    private RecyclerView usersRecyclerView;
    private ImageView backButton;
    private EditText searchEditText;
    private Button searchButton, blockUserButton, unblockUserButton;
    private Spinner roleSpinner;

    private AppDatabase database;
    // private UserManagementAdapter userAdapter;
    private List<User> usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        database = AppDatabase.getInstance(this);

        initViews();
        setupClickListeners();
        loadUsers();
    }

    private void initViews() {
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        backButton = findViewById(R.id.backButton);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        blockUserButton = findViewById(R.id.blockUserButton);
        unblockUserButton = findViewById(R.id.unblockUserButton);
        roleSpinner = findViewById(R.id.roleSpinner);

        // Setup RecyclerView
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        searchButton.setOnClickListener(v -> searchUsers());

        blockUserButton.setOnClickListener(v -> blockSelectedUsers());

        unblockUserButton.setOnClickListener(v -> unblockSelectedUsers());
    }

    private void loadUsers() {
        new Thread(() -> {
            try {
                usersList = database.userDao().getAllUsers();
                runOnUiThread(() -> {
                    // TODO: Update RecyclerView adapter
                    Toast.makeText(this, "Đã tải " + usersList.size() + " người dùng", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi tải danh sách người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void searchUsers() {
        String query = searchEditText.getText().toString().trim();
        if (query.isEmpty()) {
            loadUsers(); // Load all users if search is empty
            return;
        }

        new Thread(() -> {
            try {
                List<User> searchResults = database.userDao().searchUsers("%" + query + "%");
                runOnUiThread(() -> {
                    // TODO: Update RecyclerView with search results
                    Toast.makeText(this, "Tìm thấy " + searchResults.size() + " người dùng", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi tìm kiếm: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void blockSelectedUsers() {
        // TODO: Implement block functionality
        Toast.makeText(this, "Chức năng khóa người dùng", Toast.LENGTH_SHORT).show();
    }

    private void unblockSelectedUsers() {
        // TODO: Implement unblock functionality
        Toast.makeText(this, "Chức năng mở khóa người dùng", Toast.LENGTH_SHORT).show();
    }

    public void showUserDetails(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_details, null);

        // TODO: Populate dialog with user details

        builder.setView(dialogView)
                .setTitle("Thông tin người dùng")
                .setPositiveButton("Đóng", null);

        builder.create().show();
    }

    public void changeUserRole(int userId, String newRole) {
        new Thread(() -> {
            try {
                database.userDao().updateUserRole(userId, newRole);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đã cập nhật quyền người dùng", Toast.LENGTH_SHORT).show();
                    loadUsers(); // Refresh list
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Lỗi cập nhật quyền: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    public void deleteUser(int userId) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa người dùng này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    new Thread(() -> {
                        try {
                            database.userDao().deleteUser(userId);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Đã xóa người dùng", Toast.LENGTH_SHORT).show();
                                loadUsers(); // Refresh list
                            });
                        } catch (Exception e) {
                            runOnUiThread(() ->
                                    Toast.makeText(this, "Lỗi xóa người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                        }
                    }).start();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}