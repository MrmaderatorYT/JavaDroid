package com.ccs.javadroid;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.util.Locale;

/**
 * Оптимізований медіа-плеєр: аудіо + відео з hardware decoding,
 * керування гучністю, яскравістю, wake lock, landscape для відео.
 */
public class MediaPlayerActivity extends AppCompatActivity {

    private static final String EXTRA_FILE_PATH = "file_path";

    private MediaPlayer mediaPlayer;
    private VideoView videoView;
    private Handler handler;
    private AudioManager audioManager;
    private PowerManager.WakeLock wakeLock;
    private boolean isVideo = false;
    private boolean isPrepared = false;
    private boolean isUserSeeking = false;
    private boolean isLandscape = false;

    // UI
    private TextView tvTitle;
    private TextView tvStatus;
    private TextView tvCurrentTime;
    private TextView tvDuration;
    private SeekBar seekBar;
    private SeekBar volumeBar;
    private SeekBar brightnessBar;
    private TextView btnPlayPause;
    private TextView btnStop;
    private TextView btnRew;
    private TextView btnFwd;
    private TextView btnLandscape;
    private LinearLayout audioControls;
    private LinearLayout videoControls;
    private FrameLayout videoContainer;
    private TextView tvVolume;
    private TextView tvBrightness;
    private ProgressBar bufferProgress;

    private int accentColor = 0xFF4A86C8;
    private int bgColor = 0xFF2B2B2B;
    private int toolbarColor = 0xFF3C3F41;
    private int textColor = 0xFFBBBBBB;
    private int dimColor = 0xFF808080;

    public static void launch(Context context, File mediaFile) {
        Intent i = new Intent(context, MediaPlayerActivity.class);
        i.putExtra(EXTRA_FILE_PATH, mediaFile.getAbsolutePath());
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColors();
        handler = new Handler(Looper.getMainLooper());
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // Wake lock для тривалого відтворення
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                | PowerManager.ON_AFTER_RELEASE, "JavaDroid:Media");
        wakeLock.acquire(10 * 60 * 1000L); // 10 хвилин

        String filePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        if (filePath == null) { finish(); return; }
        File mediaFile = new File(filePath);
        if (!mediaFile.exists()) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        isVideo = isVideoFile(mediaFile.getName());

        // Відео — fullscreen landscape
        if (isVideo) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bgColor);

        // Toolbar
        Toolbar toolbar = new Toolbar(this);
        toolbar.setBackgroundColor(toolbarColor);
        toolbar.setTitle(isVideo ? "Video Player" : "Audio Player");
        toolbar.setTitleTextColor(textColor);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Title
        tvTitle = new TextView(this);
        tvTitle.setPadding(dp(16), dp(8), dp(16), dp(4));
        tvTitle.setTextColor(textColor);
        tvTitle.setTextSize(14);
        tvTitle.setMaxLines(1);
        tvTitle.setText(mediaFile.getName());

        // Status
        tvStatus = new TextView(this);
        tvStatus.setPadding(dp(16), 0, dp(16), dp(4));
        tvStatus.setTextColor(dimColor);
        tvStatus.setTextSize(12);
        tvStatus.setText("Ready");

        // ═══ Відео-контейнер ═══
        videoContainer = new FrameLayout(this);
        videoContainer.setBackgroundColor(0xFF000000);
        videoContainer.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));
        videoContainer.setVisibility(isVideo ? View.VISIBLE : View.GONE);

        videoView = new VideoView(this);
        videoContainer.addView(videoView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Buffer progress
        bufferProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bufferProgress.setIndeterminate(false);
        bufferProgress.setMax(100);
        FrameLayout.LayoutParams bufLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(4));
        bufLp.gravity = android.view.Gravity.BOTTOM;
        bufferProgress.setLayoutParams(bufLp);
        videoContainer.addView(bufferProgress);

        // ═══ Відео-контроли (знизу відео) ═══
        videoControls = new LinearLayout(this);
        videoControls.setOrientation(LinearLayout.VERTICAL);
        videoControls.setBackgroundColor(0xCC000000);
        videoControls.setPadding(dp(12), dp(6), dp(12), dp(6));
        videoControls.setVisibility(isVideo ? View.VISIBLE : View.GONE);

        // Seek bar для відео
        LinearLayout videoTimeRow = new LinearLayout(this);
        videoTimeRow.setOrientation(LinearLayout.HORIZONTAL);
        videoTimeRow.setGravity(Gravity.CENTER_VERTICAL);

        tvCurrentTime = new TextView(this);
        tvCurrentTime.setText("00:00");
        tvCurrentTime.setTextColor(0xFFFFFFFF);
        tvCurrentTime.setTextSize(11);
        tvCurrentTime.setTypeface(Typeface.MONOSPACE);

        tvDuration = new TextView(this);
        tvDuration.setText("00:00");
        tvDuration.setTextColor(0xFFFFFFFF);
        tvDuration.setTextSize(11);
        tvDuration.setTypeface(Typeface.MONOSPACE);
        tvDuration.setGravity(Gravity.END);
        tvDuration.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        videoTimeRow.addView(tvCurrentTime);
        videoTimeRow.addView(tvDuration);

        seekBar = new SeekBar(this);
        seekBar.setMax(1000);
        seekBar.getProgressDrawable().setColorFilter(accentColor, android.graphics.PorterDuff.Mode.SRC_IN);
        seekBar.getThumb().setColorFilter(accentColor, android.graphics.PorterDuff.Mode.SRC_IN);

        // Кнопки відео
        LinearLayout videoBtnRow = new LinearLayout(this);
        videoBtnRow.setOrientation(LinearLayout.HORIZONTAL);
        videoBtnRow.setGravity(Gravity.CENTER);
        videoBtnRow.setPadding(0, dp(4), 0, 0);

        btnRew = createVideoButton("⏪");
        btnPlayPause = createVideoButton("▶");
        btnFwd = createVideoButton("⏩");
        btnLandscape = createVideoButton("⛶");

        videoBtnRow.addView(btnRew);
        videoBtnRow.addView(btnPlayPause);
        videoBtnRow.addView(btnFwd);
        videoBtnRow.addView(btnLandscape);

        videoControls.addView(videoTimeRow);
        videoControls.addView(seekBar);
        videoControls.addView(videoBtnRow);

        FrameLayout.LayoutParams ctrlLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ctrlLp.gravity = android.view.Gravity.BOTTOM;
        videoControls.setLayoutParams(ctrlLp);

        // ═══ Аудіо-контроли ═══
        audioControls = new LinearLayout(this);
        audioControls.setOrientation(LinearLayout.VERTICAL);
        audioControls.setPadding(dp(16), dp(16), dp(16), dp(16));
        audioControls.setVisibility(isVideo ? View.GONE : View.VISIBLE);

        // SeekBar для аудіо
        LinearLayout audioTimeRow = new LinearLayout(this);
        audioTimeRow.setOrientation(LinearLayout.HORIZONTAL);
        audioTimeRow.setGravity(Gravity.CENTER_VERTICAL);

        tvCurrentTime = new TextView(this);
        tvCurrentTime.setText("00:00");
        tvCurrentTime.setTextColor(dimColor);
        tvCurrentTime.setTextSize(12);
        tvCurrentTime.setTypeface(Typeface.MONOSPACE);

        tvDuration = new TextView(this);
        tvDuration.setText("00:00");
        tvDuration.setTextColor(dimColor);
        tvDuration.setTextSize(12);
        tvDuration.setTypeface(Typeface.MONOSPACE);
        tvDuration.setGravity(Gravity.END);
        tvDuration.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        audioTimeRow.addView(tvCurrentTime);
        audioTimeRow.addView(tvDuration);

        seekBar = new SeekBar(this);
        seekBar.setMax(1000);

        // Кнопки аудіо
        LinearLayout audioBtnRow = new LinearLayout(this);
        audioBtnRow.setOrientation(LinearLayout.HORIZONTAL);
        audioBtnRow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams abrLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        abrLp.topMargin = dp(12);
        audioBtnRow.setLayoutParams(abrLp);

        btnRew = createButton("⏪ 10s");
        btnPlayPause = createButton("▶  Play");
        btnFwd = createButton("10s  ⏩");

        audioBtnRow.addView(btnRew);
        audioBtnRow.addView(btnPlayPause);
        audioBtnRow.addView(btnFwd);

        // Volume control
        LinearLayout volRow = new LinearLayout(this);
        volRow.setOrientation(LinearLayout.HORIZONTAL);
        volRow.setGravity(Gravity.CENTER_VERTICAL);
        volRow.setPadding(0, dp(12), 0, 0);

        tvVolume = new TextView(this);
        tvVolume.setText("🔊");
        tvVolume.setTextSize(14);

        volumeBar = new SeekBar(this);
        volumeBar.setMax(100);
        volumeBar.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeBar.setProgress((int) ((long) curVol * 100 / maxVol));

        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (!fromUser) return;
                int vol = (int) ((long) progress * maxVol / 100);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        volRow.addView(tvVolume);
        volRow.addView(volumeBar);

        // Brightness control
        LinearLayout brRow = new LinearLayout(this);
        brRow.setOrientation(LinearLayout.HORIZONTAL);
        brRow.setGravity(Gravity.CENTER_VERTICAL);
        brRow.setPadding(0, dp(4), 0, 0);

        tvBrightness = new TextView(this);
        tvBrightness.setText("☀");
        tvBrightness.setTextSize(14);

        brightnessBar = new SeekBar(this);
        brightnessBar.setMax(100);
        brightnessBar.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        try {
            int brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            brightnessBar.setProgress((int) ((long) brightness * 100 / 255));
        } catch (Settings.SettingNotFoundException e) {
            brightnessBar.setProgress(50);
        }

        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (!fromUser) return;
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.screenBrightness = progress / 100f;
                getWindow().setAttributes(lp);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        brRow.addView(tvBrightness);
        brRow.addView(brightnessBar);

        audioControls.addView(audioTimeRow);
        audioControls.addView(seekBar);
        audioControls.addView(audioBtnRow);
        audioControls.addView(volRow);
        audioControls.addView(brRow);

        // ═══ Збірка ═══
        root.addView(toolbar);
        root.addView(tvTitle);
        root.addView(tvStatus);
        root.addView(videoContainer);
        root.addView(videoControls);
        root.addView(audioControls);
        setContentView(root);
        FullScreenHelper.enable(this);
        // Спільний seekbar listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (!fromUser || !isPrepared) return;
                int dur = isVideo ? videoView.getDuration() : mediaPlayer.getDuration();
                if (dur > 0) {
                    int pos = (int) ((long) progress * dur / 1000);
                    if (isVideo) videoView.seekTo(pos);
                    else mediaPlayer.seekTo(pos);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar sb) { isUserSeeking = true; }
            @Override public void onStopTrackingTouch(SeekBar sb) { isUserSeeking = false; }
        });

        initMediaPlayer(mediaFile);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isVideo && videoView != null && videoView.isPlaying()) {
            videoView.pause();
            btnPlayPause.setText("▶");
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setText("▶  Play");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Відновити гучність
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (volumeBar != null) volumeBar.setProgress((int) ((long) curVol * 100 / maxVol));
    }

    private void initMediaPlayer(File file) {
        Uri uri = Uri.fromFile(file);

        if (isVideo) {
            initVideoPlayer(uri);
        } else {
            initAudioPlayer(uri);
        }

        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnStop.setOnClickListener(v -> stopPlayback());
        btnRew.setOnClickListener(v -> seekRelative(-10000));
        btnFwd.setOnClickListener(v -> seekRelative(10000));
        if (btnLandscape != null) {
            btnLandscape.setOnClickListener(v -> toggleOrientation());
        }
    }

    private void initVideoPlayer(Uri uri) {
        videoView.setVideoURI(uri);

        videoView.setOnPreparedListener(mp -> {
            isPrepared = true;
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            tvDuration.setText(formatDuration(mp.getDuration()));
            videoView.start();
            tvStatus.setText("Playing");
            btnPlayPause.setText("⏸");
            startVideoSeekBarUpdate();
        });

        videoView.setOnCompletionListener(mp -> {
            tvStatus.setText("Finished");
            btnPlayPause.setText("▶");
            seekBar.setProgress(0);
            tvCurrentTime.setText("00:00");
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            tvStatus.setText("Error: " + what + " / " + extra);
            return true;
        });

        // Buffer tracking
        videoView.setOnInfoListener((mp, what, extra) -> {
            if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                bufferProgress.setVisibility(View.VISIBLE);
            } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                bufferProgress.setVisibility(View.GONE);
            } else if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                bufferProgress.setVisibility(View.GONE);
            }
            return false;
        });

        videoView.start();
    }

    private void initAudioPlayer(Uri uri) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);

            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                tvDuration.setText(formatDuration(mp.getDuration()));
                tvStatus.setText("Ready — tap Play");
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                tvStatus.setText("Finished");
                btnPlayPause.setText("▶  Play");
                seekBar.setProgress(0);
                tvCurrentTime.setText("00:00");
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                tvStatus.setText("Error: " + what);
                return true;
            });

            mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
                // SeekBar secondary progress = buffer position
                seekBar.setSecondaryProgress(percent * 10);
            });

            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            tvStatus.setText("Error: " + e.getMessage());
        }
    }

    private void togglePlayPause() {
        if (isVideo) {
            if (!isPrepared) return;
            if (videoView.isPlaying()) {
                videoView.pause();
                btnPlayPause.setText("▶");
                tvStatus.setText("Paused");
            } else {
                videoView.start();
                btnPlayPause.setText("⏸");
                tvStatus.setText("Playing");
            }
            return;
        }

        if (mediaPlayer == null || !isPrepared) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setText("▶  Play");
            tvStatus.setText("Paused");
        } else {
            mediaPlayer.start();
            btnPlayPause.setText("❚❚  Pause");
            tvStatus.setText("Playing");
            startAudioSeekBarUpdate();
        }
    }

    private void stopPlayback() {
        if (isVideo) {
            videoView.stopPlayback();
            isPrepared = false;
            tvStatus.setText("Stopped");
            btnPlayPause.setText("▶");
            seekBar.setProgress(0);
            tvCurrentTime.setText("00:00");
            // Перезапуск для можливості play знову
            File f = new File(getIntent().getStringExtra(EXTRA_FILE_PATH));
            initVideoPlayer(Uri.fromFile(f));
            return;
        }
        if (mediaPlayer != null && isPrepared) {
            mediaPlayer.seekTo(0);
            mediaPlayer.pause();
            seekBar.setProgress(0);
            tvCurrentTime.setText("00:00");
            btnPlayPause.setText("▶  Play");
            tvStatus.setText("Stopped");
        }
    }

    private void seekRelative(int deltaMs) {
        if (isVideo && isPrepared) {
            int pos = videoView.getCurrentPosition();
            int dur = videoView.getDuration();
            int newPos = Math.max(0, Math.min(dur, pos + deltaMs));
            videoView.seekTo(newPos);
        } else if (mediaPlayer != null && isPrepared) {
            int pos = mediaPlayer.getCurrentPosition();
            int dur = mediaPlayer.getDuration();
            int newPos = Math.max(0, Math.min(dur, pos + deltaMs));
            mediaPlayer.seekTo(newPos);
        }
    }

    private void toggleOrientation() {
        isLandscape = !isLandscape;
        setRequestedOrientation(isLandscape
                ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                : ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
    }

    private void startVideoSeekBarUpdate() {
        Runnable updater = new Runnable() {
            @Override
            public void run() {
                if (videoView == null || !isPrepared) return;
                if (!videoView.isPlaying() && !isUserSeeking) {
                    handler.postDelayed(this, 500);
                    return;
                }
                if (!isUserSeeking) {
                    int pos = videoView.getCurrentPosition();
                    int dur = videoView.getDuration();
                    if (dur > 0) {
                        seekBar.setProgress((int) ((long) pos * 1000 / dur));
                    }
                    tvCurrentTime.setText(formatDuration(pos));
                }
                handler.postDelayed(this, 250);
            }
        };
        handler.post(updater);
    }

    private void startAudioSeekBarUpdate() {
        Runnable updater = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer == null || !isPrepared) return;
                if (!mediaPlayer.isPlaying() && !isUserSeeking) {
                    handler.postDelayed(this, 500);
                    return;
                }
                if (!isUserSeeking) {
                    int pos = mediaPlayer.getCurrentPosition();
                    int dur = mediaPlayer.getDuration();
                    if (dur > 0) {
                        seekBar.setProgress((int) ((long) pos * 1000 / dur));
                    }
                    tvCurrentTime.setText(formatDuration(pos));
                }
                handler.postDelayed(this, 250);
            }
        };
        handler.post(updater);
    }

    private TextView createButton(String text) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextColor(accentColor);
        btn.setTextSize(14);
        btn.setPadding(dp(20), dp(10), dp(20), dp(10));
        btn.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(dp(12));
        btn.setLayoutParams(lp);
        return btn;
    }

    private TextView createVideoButton(String text) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextColor(0xFFFFFFFF);
        btn.setTextSize(18);
        btn.setPadding(dp(16), dp(6), dp(16), dp(6));
        btn.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(dp(8));
        btn.setLayoutParams(lp);
        return btn;
    }

    private String formatDuration(int ms) {
        int totalSec = ms / 1000;
        int hours = totalSec / 3600;
        int min = (totalSec % 3600) / 60;
        int sec = totalSec % 60;
        if (hours > 0) {
            return String.format(Locale.US, "%d:%02d:%02d", hours, min, sec);
        }
        return String.format(Locale.US, "%02d:%02d", min, sec);
    }

    private boolean isVideoFile(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".avi")
                || lower.endsWith(".webm") || lower.endsWith(".3gp") || lower.endsWith(".mov");
    }

    @Override
    protected void onDestroy() {
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
        if (videoView != null) {
            videoView.stopPlayback();
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void applyColors() {
        try {
            AppPreferences prefs = new AppPreferences(this);
            AppTheme theme = AppTheme.byId(prefs.getThemeId(), prefs);
            accentColor = theme.accent;
            bgColor = theme.bg;
            toolbarColor = theme.toolbar;
            textColor = theme.text;
            dimColor = theme.textDim;
        } catch (Throwable ignored) {}
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
