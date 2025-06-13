package com.example.movies_app.Database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.movies_app.Database.entity.User;
import com.example.movies_app.Database.entity.UserPreference;

import java.util.List;

@Dao
public interface UserDao {

    // ========== INSERT METHODS ==========
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertUser(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserPreference(UserPreference preference);

    // ========== LOGIN & AUTHENTICATION ==========
    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :password LIMIT 1")
    User loginUser(String email, String password);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    User getUserById(int userId);

    // ========== GET ALL USERS ==========
    @Query("SELECT * FROM users ORDER BY registrationDate DESC")
    List<User> getAllUsers();

    @Query("SELECT * FROM users WHERE accountStatus = :status ORDER BY registrationDate DESC")
    List<User> getUsersByStatus(int status);

    @Query("SELECT * FROM users WHERE role = :role ORDER BY registrationDate DESC")
    List<User> getUsersByRole(String role);

    // ========== VALIDATION METHODS ==========
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int checkEmailExists(String email);

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    int checkUsernameExists(String username);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE userId = :userId)")
    boolean userExists(int userId);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    boolean emailExists(String email);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    boolean usernameExists(String username);

    // ========== COUNTING METHODS ==========
    @Query("SELECT COUNT(*) FROM users")
    int getTotalUsersCount();

    @Query("SELECT COUNT(*) FROM users WHERE accountStatus = 1")
    int getActiveUsersCount();

    @Query("SELECT COUNT(*) FROM users WHERE accountStatus = 0")
    int getInactiveUsersCount();

    @Query("SELECT COUNT(*) FROM users WHERE accountStatus = 2")
    int getBlockedUsersCount();

    @Query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN'")
    int getAdminUsersCount();

    @Query("SELECT COUNT(*) FROM users WHERE role = 'USER'")
    int getRegularUsersCount();

    // ========== DELETE METHODS ==========
    @Query("DELETE FROM users WHERE userId = :userId")
    int deleteUser(int userId);

    @Query("DELETE FROM users WHERE email = :email")
    int deleteUserByEmail(String email);

    @Query("DELETE FROM users WHERE username = :username")
    int deleteUserByUsername(String username);

    @Delete
    void deleteUserEntity(User user);

    // ========== UPDATE METHODS ==========
    @Update
    int updateUser(User user);

    @Query("UPDATE users SET accountStatus = :status WHERE userId = :userId")
    int updateUserStatus(int userId, int status);

    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE userId = :userId")
    int updateUserPassword(int userId, String newPasswordHash);

    @Query("UPDATE users SET lastLoginDate = :loginDate WHERE userId = :userId")
    int updateLastLogin(int userId, String loginDate);

    @Query("UPDATE users SET fullName = :fullName WHERE userId = :userId")
    int updateUserFullName(int userId, String fullName);

    @Query("UPDATE users SET phoneNumber = :phoneNumber WHERE userId = :userId")
    int updateUserPhone(int userId, String phoneNumber);

    @Query("UPDATE users SET avatarUrl = :avatarUrl WHERE userId = :userId")
    int updateUserAvatar(int userId, String avatarUrl);

    @Query("UPDATE users SET role = :role WHERE userId = :userId")
    int updateUserRole(int userId, String role);

    // ========== USER MANAGEMENT METHODS ==========
    @Query("UPDATE users SET accountStatus = 2 WHERE userId = :userId")
    int blockUser(int userId);

    @Query("UPDATE users SET accountStatus = 1 WHERE userId = :userId")
    int unblockUser(int userId);

    @Query("UPDATE users SET accountStatus = 0 WHERE userId = :userId")
    int deactivateUser(int userId);

    // ========== SEARCH METHODS ==========
    @Query("SELECT * FROM users WHERE username LIKE :searchTerm OR email LIKE :searchTerm OR fullName LIKE :searchTerm ORDER BY registrationDate DESC")
    List<User> searchUsers(String searchTerm);

    @Query("SELECT * FROM users WHERE fullName LIKE :name ORDER BY registrationDate DESC")
    List<User> searchUsersByName(String name);

    @Query("SELECT * FROM users WHERE email LIKE :email ORDER BY registrationDate DESC")
    List<User> searchUsersByEmail(String email);

    // ========== DATE RANGE QUERIES ==========
    @Query("SELECT * FROM users WHERE registrationDate BETWEEN :startDate AND :endDate ORDER BY registrationDate DESC")
    List<User> getUsersByDateRange(String startDate, String endDate);

    @Query("SELECT * FROM users WHERE lastLoginDate BETWEEN :startDate AND :endDate ORDER BY lastLoginDate DESC")
    List<User> getUsersByLastLoginRange(String startDate, String endDate);

    // ========== RECENT ACTIVITIES ==========
    @Query("SELECT * FROM users ORDER BY registrationDate DESC LIMIT :limit")
    List<User> getRecentRegisteredUsers(int limit);

    @Query("SELECT * FROM users WHERE lastLoginDate IS NOT NULL ORDER BY lastLoginDate DESC LIMIT :limit")
    List<User> getRecentActiveUsers(int limit);

    // ========== STATISTICS QUERIES ==========
    @Query("SELECT COUNT(*) FROM users WHERE registrationDate >= :date")
    int getUsersRegisteredSince(String date);

    @Query("SELECT COUNT(*) FROM users WHERE lastLoginDate >= :date")
    int getActiveUsersSince(String date);

    // ✅ SỬA: Thay List<Object> bằng Map hoặc các query riêng biệt
    @Query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN'")
    int getAdminCount();

    @Query("SELECT COUNT(*) FROM users WHERE role = 'USER'")
    int getUserCount();

    @Query("SELECT COUNT(*) FROM users WHERE accountStatus = 0")
    int getStatusInactiveCount();

    @Query("SELECT COUNT(*) FROM users WHERE accountStatus = 1")
    int getStatusActiveCount();

    @Query("SELECT COUNT(*) FROM users WHERE accountStatus = 2")
    int getStatusBlockedCount();

    // ========== ADMIN SPECIFIC QUERIES ==========
    @Query("SELECT * FROM users WHERE role = 'ADMIN' ORDER BY registrationDate DESC")
    List<User> getAllAdmins();

    @Query("SELECT * FROM users WHERE role = 'USER' ORDER BY registrationDate DESC")
    List<User> getAllRegularUsers();

    @Query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND accountStatus = 1")
    int getActiveAdminsCount();

    // ========== BULK OPERATIONS ==========
    @Query("UPDATE users SET accountStatus = :status WHERE userId IN (:userIds)")
    int updateMultipleUsersStatus(List<Integer> userIds, int status);

    @Query("DELETE FROM users WHERE userId IN (:userIds)")
    int deleteMultipleUsers(List<Integer> userIds);

    @Query("UPDATE users SET accountStatus = 2 WHERE userId IN (:userIds)")
    int blockMultipleUsers(List<Integer> userIds);

    @Query("UPDATE users SET accountStatus = 1 WHERE userId IN (:userIds)")
    int unblockMultipleUsers(List<Integer> userIds);

    // ========== CLEANUP METHODS ==========
    @Query("DELETE FROM users WHERE accountStatus = 0 AND registrationDate < :cutoffDate")
    int deleteInactiveUsersOlderThan(String cutoffDate);

    @Query("SELECT COUNT(*) FROM users WHERE lastLoginDate IS NULL OR lastLoginDate < :cutoffDate")
    int countInactiveUsers(String cutoffDate);

    // ========== PREFERENCE QUERIES ==========
    @Query("SELECT * FROM user_preferences WHERE userId = :userId")
    UserPreference getUserPreference(int userId);

    @Query("DELETE FROM user_preferences WHERE userId = :userId")
    int deleteUserPreference(int userId);

    @Query("SELECT COUNT(*) FROM user_preferences WHERE userId = :userId")
    int hasUserPreference(int userId);
}