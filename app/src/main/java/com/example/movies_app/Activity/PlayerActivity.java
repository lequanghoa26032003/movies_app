package com.example.movies_app.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.example.movies_app.R;
import com.example.movies_app.service.WatchHistoryService;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;

public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = "PlayerActivity";

    // UI Components
    private YouTubePlayerView youTubePlayerView;
    private PlayerView exoPlayerView;
    private WebView webViewPlayer;
    private TextView titleText;
    private ImageView backButton;

    // Players
    private ExoPlayer exoPlayer;
    private YouTubePlayer currentYouTubePlayer;

    // Intent data
    private String videoUrl;
    private String youtubeKey;
    private String movieTitle;
    private long resumePosition = 0;
    private int movieId = -1;

    // Watch History
    private WatchHistoryService watchHistoryService;
    private int currentUserId = -1;
    private long currentPosition = 0;
    private boolean isWatchingStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        initViews();
        initServices();
        getIntentData();
        setupBackButton();
        playVideo();
    }

    private void initViews() {
        youTubePlayerView = findViewById(R.id.youtube_player_view);
        exoPlayerView = findViewById(R.id.exo_player_view);
        webViewPlayer = findViewById(R.id.webview_player);
        titleText = findViewById(R.id.movie_title);
        backButton = findViewById(R.id.back_button);

        // Add YouTube player to lifecycle
        getLifecycle().addObserver(youTubePlayerView);
    }

    private void initServices() {
        watchHistoryService = WatchHistoryService.getInstance(this);
        getCurrentUser();
    }

    private void getCurrentUser() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);
        Log.d(TAG, "Current user ID: " + currentUserId);
    }

    private void getIntentData() {
        videoUrl = getIntent().getStringExtra("videoUrl");
        youtubeKey = getIntent().getStringExtra("youtubeKey");
        movieTitle = getIntent().getStringExtra("title");
        resumePosition = getIntent().getLongExtra("resumePosition", 0);
        movieId = getIntent().getIntExtra("movieId", -1);

        Log.d(TAG, "📱 Intent data - Title: " + movieTitle);
        Log.d(TAG, "📱 Intent data - VideoURL: " + videoUrl);
        Log.d(TAG, "📱 Intent data - YouTubeKey: " + youtubeKey);
        Log.d(TAG, "📱 Intent data - ResumePosition: " + resumePosition);
        Log.d(TAG, "📱 Intent data - MovieID: " + movieId);

        // Set title
        if (movieTitle != null && !movieTitle.isEmpty()) {
            titleText.setText(movieTitle);
        } else {
            titleText.setText("Movie Player");
        }
    }

    private void setupBackButton() {
        backButton.setOnClickListener(v -> {
            Log.d(TAG, "🔙 Back button clicked");
            saveWatchHistoryBeforeExit();
            finish();
        });
    }

    private void playVideo() {
        hideAllPlayers();

        // Determine video type and play accordingly
        if (youtubeKey != null && !youtubeKey.isEmpty()) {
            Log.d(TAG, "🎬 Playing YouTube video with key: " + youtubeKey);
            playYouTubeVideo(youtubeKey);
        } else if (videoUrl != null && !videoUrl.isEmpty()) {
            if (isYouTubeUrl(videoUrl)) {
                String extractedKey = extractYouTubeKey(videoUrl);
                if (extractedKey != null) {
                    Log.d(TAG, "🎬 Extracted YouTube key: " + extractedKey);
                    playYouTubeVideo(extractedKey);
                } else {
                    Log.d(TAG, "🎬 Playing YouTube URL with WebView: " + videoUrl);
                    playWithWebView(videoUrl);
                }
            } else if (isDirectVideoUrl(videoUrl)) {
                Log.d(TAG, "🎬 Playing direct video with ExoPlayer: " + videoUrl);
                playWithExoPlayer(videoUrl);
            } else {
                Log.d(TAG, "🎬 Playing unknown URL with WebView: " + videoUrl);
                playWithWebView(videoUrl);
            }
        } else {
            showError("Không có URL video để phát");
            finish();
        }
    }

    private boolean isYouTubeUrl(String url) {
        return url != null && (url.contains("youtube.com") || url.contains("youtu.be"));
    }

    private boolean isDirectVideoUrl(String url) {
        return url != null && (url.endsWith(".mp4") || url.endsWith(".m3u8") ||
                url.endsWith(".mkv") || url.endsWith(".avi") ||
                url.endsWith(".mov") || url.endsWith(".wmv"));
    }

    private String extractYouTubeKey(String url) {
        try {
            if (url.contains("youtube.com/watch?v=")) {
                return url.split("v=")[1].split("&")[0];
            } else if (url.contains("youtu.be/")) {
                return url.split("youtu.be/")[1].split("\\?")[0];
            } else if (url.contains("youtube.com/embed/")) {
                return url.split("embed/")[1].split("\\?")[0];
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error extracting YouTube key: " + e.getMessage());
        }
        return null;
    }

    private void playYouTubeVideo(String videoKey) {
        youTubePlayerView.setVisibility(View.VISIBLE);

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                Log.d(TAG, "✅ YouTube Player ready, loading video: " + videoKey);
                currentYouTubePlayer = youTubePlayer;

                // Load video và seek đến vị trí resume nếu có
                if (resumePosition > 0) {
                    youTubePlayer.loadVideo(videoKey, resumePosition / 1000f); // Convert ms to seconds
                } else {
                    youTubePlayer.loadVideo(videoKey, 0f);
                }

                youTubePlayer.play();

                // Lưu lịch sử bắt đầu xem
                saveWatchHistoryStart();
            }

            @Override
            public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float second) {
                // Cập nhật vị trí hiện tại (convert seconds to milliseconds)
                currentPosition = (long) (second * 1000);

                // Log mỗi 10 giây để không spam log
                if ((long) second % 10 == 0) {
                    Log.d(TAG, "🎬 Current position: " + second + "s (" + currentPosition + "ms)");
                }
            }

            @Override
            public void onStateChange(@NonNull YouTubePlayer youTubePlayer,
                                      @NonNull PlayerConstants.PlayerState state) {
                Log.d(TAG, "🎬 YouTube Player state changed: " + state.toString());

                switch (state) {
                    case PLAYING:
                        if (!isWatchingStarted) {
                            saveWatchHistoryStart();
                        }
                        break;
                    case PAUSED:
                    case ENDED:
                        saveWatchHistoryPosition();
                        break;
                }
            }

            @Override
            public void onError(@NonNull YouTubePlayer youTubePlayer,
                                @NonNull PlayerConstants.PlayerError error) {
                Log.e(TAG, "❌ YouTube Player error: " + error.toString());
                showError("Lỗi phát video YouTube: " + error.toString());

                // Fallback to WebView
                hideAllPlayers();
                String fallbackUrl = "https://www.youtube.com/embed/" + videoKey + "?autoplay=1";
                playWithWebView(fallbackUrl);
            }
        });
    }

    private void playWithExoPlayer(String videoUrl) {
        exoPlayerView.setVisibility(View.VISIBLE);

        try {
            if (exoPlayer == null) {
                exoPlayer = new ExoPlayer.Builder(this).build();
                exoPlayerView.setPlayer(exoPlayer);
            }

            MediaItem mediaItem = MediaItem.fromUri(videoUrl);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();

            // Seek đến vị trí resume nếu có
            if (resumePosition > 0) {
                exoPlayer.seekTo(resumePosition);
            }

            exoPlayer.play();
            saveWatchHistoryStart();

            Log.d(TAG, "✅ ExoPlayer started successfully");

        } catch (Exception e) {
            Log.e(TAG, "❌ ExoPlayer error: " + e.getMessage());
            showError("Lỗi phát video: " + e.getMessage());

            // Fallback to WebView
            hideAllPlayers();
            playWithWebView(videoUrl);
        }
    }

    private void playWithWebView(String videoUrl) {
        webViewPlayer.setVisibility(View.VISIBLE);

        try {
            WebSettings webSettings = webViewPlayer.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setMediaPlaybackRequiresUserGesture(false);
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

            String finalUrl = videoUrl;

            // Convert YouTube watch URL to embed URL for better compatibility
            if (isYouTubeUrl(videoUrl)) {
                String key = extractYouTubeKey(videoUrl);
                if (key != null) {
                    finalUrl = "https://www.youtube.com/embed/" + key + "?autoplay=1&playsinline=1";
                    if (resumePosition > 0) {
                        finalUrl += "&start=" + (resumePosition / 1000); // Convert to seconds
                    }
                }
            }

            webViewPlayer.loadUrl(finalUrl);
            saveWatchHistoryStart();
            Log.d(TAG, "✅ WebView loading URL: " + finalUrl);

        } catch (Exception e) {
            Log.e(TAG, "❌ WebView error: " + e.getMessage());
            showError("Lỗi phát video với WebView: " + e.getMessage());
        }
    }

    // ========== WATCH HISTORY METHODS ==========

    private void saveWatchHistoryStart() {
        if (currentUserId == -1 || movieId == -1 || isWatchingStarted) {
            return;
        }

        isWatchingStarted = true;
        Log.d(TAG, "📝 Saving watch history start for movie: " + movieId);

        watchHistoryService.saveWatchHistory(movieId, currentPosition, new WatchHistoryService.WatchHistoryOperationCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "✅ Watch history start saved: " + message);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error saving watch history start: " + error);
            }
        });
    }

    private void saveWatchHistoryPosition() {
        if (currentUserId == -1 || movieId == -1 || currentPosition <= 0) {
            return;
        }

        Log.d(TAG, "📝 Saving watch position: " + currentPosition + "ms for movie: " + movieId);

        watchHistoryService.saveWatchHistory(movieId, currentPosition, new WatchHistoryService.WatchHistoryOperationCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "✅ Watch position saved: " + message);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error saving watch position: " + error);
            }
        });
    }

    private void saveWatchHistoryBeforeExit() {
        if (currentUserId == -1 || movieId == -1) {
            return;
        }

        // Lấy vị trí hiện tại từ ExoPlayer nếu đang phát
        if (exoPlayer != null && exoPlayer.isPlaying()) {
            currentPosition = exoPlayer.getCurrentPosition();
        }

        Log.d(TAG, "📝 Saving watch history before exit. Position: " + currentPosition + "ms");
        saveWatchHistoryPosition();
    }

    private void hideAllPlayers() {
        youTubePlayerView.setVisibility(View.GONE);
        exoPlayerView.setVisibility(View.GONE);
        webViewPlayer.setVisibility(View.GONE);

        // Stop ExoPlayer if running
        if (exoPlayer != null && exoPlayer.isPlaying()) {
            exoPlayer.stop();
        }
    }

    private void showError(String message) {
        Log.e(TAG, "❌ Error: " + message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Lưu vị trí khi pause
        saveWatchHistoryPosition();

        if (exoPlayer != null && exoPlayer.isPlaying()) {
            exoPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (exoPlayer != null) {
            exoPlayer.play();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Lưu lịch sử khi activity bị stop
        saveWatchHistoryBeforeExit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Lưu lịch sử trước khi destroy
        saveWatchHistoryBeforeExit();

        // Release ExoPlayer
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }

        currentYouTubePlayer = null;

        // YouTube player will be automatically released by lifecycle
        Log.d(TAG, "🧹 PlayerActivity destroyed and resources released");
    }
}