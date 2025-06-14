package com.example.movies_app.service;

import android.content.Context;
import android.util.Log;

import com.example.movies_app.Database.AppDatabase;
import com.example.movies_app.Database.dao.UserDao;
import com.example.movies_app.Database.entity.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserManagementService {
    private static final String TAG = "UserManagementService";
    private UserDao userDao;
    private ExecutorService executorService;

    // Account Status Constants
    public static final int STATUS_INACTIVE = 0;
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_BLOCKED = 2;

    // Role Constants
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    public UserManagementService(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        userDao = database.userDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    // ========== CRUD OPERATIONS ==========

    /**
     * Tạo người dùng mới
     */
    public void createUser(String email, String username, String password,
                           String fullName, String phoneNumber, String role,
                           UserOperationCallback callback) {
        executorService.execute(() -> {
            try {
                // Kiểm tra email và username đã tồn tại chưa
                if (userDao.emailExists(email)) {
                    callback.onError("Email đã được sử dụng");
                    return;
                }

                if (userDao.usernameExists(username)) {
                    callback.onError("Username đã được sử dụng");
                    return;
                }

                // Hash password
                String passwordHash = hashPassword(password);
                if (passwordHash == null) {
                    callback.onError("Lỗi mã hóa mật khẩu");
                    return;
                }

                // Tạo user mới
                String currentDate = getCurrentDate();
                User newUser = new User(email, username, passwordHash, fullName,
                        phoneNumber, currentDate, role);
                newUser.setAccountStatus(STATUS_ACTIVE);

                long userId = userDao.insertUser(newUser);

                if (userId > 0) {
                    newUser.setUserId((int) userId);
                    callback.onSuccess("Tạo người dùng thành công", newUser);
                } else {
                    callback.onError("Không thể tạo người dùng");
                }

            } catch (Exception e) {
                Log.e(TAG, "Error creating user: " + e.getMessage());
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }

    /**
     * Lấy thông tin người dùng theo ID
     */
    public void getUserById(int userId, UserOperationCallback callback) {
        executorService.execute(() -> {
            try {
                User user = userDao.getUserById(userId);
                if (user != null) {
                    callback.onSuccess("Lấy thông tin thành công", user);
                } else {
                    callback.onError("Không tìm thấy người dùng");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting user: " + e.getMessage());
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }

    /**
     * Cập nhật thông tin người dùng
     */
    public void updateUser(User user, UserOperationCallback callback) {
        executorService.execute(() -> {
            try {
                int result = userDao.updateUser(user);
                if (result > 0) {
                    callback.onSuccess("Cập nhật thành công", user);
                } else {
                    callback.onError("Không thể cập nhật người dùng");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating user: " + e.getMessage());
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }

    /**
     * Xóa người dùng
     */
    public void deleteUser(int userId, UserOperationCallback callback) {
        executorService.execute(() -> {
            try {
                // Kiểm tra xem có phải admin cuối cùng không
                if (isLastAdmin(userId)) {
                    callback.onError("Không thể xóa admin cuối cùng");
                    return;
                }

                int result = userDao.deleteUser(userId);
                if (result > 0) {
                    callback.onSuccess("Xóa người dùng thành công", null);
                } else {
                    callback.onError("Không thể xóa người dùng");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting user: " + e.getMessage());
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }

    /**
     * Xóa người dùng với kiểm tra admin hiện tại
     */
    public void deleteUser(int userId, int currentAdminId, UserOperationCallback callback) {
        executorService.execute(() -> {
            try {
                // Kiểm tra admin không thể xóa chính mình
                if (userId == currentAdminId) {
                    callback.onError("Không thể xóa tài khoản của chính mình");
                    return;
                }

                // Kiểm tra xem có phải admin cuối cùng không
                if (isLastAdmin(userId)) {
                    callback.onError("Không thể xóa admin cuối cùng");
                    return;
                }

                int result = userDao.deleteUser(userId);
                if (result > 0) {
                    callback.onSuccess("Xóa người dùng thành công", null);
                } else {
                    callback.onError("Không thể xóa người dùng");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting user: " + e.getMessage());
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }

    /**
     * Lấy danh sách tất cả người dùng
     */
    public void getAllUsers(UserListCallback callback) {
        executorService.execute(() -> {
            try {
                List<User> users = userDao.getAllUsers();
                callback.onSuccess(users);
            } catch (Exception e) {
                Log.e(TAG, "Error getting all users: " + e.getMessage());
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }

    // ========== ACCOUNT STATUS MANAGEMENT ==========

    /**
     * Khóa tài khoản người dùng
     */
    public void blockUser(int userId, UserOperationCallback callback) {
        executorService.execute(() -> {
            try {
                // Kiểm tra xem có phải admin cuối cùng không
                if (isLastAdmin(userId)) {
                    callback.onError("Không thể khóa admin cuối cùng");
                    return;
                }

                int result = userDao.blockUser(userId);
                if (result > 0) {
                    User user = userDao.getUserById(userId);
                    callback.onSuccess("Khóa tài khoản thành công", user);
                } else {
                    callback.onError("Không thể khóa tài khoản");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error blocking user: " + e.getMessage());
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }

    /**
     * Khóa tài khoản người dùng với kiểm tra admin hiện tại
     */
    public void blockUser(int userId, int currentAdminId, UserOperationCallback callback) {
        executorService.execute(() -> {
            try {
                // Kiểm tra admin không thể khóa chính mình
                if (userId == currentAdminId) {
                    callback.onError("Không thể khóa tài khoản của chính mình");
                    return;
                }

                // Kiểm tra xem có phải admin cuối cùng không
                if (isLastAdmin(userId)) {
                    callback.onError("Không thể khóa admin cuối cùng");
                    return;
                }

                int result = userDao.blockUser(userId);
                if (result > 0) {
                    User user = userDao.getUserById(userId);
                    callback.onSuccess("Khóa tài khoản thành công", user);
                } else {
                    callback.onError("Không thể khóa tài khoản");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error blocking user: " + e.getMessage());
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }

    /**
     * Mở khóa tài khoản người dùng
     */
    public void unblockUser(int userId, UserOperationCallback callback) {
        executorService.execute(() -> {
            try {
                int result = userDao.unblockUser(userId);
                if (result > 0) {
                    User user = userDao.getUserById(userId);
                    callback.onSuccess("Mở khóa tài khoản thành công", user);
                } else {
                    callback.onError("Không thể mở khóa tài khoản");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error unblocking user: " + e.getMessage());
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }

    /**
     * Thay đổi trạng thái tài khoản
     */
    public void changeAccountStatus(int userId, int newStatus, UserOperationCallback callback) {
        executorService.execute(() -> {
            try {
                // Kiểm tra nếu đang khóa admin cuối cùng
                if (newStatus == STATUS_BLOCKED && isLastAdmin(userId)) {
                    callback.onError("Không thể khóa admin cuối cùng");
                    return;
                }

                int result = userDao.updateUserStatus(userId, newStatus);
                if (result > 0) {
                    User user = userDao.getUserById(userId);
                    String message = getStatusChangeMessage(newStatus);
                    callback.onSuccess(message, user);
                } else {
                    callback.onError("Không thể thay đổi trạng thái");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error changing status: " + e.getMessage());
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }

    /**
     * Thay đổi trạng thái tài khoản với kiểm tra admin hiện tại
     */
    public void changeAccountStatus(int userId, int newStatus, int currentAdminId, UserOperationCallback callback) {
        executorService.execute(() -> {
            try {
                // Kiểm tra admin không thể thay đổi trạng thái chính mình
                if (userId == currentAdminId && newStatus == STATUS_BLOCKED) {
                    callback.onError("Không thể khóa tài khoản của chính mình");
                    return;
                }

                // Kiểm tra nếu đang khóa admin cuối cùng
                if (newStatus == STATUS_BLOCKED && isLastAdmin(userId)) {
                    callback.onError("Không thể khóa admin cuối cùng");
                    return;
                }

                int result = userDao.updateUserStatus(userId, newStatus);
                if (result > 0) {
                    User user = userDao.getUserById(userId);
                    String message = getStatusChangeMessage(newStatus);
                    callback.onSuccess(message, user);
                } else {
                    callback.onError("Không thể thay đổi trạng thái");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error changing status: " + e.getMessage());
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }

    // ========== SEARCH OPERATIONS ==========

    /**
     * Tìm kiếm người dùng
     */
    public void searchUsers(String searchTerm, UserListCallback callback) {
        executorService.execute(() -> {
            try {
                String searchPattern = "%" + searchTerm + "%";
                List<User> users = userDao.searchUsers(searchPattern);
                callback.onSuccess(users);
            } catch (Exception e) {
                Log.e(TAG, "Error searching users: " + e.getMessage());
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }

    /**
     * Lấy người dùng theo trạng thái
     */
    public void getUsersByStatus(int status, UserListCallback callback) {
        executorService.execute(() -> {
            try {
                List<User> users = userDao.getUsersByStatus(status);
                callback.onSuccess(users);
            } catch (Exception e) {
                Log.e(TAG, "Error getting users by status: " + e.getMessage());
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }

    /**
     * Lấy người dùng theo vai trò
     */
    public void getUsersByRole(String role, UserListCallback callback) {
        executorService.execute(() -> {
            try {
                List<User> users = userDao.getUsersByRole(role);
                callback.onSuccess(users);
            } catch (Exception e) {
                Log.e(TAG, "Error getting users by role: " + e.getMessage());
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }

    // ========== BULK OPERATIONS ==========

    /**
     * Thay đổi trạng thái nhiều người dùng
     */
    public void bulkChangeStatus(List<Integer> userIds, int newStatus, BulkOperationCallback callback) {
        executorService.execute(() -> {
            try {
                // Kiểm tra nếu có admin trong danh sách và đang khóa
                if (newStatus == STATUS_BLOCKED) {
                    for (int userId : userIds) {
                        if (isLastAdmin(userId)) {
                            callback.onError("Không thể khóa admin cuối cùng");
                            return;
                        }
                    }
                }

                int result = userDao.updateMultipleUsersStatus(userIds, newStatus);
                if (result > 0) {
                    String message = getStatusChangeMessage(newStatus) + " cho " + result + " người dùng";
                    callback.onSuccess(message, result);
                } else {
                    callback.onError("Không thể thay đổi trạng thái");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error bulk status change: " + e.getMessage());
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }

    /**
     * Thay đổi trạng thái nhiều người dùng với kiểm tra admin hiện tại
     */
    public void bulkChangeStatus(List<Integer> userIds, int newStatus, int currentAdminId, BulkOperationCallback callback) {
        executorService.execute(() -> {
            try {
                // Kiểm tra admin không thể thay đổi trạng thái chính mình
                if (newStatus == STATUS_BLOCKED && userIds.contains(currentAdminId)) {
                    callback.onError("Không thể khóa tài khoản của chính mình");
                    return;
                }

                // Kiểm tra nếu có admin trong danh sách và đang khóa
                if (newStatus == STATUS_BLOCKED) {
                    for (int userId : userIds) {
                        if (isLastAdmin(userId)) {
                            callback.onError("Không thể khóa admin cuối cùng");
                            return;
                        }
                    }
                }

                int result = userDao.updateMultipleUsersStatus(userIds, newStatus);
                if (result > 0) {
                    String message = getStatusChangeMessage(newStatus) + " cho " + result + " người dùng";
                    callback.onSuccess(message, result);
                } else {
                    callback.onError("Không thể thay đổi trạng thái");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error bulk status change: " + e.getMessage());
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }

    /**
     * Xóa nhiều người dùng
     */
    public void bulkDeleteUsers(List<Integer> userIds, BulkOperationCallback callback) {
        executorService.execute(() -> {
            try {
                // Kiểm tra admin cuối cùng
                for (int userId : userIds) {
                    if (isLastAdmin(userId)) {
                        callback.onError("Không thể xóa admin cuối cùng");
                        return;
                    }
                }

                int result = userDao.deleteMultipleUsers(userIds);
                if (result > 0) {
                    callback.onSuccess("Đã xóa " + result + " người dùng", result);
                } else {
                    callback.onError("Không thể xóa người dùng");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error bulk delete: " + e.getMessage());
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }

    /**
     * Xóa nhiều người dùng với kiểm tra admin hiện tại
     */
    public void bulkDeleteUsers(List<Integer> userIds, int currentAdminId, BulkOperationCallback callback) {
        executorService.execute(() -> {
            try {
                // Kiểm tra admin không thể xóa chính mình
                if (userIds.contains(currentAdminId)) {
                    callback.onError("Không thể xóa tài khoản của chính mình");
                    return;
                }

                // Kiểm tra admin cuối cùng
                for (int userId : userIds) {
                    if (isLastAdmin(userId)) {
                        callback.onError("Không thể xóa admin cuối cùng");
                        return;
                    }
                }

                int result = userDao.deleteMultipleUsers(userIds);
                if (result > 0) {
                    callback.onSuccess("Đã xóa " + result + " người dùng", result);
                } else {
                    callback.onError("Không thể xóa người dùng");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error bulk delete: " + e.getMessage());
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }

    // ========== UTILITY METHODS ==========

    /**
     * Hash password using MD5
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashInBytes = md.digest(password.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error hashing password: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy ngày hiện tại
     */
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Kiểm tra xem có phải admin cuối cùng không
     */
    private boolean isLastAdmin(int userId) {
        try {
            User user = userDao.getUserById(userId);
            if (user != null && ROLE_ADMIN.equals(user.getRole())) {
                int activeAdminCount = userDao.getActiveAdminsCount();
                return activeAdminCount <= 1;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking last admin: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy thông báo thay đổi trạng thái
     */
    private String getStatusChangeMessage(int status) {
        switch (status) {
            case STATUS_ACTIVE:
                return "Kích hoạt tài khoản thành công";
            case STATUS_BLOCKED:
                return "Khóa tài khoản thành công";
            case STATUS_INACTIVE:
                return "Vô hiệu hóa tài khoản thành công";
            default:
                return "Thay đổi trạng thái thành công";
        }
    }

    /**
     * Lấy tên trạng thái
     */
    public static String getStatusName(int status) {
        switch (status) {
            case STATUS_ACTIVE:
                return "Hoạt động";
            case STATUS_BLOCKED:
                return "Bị khóa";
            case STATUS_INACTIVE:
                return "Chưa kích hoạt";
            default:
                return "Không xác định";
        }
    }

    /**
     * Đóng service
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    // ========== CALLBACK INTERFACES ==========

    public interface UserOperationCallback {
        void onSuccess(String message, User user);
        void onError(String error);
    }

    public interface UserListCallback {
        void onSuccess(List<User> users);
        void onError(String error);
    }

    public interface BulkOperationCallback {
        void onSuccess(String message, int affectedCount);
        void onError(String error);
    }
}