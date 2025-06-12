package com.example.movies_app.Database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.example.movies_app.Database.entity.User;
import com.example.movies_app.Database.entity.UserPreference;

@Dao
public interface UserDao {
    // Phương thức hiện có...
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertUser(User user);

    // THÊM PHƯƠNG THỨC NÀY
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserPreference(UserPreference preference);

    // Các phương thức khác...
}