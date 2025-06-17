package com.example.movies_app.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.Database.dao.MovieDao;
import com.example.movies_app.Database.dao.UserDao;
import com.example.movies_app.Database.entity.User;
import com.example.movies_app.Helper.BaseBottomNavigationHelper;
import com.example.movies_app.Helper.PasswordHelper;
import com.example.movies_app.R;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    // UI Components
    private ImageView profileImage;
    private TextView userName, userEmail, userPhone, memberSince;
    private TextView watchedMoviesCount, favoriteCount;

    // Menu Options
    private LinearLayout editProfileOption, changePasswordOption, preferencesOption;
    private LinearLayout notificationOption, darkModeOption, appInfoOption, logoutOption;

    // Bottom Navigation
    private BottomAppBar bottomAppBar;
    private FloatingActionButton fabHome;
    private ImageView btnMain, btnHistory, btnFavorites, btnSearch, btnProfile;

    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final int CAPTURE_IMAGE_REQUEST = 1002;
    private static final int PERMISSION_REQUEST_CODE = 1003;
    private Uri photoUri;
    private AppDatabase database;
    private UserDao userDao;
    private MovieDao movieDao;
    private ExecutorService executorService;

    // User Data
    private User currentUser;
    private int currentUserId;

    // SharedPreferences for user settings
    private SharedPreferences userPrefs, appSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initDatabase();
        initSharedPreferences();
        initViews();
        setupBottomNavigation();
        setupMenuOptions();
        setupClickableStats();
        setFabToProfilePosition();
        highlightCurrentTab();

        loadUserProfile();
        loadUserStats();
    }

    private void initDatabase() {
        database = AppDatabase.getInstance(this);
        userDao = database.userDao();
        movieDao = database.movieDao();
        executorService = Executors.newFixedThreadPool(3);
    }

    private void initSharedPreferences() {
        userPrefs = getSharedPreferences("user_preferences", MODE_PRIVATE);
        appSettings = getSharedPreferences("app_settings", MODE_PRIVATE);
    }
    private void setupClickableStats() {
        // Make stats clickable
        if (watchedMoviesCount != null) {
            View watchedStatsContainer = findViewById(R.id.watchedStatsContainer); // Container của "Phim đã xem"
            if (watchedStatsContainer != null) {
                watchedStatsContainer.setOnClickListener(v -> {
                    // Add ripple effect
                    v.setPressed(true);
                    v.postDelayed(() -> v.setPressed(false), 100);

                    navigateToHistory();
                });

                // Add visual feedback
                watchedStatsContainer.setBackgroundResource(R.drawable.stat_background_clickable);
                watchedStatsContainer.setClickable(true);
                watchedStatsContainer.setFocusable(true);
            }
        }

        if (favoriteCount != null) {
            View favoriteStatsContainer = findViewById(R.id.favoriteStatsContainer); // Container của "Yêu thích"
            if (favoriteStatsContainer != null) {
                favoriteStatsContainer.setOnClickListener(v -> {
                    // Add ripple effect
                    v.setPressed(true);
                    v.postDelayed(() -> v.setPressed(false), 100);

                    navigateToFavorites();
                });

                // Add visual feedback
                favoriteStatsContainer.setBackgroundResource(R.drawable.stat_background_clickable);
                favoriteStatsContainer.setClickable(true);
                favoriteStatsContainer.setFocusable(true);
            }
        }
    }

    private void navigateToHistory() {
        // Show loading toast
        Toast.makeText(this, "📺 Đang tải lịch sử xem...", Toast.LENGTH_SHORT).show();

        // Animate FAB to history position
        BaseBottomNavigationHelper.setFabPosition(
                bottomAppBar,
                fabHome,
                BaseBottomNavigationHelper.HISTORY_POSITION
        );

        // Navigate after animation
        fabHome.postDelayed(() -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            intent.putExtra("from_profile", true); // Flag to show different behavior
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }, 200);
    }

    private void navigateToFavorites() {
        // Show loading toast
        Toast.makeText(this, "❤️ Đang tải phim yêu thích...", Toast.LENGTH_SHORT).show();

        // Animate FAB to favorites position
        BaseBottomNavigationHelper.setFabPosition(
                bottomAppBar,
                fabHome,
                BaseBottomNavigationHelper.FAVORITES_POSITION
        );

        // Navigate after animation
        fabHome.postDelayed(() -> {
            Intent intent = new Intent(this, FavoriteActivity.class);
            intent.putExtra("from_profile", true); // Flag to show different behavior
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }, 200);
    }

    private void initViews() {
        // Profile components
        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);

        // Additional profile info (add these IDs to your XML if needed)
        userPhone = findViewById(R.id.userPhone);
        memberSince = findViewById(R.id.memberSince);

        // Stats TextViews
        watchedMoviesCount = findViewById(R.id.watchedMoviesCount);
        favoriteCount = findViewById(R.id.favoriteCount);

        // New menu options (add these IDs to your XML)
        editProfileOption = findViewById(R.id.editProfileOption);
        changePasswordOption = findViewById(R.id.changePasswordOption);
        preferencesOption = findViewById(R.id.preferencesOption);

        // Existing menu options
        notificationOption = findViewById(R.id.notificationOption);
        darkModeOption = findViewById(R.id.darkModeOption);
        appInfoOption = findViewById(R.id.appInfoOption);
        logoutOption = findViewById(R.id.logoutOption);

        // Bottom Navigation
        bottomAppBar = findViewById(R.id.app_bar);
        btnHistory = findViewById(R.id.btn_history);
        btnFavorites = findViewById(R.id.btn_favorites);
        btnSearch = findViewById(R.id.btn_search);
        btnProfile = findViewById(R.id.btn_profile);
        fabHome = findViewById(R.id.fab_home);
        btnMain = findViewById(R.id.btn_center);
    }

    private void loadUserProfile() {
        // Get current user ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        // Load user data from database
        executorService.execute(() -> {
            try {
                currentUser = userDao.getUserById(currentUserId);

                runOnUiThread(() -> {
                    if (currentUser != null) {
                        displayUserInfo(currentUser);
                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                        redirectToLogin();
                    }
                });

            } catch (Exception e) {
                Log.e("ProfileActivity", "Error loading user profile: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayUserInfo(User user) {
        userName.setText(user.getUsername());
        userEmail.setText(user.getEmail());

        // Display additional info if views exist
        if (userPhone != null) {
            String phone = user.getPhoneNumber();
            userPhone.setText(phone != null && !phone.isEmpty() ? phone : "Chưa cập nhật");
        }

        if (memberSince != null) {
            String regDate = user.getRegistrationDate();
            if (regDate != null && !regDate.isEmpty()) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date date = inputFormat.parse(regDate);
                    memberSince.setText("Thành viên từ: " + outputFormat.format(date));
                } catch (Exception e) {
                    memberSince.setText("Thành viên từ: " + regDate.substring(0, 10));
                }
            }
        }

        // Load actual avatar image
        loadUserAvatar(user.getAvatarUrl());

        // Set click listener for profile image to change avatar
        profileImage.setOnClickListener(v -> showChangeAvatarDialog());
    }
    private void loadUserAvatar(String avatarPath) {
        if (avatarPath != null && !avatarPath.isEmpty()) {
            executorService.execute(() -> {
                try {
                    File avatarFile = new File(avatarPath);
                    if (avatarFile.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(avatarPath);
                        if (bitmap != null) {
                            runOnUiThread(() -> {
                                profileImage.setImageBitmap(bitmap);
                            });
                            return;
                        }
                    }
                } catch (Exception e) {
                    Log.e("ProfileActivity", "Error loading avatar: " + e.getMessage());
                }

                // Fallback to default avatar
                runOnUiThread(() -> {
                    profileImage.setImageResource(R.drawable.ic_person);
                });
            });
        } else {
            profileImage.setImageResource(R.drawable.ic_person);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                Toast.makeText(this, "✅ Quyền đã được cấp", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ Cần cấp quyền để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadUserStats() {
        executorService.execute(() -> {
            try {
                // Get watched movies count
                int watchedCount = movieDao.getWatchHistoryCountByUser(currentUserId);

                // Get favorite movies count
                int favoritesCount = movieDao.getFavoriteMoviesCountByUser(currentUserId);

                runOnUiThread(() -> {
                    // Update stats
                    if (watchedMoviesCount != null) {
                        watchedMoviesCount.setText(String.valueOf(watchedCount));
                    }
                    if (favoriteCount != null) {
                        favoriteCount.setText(String.valueOf(favoritesCount));
                    }
                });

            } catch (Exception e) {
                Log.e("ProfileActivity", "Error loading user stats: " + e.getMessage());
                runOnUiThread(() -> {
                    if (watchedMoviesCount != null) watchedMoviesCount.setText("0");
                    if (favoriteCount != null) favoriteCount.setText("0");
                });
            }
        });
    }

    private void setupMenuOptions() {
        // NEW: Edit Profile
        if (editProfileOption != null) {
            editProfileOption.setOnClickListener(v -> showEditProfileDialog());
        }

        // NEW: Change Password
        if (changePasswordOption != null) {
            changePasswordOption.setOnClickListener(v -> showChangePasswordDialog());
        }

        // NEW: User Preferences
        if (preferencesOption != null) {
            preferencesOption.setOnClickListener(v -> showUserPreferencesDialog());
        }

        // Existing options
        if (notificationOption != null) {
            notificationOption.setOnClickListener(v -> showNotificationSettings());
        }

        if (darkModeOption != null) {
            darkModeOption.setOnClickListener(v -> toggleDarkMode());
        }

        if (appInfoOption != null) {
            appInfoOption.setOnClickListener(v -> showAppInfo());
        }

        // Logout option
        if (logoutOption != null) {
            logoutOption.setOnClickListener(v -> showLogoutDialog());
        }
    }

    // ===== NEW: EDIT PROFILE FUNCTIONALITY =====
    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DarkDialogTheme);

        // Create dialog layout programmatically
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.setBackgroundColor(ContextCompat.getColor(this, R.color.dialog_background)); // Dark background

        // Full Name
        TextView fullNameLabel = new TextView(this);
        fullNameLabel.setText("Họ và tên:");
        fullNameLabel.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        fullNameLabel.setTextSize(14);
        fullNameLabel.setPadding(0, 0, 0, 8);
        layout.addView(fullNameLabel);

        EditText editFullName = new EditText(this);
        editFullName.setHint("Nhập họ và tên");
        editFullName.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        editFullName.setHintTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        editFullName.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.white));
        if (currentUser != null && currentUser.getFullName() != null) {
            editFullName.setText(currentUser.getFullName());
        }
        layout.addView(editFullName);

        // Phone Number
        TextView phoneLabel = new TextView(this);
        phoneLabel.setText("Số điện thoại:");
        phoneLabel.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        phoneLabel.setTextSize(14);
        phoneLabel.setPadding(0, 24, 0, 8);
        layout.addView(phoneLabel);

        EditText editPhoneNumber = new EditText(this);
        editPhoneNumber.setHint("Nhập số điện thoại");
        editPhoneNumber.setInputType(InputType.TYPE_CLASS_PHONE);
        editPhoneNumber.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        editPhoneNumber.setHintTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        editPhoneNumber.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.white));
        if (currentUser != null && currentUser.getPhoneNumber() != null) {
            editPhoneNumber.setText(currentUser.getPhoneNumber());
        }
        layout.addView(editPhoneNumber);

        // Email
        TextView emailLabel = new TextView(this);
        emailLabel.setText("Email:");
        emailLabel.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        emailLabel.setTextSize(14);
        emailLabel.setPadding(0, 24, 0, 8);
        layout.addView(emailLabel);

        EditText editEmail = new EditText(this);
        editEmail.setHint("Nhập email");
        editEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editEmail.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        editEmail.setHintTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        editEmail.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.white));
        if (currentUser != null) {
            editEmail.setText(currentUser.getEmail());
        }
        layout.addView(editEmail);

        AlertDialog dialog = builder.setView(layout)
                .setTitle("✏️ Chỉnh sửa thông tin")
                .setPositiveButton("Lưu", (d, which) -> {
                    String newFullName = editFullName.getText().toString().trim();
                    String newPhone = editPhoneNumber.getText().toString().trim();
                    String newEmail = editEmail.getText().toString().trim();

                    updateUserProfile(newFullName, newPhone, newEmail);
                })
                .setNegativeButton("Hủy", null)
                .create();

        // Set dialog background
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();

        // Set button colors
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.selected_tab_color));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

    private void updateUserProfile(String fullName, String phone, String email) {
        // Validate input
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phone.isEmpty() && !phone.matches("^[0-9+\\-\\s()]+$")) {
            Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            try {
                // Check if email already exists (except current user)
                User existingUser = userDao.getUserByEmail(email);
                if (existingUser != null && existingUser.getUserId() != currentUserId) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Email đã được sử dụng bởi tài khoản khác", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Update user info
                int fullNameResult = userDao.updateUserFullName(currentUserId, fullName);
                int phoneResult = userDao.updateUserPhone(currentUserId, phone);
                int emailResult = userDao.updateUserEmail(currentUserId, email);

                if (fullNameResult > 0 || phoneResult > 0 || emailResult > 0) {
                    // Reload user data
                    currentUser = userDao.getUserById(currentUserId);

                    runOnUiThread(() -> {
                        displayUserInfo(currentUser);
                        Toast.makeText(this, "✅ Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "❌ Không có thay đổi nào được lưu", Toast.LENGTH_SHORT).show();
                    });
                }

            } catch (Exception e) {
                Log.e("ProfileActivity", "Error updating profile: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "❌ Lỗi khi cập nhật thông tin", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // ===== NEW: CHANGE PASSWORD FUNCTIONALITY =====
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DarkDialogTheme);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.setBackgroundColor(ContextCompat.getColor(this, R.color.dialog_background));

        // Current Password
        TextView currentPassLabel = new TextView(this);
        currentPassLabel.setText("Mật khẩu hiện tại:");
        currentPassLabel.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        currentPassLabel.setTextSize(14);
        currentPassLabel.setPadding(0, 0, 0, 8);
        layout.addView(currentPassLabel);

        EditText editCurrentPassword = new EditText(this);
        editCurrentPassword.setHint("Nhập mật khẩu hiện tại");
        editCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editCurrentPassword.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        editCurrentPassword.setHintTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        editCurrentPassword.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.white));
        layout.addView(editCurrentPassword);

        // New Password
        TextView newPassLabel = new TextView(this);
        newPassLabel.setText("Mật khẩu mới:");
        newPassLabel.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        newPassLabel.setTextSize(14);
        newPassLabel.setPadding(0, 24, 0, 8);
        layout.addView(newPassLabel);

        EditText editNewPassword = new EditText(this);
        editNewPassword.setHint("Nhập mật khẩu mới");
        editNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editNewPassword.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        editNewPassword.setHintTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        editNewPassword.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.white));
        layout.addView(editNewPassword);

        // Confirm Password
        TextView confirmPassLabel = new TextView(this);
        confirmPassLabel.setText("Xác nhận mật khẩu mới:");
        confirmPassLabel.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        confirmPassLabel.setTextSize(14);
        confirmPassLabel.setPadding(0, 24, 0, 8);
        layout.addView(confirmPassLabel);

        EditText editConfirmPassword = new EditText(this);
        editConfirmPassword.setHint("Nhập lại mật khẩu mới");
        editConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editConfirmPassword.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        editConfirmPassword.setHintTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        editConfirmPassword.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.white));
        layout.addView(editConfirmPassword);

        // Show password checkbox
        CheckBox showPasswordCheckbox = new CheckBox(this);
        showPasswordCheckbox.setText("Hiển thị mật khẩu");
        showPasswordCheckbox.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        showPasswordCheckbox.setButtonTintList(ContextCompat.getColorStateList(this, android.R.color.white));
        showPasswordCheckbox.setPadding(0, 24, 0, 0);
        showPasswordCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int inputType = isChecked ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
            editCurrentPassword.setInputType(inputType);
            editNewPassword.setInputType(inputType);
            editConfirmPassword.setInputType(inputType);

            // Move cursor to end
            editCurrentPassword.setSelection(editCurrentPassword.getText().length());
            editNewPassword.setSelection(editNewPassword.getText().length());
            editConfirmPassword.setSelection(editConfirmPassword.getText().length());
        });
        layout.addView(showPasswordCheckbox);

        AlertDialog dialog = builder.setView(layout)
                .setTitle("🔒 Đổi mật khẩu")
                .setPositiveButton("Đổi mật khẩu", (d, which) -> {
                    String currentPassword = editCurrentPassword.getText().toString();
                    String newPassword = editNewPassword.getText().toString();
                    String confirmPassword = editConfirmPassword.getText().toString();

                    changePassword(currentPassword, newPassword, confirmPassword);
                })
                .setNegativeButton("Hủy", null)
                .create();

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();

        // Set button colors
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.selected_tab_color));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

    private void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        // Validate input
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            try {
                // Verify current password
                String currentPasswordHash = PasswordHelper.hashPassword(currentPassword);
                if (!currentPasswordHash.equals(currentUser.getPasswordHash())) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "❌ Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Update password
                String newPasswordHash = PasswordHelper.hashPassword(newPassword);
                int result = userDao.updateUserPassword(currentUserId, newPasswordHash);

                if (result > 0) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "✅ Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "❌ Lỗi khi đổi mật khẩu", Toast.LENGTH_SHORT).show();
                    });
                }

            } catch (Exception e) {
                Log.e("ProfileActivity", "Error changing password: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "❌ Lỗi khi đổi mật khẩu", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showUserPreferencesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DarkDialogTheme);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.setBackgroundColor(ContextCompat.getColor(this, R.color.dialog_background));

        // Video Quality Section
        TextView videoQualityLabel = new TextView(this);
        videoQualityLabel.setText("🎥 Chất lượng video:");
        videoQualityLabel.setTextColor(ContextCompat.getColor(this, R.color.selected_tab_color));
        videoQualityLabel.setTextSize(16);
        videoQualityLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        videoQualityLabel.setPadding(0, 0, 0, 16);
        layout.addView(videoQualityLabel);

        String currentQuality = userPrefs.getString("video_quality", "auto");
        String[] qualities = {"auto", "1080p", "720p", "480p"};
        String[] qualityLabels = {"Tự động", "1080p (HD)", "720p", "480p"};

        LinearLayout videoQualityGroup = new LinearLayout(this);
        videoQualityGroup.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < qualities.length; i++) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(qualityLabels[i]);
            checkBox.setTag(qualities[i]);
            checkBox.setChecked(qualities[i].equals(currentQuality));
            checkBox.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            checkBox.setButtonTintList(ContextCompat.getColorStateList(this, R.color.selected_tab_color));
            checkBox.setPadding(0, 4, 0, 4);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    for (int j = 0; j < videoQualityGroup.getChildCount(); j++) {
                        CheckBox otherCheckBox = (CheckBox) videoQualityGroup.getChildAt(j);
                        if (otherCheckBox != buttonView) {
                            otherCheckBox.setChecked(false);
                        }
                    }
                }
            });

            videoQualityGroup.addView(checkBox);
        }
        layout.addView(videoQualityGroup);

        // Divider
        View divider1 = new View(this);
        divider1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
        divider1.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        divider1.setPadding(0, 20, 0, 20);
        layout.addView(divider1);

        // Auto-play switch
        LinearLayout autoPlayLayout = new LinearLayout(this);
        autoPlayLayout.setOrientation(LinearLayout.HORIZONTAL);
        autoPlayLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        autoPlayLayout.setPadding(0, 16, 0, 16);

        TextView autoPlayLabel = new TextView(this);
        autoPlayLabel.setText("▶️ Tự động phát");
        autoPlayLabel.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        autoPlayLabel.setTextSize(16);
        autoPlayLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Switch autoPlaySwitch = new Switch(this);
        autoPlaySwitch.setChecked(userPrefs.getBoolean("auto_play", true));
        autoPlaySwitch.setThumbTintList(ContextCompat.getColorStateList(this, R.color.selected_tab_color));
        autoPlaySwitch.setTrackTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));

        autoPlayLayout.addView(autoPlayLabel);
        autoPlayLayout.addView(autoPlaySwitch);
        layout.addView(autoPlayLayout);

        // Subtitles switch
        LinearLayout subtitleLayout = new LinearLayout(this);
        subtitleLayout.setOrientation(LinearLayout.HORIZONTAL);
        subtitleLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        subtitleLayout.setPadding(0, 16, 0, 16);

        TextView subtitleLabel = new TextView(this);
        subtitleLabel.setText("📝 Phụ đề tự động");
        subtitleLabel.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        subtitleLabel.setTextSize(16);
        subtitleLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Switch subtitleSwitch = new Switch(this);
        subtitleSwitch.setChecked(userPrefs.getBoolean("subtitles_enabled", true));
        subtitleSwitch.setThumbTintList(ContextCompat.getColorStateList(this, R.color.selected_tab_color));
        subtitleSwitch.setTrackTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));

        subtitleLayout.addView(subtitleLabel);
        subtitleLayout.addView(subtitleSwitch);
        layout.addView(subtitleLayout);

        // Data Saver switch
        LinearLayout dataSaverLayout = new LinearLayout(this);
        dataSaverLayout.setOrientation(LinearLayout.HORIZONTAL);
        dataSaverLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        dataSaverLayout.setPadding(0, 16, 0, 0);

        TextView dataSaverLabel = new TextView(this);
        dataSaverLabel.setText("📊 Tiết kiệm dữ liệu");
        dataSaverLabel.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        dataSaverLabel.setTextSize(16);
        dataSaverLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Switch dataSaverSwitch = new Switch(this);
        dataSaverSwitch.setChecked(userPrefs.getBoolean("data_saver", false));
        dataSaverSwitch.setThumbTintList(ContextCompat.getColorStateList(this, R.color.selected_tab_color));
        dataSaverSwitch.setTrackTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));

        dataSaverLayout.addView(dataSaverLabel);
        dataSaverLayout.addView(dataSaverSwitch);
        layout.addView(dataSaverLayout);

        AlertDialog dialog = builder.setView(layout)
                .setTitle("⚙️ Tùy chọn người dùng")
                .setPositiveButton("Lưu", (d, which) -> {
                    SharedPreferences.Editor editor = userPrefs.edit();

                    // Save video quality
                    for (int i = 0; i < videoQualityGroup.getChildCount(); i++) {
                        CheckBox checkBox = (CheckBox) videoQualityGroup.getChildAt(i);
                        if (checkBox.isChecked()) {
                            editor.putString("video_quality", (String) checkBox.getTag());
                            break;
                        }
                    }

                    // Save other preferences
                    editor.putBoolean("auto_play", autoPlaySwitch.isChecked());
                    editor.putBoolean("subtitles_enabled", subtitleSwitch.isChecked());
                    editor.putBoolean("data_saver", dataSaverSwitch.isChecked());
                    editor.apply();

                    Toast.makeText(this, "✅ Đã lưu tùy chọn", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .create();

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();

        // Set button colors
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.selected_tab_color));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }


    private void showNotificationSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DarkDialogTheme);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.setBackgroundColor(ContextCompat.getColor(this, R.color.dialog_background));

        // Push notifications
        LinearLayout pushLayout = new LinearLayout(this);
        pushLayout.setOrientation(LinearLayout.HORIZONTAL);
        pushLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        pushLayout.setPadding(0, 16, 0, 16);

        TextView pushLabel = new TextView(this);
        pushLabel.setText("📱 Thông báo đẩy");
        pushLabel.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        pushLabel.setTextSize(16);
        pushLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Switch pushSwitch = new Switch(this);
        pushSwitch.setChecked(userPrefs.getBoolean("push_notifications", true));
        pushSwitch.setThumbTintList(ContextCompat.getColorStateList(this, R.color.selected_tab_color));
        pushSwitch.setTrackTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));

        pushLayout.addView(pushLabel);
        pushLayout.addView(pushSwitch);
        layout.addView(pushLayout);

        // Email notifications
        LinearLayout emailLayout = new LinearLayout(this);
        emailLayout.setOrientation(LinearLayout.HORIZONTAL);
        emailLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        emailLayout.setPadding(0, 16, 0, 16);

        TextView emailLabel = new TextView(this);
        emailLabel.setText("📧 Thông báo email");
        emailLabel.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        emailLabel.setTextSize(16);
        emailLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Switch emailSwitch = new Switch(this);
        emailSwitch.setChecked(userPrefs.getBoolean("email_notifications", false));
        emailSwitch.setThumbTintList(ContextCompat.getColorStateList(this, R.color.selected_tab_color));
        emailSwitch.setTrackTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));

        emailLayout.addView(emailLabel);
        emailLayout.addView(emailSwitch);
        layout.addView(emailLayout);

        // New Movies notifications
        LinearLayout newMoviesLayout = new LinearLayout(this);
        newMoviesLayout.setOrientation(LinearLayout.HORIZONTAL);
        newMoviesLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        newMoviesLayout.setPadding(0, 16, 0, 0);

        TextView newMoviesLabel = new TextView(this);
        newMoviesLabel.setText("🆕 Phim mới");
        newMoviesLabel.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        newMoviesLabel.setTextSize(16);
        newMoviesLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Switch newMoviesSwitch = new Switch(this);
        newMoviesSwitch.setChecked(userPrefs.getBoolean("new_movie_notifications", true));
        newMoviesSwitch.setThumbTintList(ContextCompat.getColorStateList(this, R.color.selected_tab_color));
        newMoviesSwitch.setTrackTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));

        newMoviesLayout.addView(newMoviesLabel);
        newMoviesLayout.addView(newMoviesSwitch);
        layout.addView(newMoviesLayout);

        AlertDialog dialog = builder.setView(layout)
                .setTitle("🔔 Cài đặt thông báo")
                .setPositiveButton("Lưu", (d, which) -> {
                    SharedPreferences.Editor editor = userPrefs.edit();
                    editor.putBoolean("push_notifications", pushSwitch.isChecked());
                    editor.putBoolean("email_notifications", emailSwitch.isChecked());
                    editor.putBoolean("new_movie_notifications", newMoviesSwitch.isChecked());
                    editor.apply();

                    Toast.makeText(this, "✅ Đã lưu cài đặt thông báo", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .create();

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();

        // Set button colors
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.selected_tab_color));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

    private void toggleDarkMode() {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("dark_mode", !isDarkMode);
        editor.apply();

        Toast.makeText(this, isDarkMode ? "🌞 Đã tắt chế độ tối" : "🌙 Đã bật chế độ tối", Toast.LENGTH_SHORT).show();

        // Restart activity to apply theme
        recreate();
    }

    private void showAppInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ℹ️ Thông tin ứng dụng")
                .setMessage("🎬 Movies App v1.0\n\n" +
                        "📱 Ứng dụng xem phim trực tuyến\n" +
                            "👨‍💻 Phát triển bởi: Movies Team\n" +
                        "📅 Ngày phát hành: 26/03/2025\n" +
                        "📧 Liên hệ: hoakieu2603@gmail.com\n\n" +
                        "© 2025 All rights reserved")
                .setPositiveButton("OK", null)
                .setNeutralButton("Đánh giá ứng dụng", (dialog, which) -> {
                    Toast.makeText(this, "🌟 Cảm ơn bạn đã sử dụng ứng dụng!", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showChangeAvatarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DarkDialogTheme);
        builder.setTitle("🖼️ Thay đổi ảnh đại diện")
                .setItems(new String[]{"📷 Chụp ảnh", "🖼️ Chọn từ thư viện", "👤 Sử dụng ảnh mặc định"},
                        (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    openCamera();
                                    break;
                                case 1:
                                    openGallery();
                                    break;
                                case 2:
                                    useDefaultAvatar();
                                    break;
                            }
                        })
                .show();
    }
    private void openGallery() {
        // Check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh đại diện"), PICK_IMAGE_REQUEST);
    }
    private void openCamera() {
        // Check permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Create file for photo
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider",
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
            }
        } else {
            Toast.makeText(this, "❌ Không tìm thấy ứng dụng camera", Toast.LENGTH_SHORT).show();
        }
    }
    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "AVATAR_" + timeStamp + "_";
            File storageDir = getExternalFilesDir("avatars");

            if (storageDir != null && !storageDir.exists()) {
                storageDir.mkdirs();
            }

            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (Exception e) {
            Log.e("ProfileActivity", "Error creating image file: " + e.getMessage());
            return null;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_IMAGE_REQUEST:
                    if (data != null && data.getData() != null) {
                        handleImageSelection(data.getData());
                    }
                    break;

                case CAPTURE_IMAGE_REQUEST:
                    if (photoUri != null) {
                        handleImageSelection(photoUri);
                    }
                    break;
            }
        }
    }

    private void handleImageSelection(Uri imageUri) {
        try {
            // Show progress dialog
            AlertDialog progressDialog = new AlertDialog.Builder(this, R.style.DarkDialogTheme)
                    .setTitle("🔄 Đang xử lý...")
                    .setMessage("Vui lòng đợi trong giây lát")
                    .setCancelable(false)
                    .show();

            // Process image in background
            executorService.execute(() -> {
                try {
                    // Load and resize image
                    Bitmap bitmap = loadAndResizeImage(imageUri);

                    // Save to internal storage
                    String savedPath = saveAvatarToInternalStorage(bitmap);

                    if (savedPath != null) {
                        // Update database
                        int result = userDao.updateUserAvatar(currentUserId, savedPath);

                        runOnUiThread(() -> {
                            progressDialog.dismiss();

                            if (result > 0) {
                                // Update UI
                                profileImage.setImageBitmap(bitmap);
                                Toast.makeText(this, "✅ Đã cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show();

                                // Update current user object
                                if (currentUser != null) {
                                    currentUser.setAvatarUrl(savedPath);
                                }
                            } else {
                                Toast.makeText(this, "❌ Lỗi khi lưu ảnh đại diện", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "❌ Lỗi khi xử lý ảnh", Toast.LENGTH_SHORT).show();
                        });
                    }

                } catch (Exception e) {
                    Log.e("ProfileActivity", "Error handling image: " + e.getMessage());
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "❌ Lỗi khi xử lý ảnh", Toast.LENGTH_SHORT).show();
                    });
                }
            });

        } catch (Exception e) {
            Log.e("ProfileActivity", "Error selecting image: " + e.getMessage());
            Toast.makeText(this, "❌ Lỗi khi chọn ảnh", Toast.LENGTH_SHORT).show();
        }
    }
    private String saveAvatarToInternalStorage(Bitmap bitmap) {
        try {
            String fileName = "avatar_user_" + currentUserId + "_" + System.currentTimeMillis() + ".jpg";
            File avatarsDir = new File(getFilesDir(), "avatars");

            if (!avatarsDir.exists()) {
                avatarsDir.mkdirs();
            }

            File avatarFile = new File(avatarsDir, fileName);
            FileOutputStream fos = new FileOutputStream(avatarFile);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            return avatarFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e("ProfileActivity", "Error saving avatar: " + e.getMessage());
            return null;
        }
    }
    private Bitmap loadAndResizeImage(Uri imageUri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();

        // Resize to 300x300 for avatar
        int targetSize = 300;
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        float scaleFactor = Math.min((float) targetSize / width, (float) targetSize / height);
        int scaledWidth = Math.round(width * scaleFactor);
        int scaledHeight = Math.round(height * scaleFactor);

        return Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true);
    }
    private void useDefaultAvatar() {
        profileImage.setImageResource(R.drawable.ic_person);

        // Update in database
        executorService.execute(() -> {
            try {
                userDao.updateUserAvatar(currentUserId, "");
                runOnUiThread(() -> {
                    Toast.makeText(this, "✅ Đã cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e("ProfileActivity", "Error updating avatar: " + e.getMessage());
            }
        });
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🚪 Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?\n\nTất cả dữ liệu chưa lưu sẽ bị mất.")
                .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performLogout() {
        // Clear user session
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Update last login in database
        executorService.execute(() -> {
            try {
                String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()).format(new Date());
                userDao.updateLastLogin(currentUserId, currentTime);
            } catch (Exception e) {
                Log.e("ProfileActivity", "Error updating last login: " + e.getMessage());
            }
        });

        Toast.makeText(this, "👋 Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();

        // Redirect to login
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBottomNavigation() {
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        });

        btnFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(this, FavoriteActivity.class);
            startActivity(intent);
        });

        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExploreActivity.class);
            startActivity(intent);
        });

        btnMain.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void setFabToProfilePosition() {
        BaseBottomNavigationHelper.setFabPositionImmediate(
                bottomAppBar,
                fabHome,
                BaseBottomNavigationHelper.PROFILE_POSITION
        );
    }

    private void highlightCurrentTab() {
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        int selectedColor = ContextCompat.getColor(this, R.color.selected_tab_color);

        btnHistory.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnFavorites.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnSearch.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        btnProfile.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user stats when returning to profile
        if (currentUserId != -1) {
            loadUserStats();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}