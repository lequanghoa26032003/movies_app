package com.example.movies_app.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.movies_app.Database.entity.User;
import com.example.movies_app.R;
import com.example.movies_app.service.UserManagementService;
import com.google.android.material.textfield.TextInputLayout;

public class AddEditUserActivity extends AppCompatActivity {
    
    private TextInputLayout tilEmail, tilUsername, tilPassword, tilFullName, tilPhone;
    private EditText editTextEmail, editTextUsername, editTextPassword, editTextFullName, editTextPhone;
    private Spinner spinnerRole, spinnerStatus;
    private Button buttonSave;
    
    private UserManagementService userService;
    private boolean isEditMode = false;
    private int userId = -1;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_user);
        
        initViews();
        setupToolbar();
        setupSpinners();
        
        userService = new UserManagementService(this);
        
        // Kiểm tra mode
        checkEditMode();
        
        buttonSave.setOnClickListener(v -> saveUser());
    }
    
    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        tilFullName = findViewById(R.id.tilFullName);
        tilPhone = findViewById(R.id.tilPhone);
        
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextPhone = findViewById(R.id.editTextPhone);
        
        spinnerRole = findViewById(R.id.spinnerRole);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        buttonSave = findViewById(R.id.buttonSave);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupSpinners() {
        // Role spinner
        String[] roles = {"USER", "ADMIN"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);
        
        // Status spinner
        String[] statuses = {"Hoạt động", "Chưa kích hoạt", "Bị khóa"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
    }
    
    private void checkEditMode() {
        isEditMode = getIntent().getBooleanExtra("edit_mode", false);
        userId = getIntent().getIntExtra("user_id", -1);
        
        if (isEditMode && userId != -1) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Chỉnh sửa người dùng");
            }
            loadUserData();
            
            // Ẩn password field trong edit mode
            tilPassword.setVisibility(View.GONE);
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Thêm người dùng mới");
            }
            // Hiện password field trong add mode
            tilPassword.setVisibility(View.VISIBLE);
        }
    }
    
    private void loadUserData() {
        userService.getUserById(userId, new UserManagementService.UserOperationCallback() {
            @Override
            public void onSuccess(String message, User user) {
                runOnUiThread(() -> {
                    currentUser = user;
                    populateFields(user);
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(AddEditUserActivity.this, error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
    
    private void populateFields(User user) {
        editTextEmail.setText(user.getEmail());
        editTextUsername.setText(user.getUsername());
        editTextFullName.setText(user.getFullName());
        editTextPhone.setText(user.getPhoneNumber());
        
        // Set role
        if ("ADMIN".equals(user.getRole())) {
            spinnerRole.setSelection(1);
        } else {
            spinnerRole.setSelection(0);
        }
        
        // Set status
        spinnerStatus.setSelection(user.getAccountStatus());
    }
    
    private void saveUser() {
        if (!validateInputs()) {
            return;
        }
        
        String email = editTextEmail.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String fullName = editTextFullName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();
        int status = spinnerStatus.getSelectedItemPosition();
        
        if (isEditMode) {
            updateUser(email, username, fullName, phone, role, status);
        } else {
            createUser(email, username, password, fullName, phone, role);
        }
    }
    
    private void createUser(String email, String username, String password, 
                           String fullName, String phone, String role) {
        userService.createUser(email, username, password, fullName, phone, role,
                new UserManagementService.UserOperationCallback() {
                    @Override
                    public void onSuccess(String message, User user) {
                        runOnUiThread(() -> {
                            Toast.makeText(AddEditUserActivity.this, message, Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        });
                    }
                    
                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(AddEditUserActivity.this, error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }
    
    private void updateUser(String email, String username, String fullName, 
                           String phone, String role, int status) {
        if (currentUser == null) return;
        
        // Update user object
        currentUser.setEmail(email);
        currentUser.setUsername(username);
        currentUser.setFullName(fullName);
        currentUser.setPhoneNumber(phone);
        currentUser.setRole(role);
        currentUser.setAccountStatus(status);
        
        userService.updateUser(currentUser, new UserManagementService.UserOperationCallback() {
            @Override
            public void onSuccess(String message, User user) {
                runOnUiThread(() -> {
                    Toast.makeText(AddEditUserActivity.this, message, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(AddEditUserActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private boolean validateInputs() {
        boolean isValid = true;
        
        // Clear previous errors
        tilEmail.setError(null);
        tilUsername.setError(null);
        tilPassword.setError(null);
        tilFullName.setError(null);
        tilPhone.setError(null);
        
        // Validate email
        String email = editTextEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email không được để trống");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            isValid = false;
        }
        
        // Validate username
        String username = editTextUsername.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Username không được để trống");
            isValid = false;
        } else if (username.length() < 3) {
            tilUsername.setError("Username phải có ít nhất 3 ký tự");
            isValid = false;
        }
        
        // Validate password (chỉ cho add mode)
        if (!isEditMode) {
            String password = editTextPassword.getText().toString().trim();
            if (TextUtils.isEmpty(password)) {
                tilPassword.setError("Mật khẩu không được để trống");
                isValid = false;
            } else if (password.length() < 6) {
                tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                isValid = false;
            }
        }
        
        // Validate full name
        String fullName = editTextFullName.getText().toString().trim();
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Họ tên không được để trống");
            isValid = false;
        }
        
        // Validate phone (optional but if provided, must be valid)
        String phone = editTextPhone.getText().toString().trim();
        if (!TextUtils.isEmpty(phone) && !android.util.Patterns.PHONE.matcher(phone).matches()) {
            tilPhone.setError("Số điện thoại không hợp lệ");
            isValid = false;
        }
        
        return isValid;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userService != null) {
            userService.shutdown();
        }
    }
}