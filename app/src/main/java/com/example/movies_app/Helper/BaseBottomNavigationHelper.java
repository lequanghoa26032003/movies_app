package com.example.movies_app.Helper;

import android.content.Context;
import android.content.Intent;
import com.example.movies_app.Activity.HistoryActivity;
import com.example.movies_app.Activity.ExploreActivity;
import com.example.movies_app.Activity.FavoriteActivity;
import com.example.movies_app.Activity.MainActivity;
import com.example.movies_app.Activity.ProfileActivity;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BaseBottomNavigationHelper {

    // ✅ Định nghĩa vị trí các tab
    public static final float HISTORY_POSITION = 0.1f;    // 10% từ trái
    public static final float FAVORITES_POSITION = 0.3f;  // 90% từ trái
    public static final float CENTER_POSITION = 0.5f;     // 50% từ trái (Home)
    public static final float SEARCH_POSITION = 0.7f;     // 70% từ trái
    public static final float PROFILE_POSITION = 0.9f;    // 30% từ trái

    /**
     * Di chuyển FAB đến vị trí được chỉ định
     * @param bottomAppBar BottomAppBar chứa FAB
     * @param fab FloatingActionButton cần di chuyển
     * @param positionPercent Vị trí từ 0.0 (trái) đến 1.0 (phải)
     */
    public static void setFabPosition(BottomAppBar bottomAppBar,
                                      FloatingActionButton fab,
                                      float positionPercent) {
        if (positionPercent == CENTER_POSITION) {
            // FAB về center - có cradle ở giữa
            bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
        } else {
            // FAB di chuyển đến vị trí được chỉ định
            fab.post(() -> {
                int margin = calculateMarginForPosition(bottomAppBar.getContext(),
                        fab, positionPercent);
                bottomAppBar.setFabAlignmentModeEndMargin(margin);
                bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
            });
        }
    }

    /**
     * Tính toán margin từ mép phải để FAB ở đúng vị trí
     */
    private static int calculateMarginForPosition(Context context,
                                                  FloatingActionButton fab,
                                                  float positionPercent) {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        // Lấy kích thước FAB (đợi render xong)
        int fabWidth = fab.getWidth();
        if (fabWidth == 0) {
            // Fallback nếu FAB chưa được render
            fabWidth = (int) (56 * context.getResources().getDisplayMetrics().density); // 56dp default FAB size
        }

        // Tính vị trí center của FAB
        int targetCenterX = (int) (screenWidth * positionPercent);

        // Margin = khoảng cách từ mép phải đến vị trí mong muốn
        int marginNeeded = screenWidth - targetCenterX - (fabWidth / 2);

        // Đảm bảo margin không âm
        return Math.max(0, marginNeeded);
    }

    /**
     * Đặt FAB về vị trí mặc định (center) với animation mượt
     */
    public static void resetFabToCenter(BottomAppBar bottomAppBar) {
        bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
    }


}