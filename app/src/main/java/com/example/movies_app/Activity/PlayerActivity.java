package com.example.movies_app.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.movies_app.R;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.ui.PlayerView;

public class PlayerActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    private ProgressBar progressBar;
    private ImageView backBtn;
    private TextView titleTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Khởi tạo UI
        initView();

        String title = getIntent().getStringExtra("title");
        if (title != null && !title.isEmpty() && titleTxt != null) {
            titleTxt.setText(title);
        }

        String videoUrl = getIntent().getStringExtra("videoUrl");
        if (videoUrl != null && !videoUrl.isEmpty()) {
            playVideo(videoUrl);
        } else {
            // Hiển thị thông báo lỗi nếu không có URL
            Toast.makeText(this, "Không có URL video để phát", Toast.LENGTH_LONG).show();
        }
    }

    private void initView() {
        playerView = findViewById(R.id.playerView);
        progressBar = findViewById(R.id.progressBar);
        backBtn = findViewById(R.id.backBtn);
        titleTxt = findViewById(R.id.movieTitle);

        // Xử lý nút back
        backBtn.setOnClickListener(v -> finish());

        // Khởi tạo ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // Thiết lập listener để hiển thị/ẩn progressBar
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    progressBar.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void playVideo(String videoUrl) {
        // Hiển thị progressBar khi bắt đầu tải video
        progressBar.setVisibility(View.VISIBLE);

        // Tạo MediaItem từ URL
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));

        // Thiết lập MediaItem cho player
        player.setMediaItem(mediaItem);

        // Chuẩn bị player
        player.prepare();

        // Bắt đầu phát
        player.play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}