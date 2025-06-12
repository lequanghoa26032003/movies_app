package com.example.movies_app.Helper;

import android.content.Context;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BaseBottomNavigationHelper {

    public static final float HISTORY_POSITION = 0.1f;
    public static final float FAVORITES_POSITION = 0.3f;
    public static final float CENTER_POSITION = 0.5f;
    public static final float SEARCH_POSITION = 0.7f;
    public static final float PROFILE_POSITION = 0.9f;

    public static final int ANIMATION_DURATION = 200;

    /**
     * ✅ Di chuyển FAB với animation (dùng khi click)
     */
    public static void setFabPosition(BottomAppBar bottomAppBar,
                                      FloatingActionButton fab,
                                      float positionPercent) {
        if (positionPercent == CENTER_POSITION) {
            bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
        } else {
            fab.post(() -> {
                int margin = calculateMarginForPosition(bottomAppBar.getContext(),
                        fab, positionPercent);

                bottomAppBar.setFabAlignmentModeEndMargin(margin);
                bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);

                // Animation scale nhẹ
                fab.animate()
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(100)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .withEndAction(() -> {
                            fab.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(100)
                                    .setInterpolator(new AccelerateDecelerateInterpolator())
                                    .start();
                        })
                        .start();
            });
        }
    }

    /**
     * ✅ THÊM: Set FAB position NGAY LẬP TỨC (không animation) - dùng khi vào Activity
     */
    /**
     * ✅ Set FAB position NGAY LẬP TỨC (không animation) - dùng khi vào Activity
     */
    public static void setFabPositionImmediate(BottomAppBar bottomAppBar,
                                               FloatingActionButton fab,
                                               float positionPercent) {
        if (positionPercent == CENTER_POSITION) {
            bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
        } else {
            fab.post(() -> {
                int margin = calculateMarginForPosition(bottomAppBar.getContext(),
                        fab, positionPercent);

                bottomAppBar.setFabAlignmentModeEndMargin(margin);
                bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
            });
        }
    }

    private static int calculateMarginForPosition(Context context,
                                                  FloatingActionButton fab,
                                                  float positionPercent) {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        int fabWidth = fab.getWidth();
        if (fabWidth == 0) {
            fabWidth = (int) (56 * context.getResources().getDisplayMetrics().density);
        }

        int targetCenterX = (int) (screenWidth * positionPercent);
        int marginNeeded = screenWidth - targetCenterX - (fabWidth / 2);

        return Math.max(0, marginNeeded);
    }

    public static void resetFabToCenter(BottomAppBar bottomAppBar) {
        bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
    }
}