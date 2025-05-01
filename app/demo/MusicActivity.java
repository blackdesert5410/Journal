package com.edu.practise;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.edu.practise.adapter.MusicAdapter;
import com.edu.practise.model.Song;
import com.edu.practise.repository.SongRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener {
    private static final String TAG = "MusicActivity";
    private static final int PERMISSION_REQUEST_CODE = 1;

    private ListView musicListView;
    private Button btnPlay, btnPause, btnStop;
    private SeekBar progressBar;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private MusicAdapter musicAdapter;
    private SongRepository songRepository;
    private int currentSongPosition = 0;
    private ExecutorService executorService;
    private boolean isPrepared = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate 开始");
        setContentView(R.layout.activity_music);

        executorService = Executors.newSingleThreadExecutor();
        initializeViews();
        checkPermissions();
        setupClickListeners();
        Log.i(TAG, "onCreate 完成");
    }

    private void initializeViews() {
        Log.i(TAG, "初始化视图");
        musicListView = findViewById(R.id.musicListView);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnStop = findViewById(R.id.btnStop);
        progressBar = findViewById(R.id.progressBar);
        handler = new Handler();
        Log.i(TAG, "视图初始化完成");
    }

    private void checkPermissions() {
        Log.i(TAG, "检查权限");
        
        // 根据Android版本请求不同的权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10及以上版本使用READ_MEDIA_AUDIO权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) 
                    != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "请求READ_MEDIA_AUDIO权限");
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQUEST_CODE);
            } else {
                Log.i(TAG, "已有READ_MEDIA_AUDIO权限，初始化音乐列表");
                initializeMusicList();
            }
        } else {
            // Android 9及以下版本使用READ_EXTERNAL_STORAGE权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "请求READ_EXTERNAL_STORAGE权限");
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                Log.i(TAG, "已有READ_EXTERNAL_STORAGE权限，初始化音乐列表");
                initializeMusicList();
            }
        }
    }

    private void initializeMusicList() {
        Log.i(TAG, "初始化音乐列表");
        executorService.execute(() -> {
            songRepository = SongRepository.getInstance(this);
            List<Song> songs = songRepository.getAllSongs();
            Log.i(TAG, "获取到 " + songs.size() + " 首歌曲");
            
            runOnUiThread(() -> {
                if (songs.isEmpty()) {
                    Log.e(TAG, "没有找到任何歌曲");
                    Toast.makeText(this, "没有找到任何音乐文件", Toast.LENGTH_SHORT).show();
                }
                
                musicAdapter = new MusicAdapter(this, songs);
                musicListView.setAdapter(musicAdapter);
                Log.i(TAG, "音乐列表初始化完成");
            });
        });
    }

    private void setupClickListeners() {
        Log.i(TAG, "设置点击监听器");
        musicListView.setOnItemClickListener((parent, view, position, id) -> {
            Log.i(TAG, "点击了列表项: " + position);
            currentSongPosition = position;
            Song song = songRepository.getSong(position);
            if (song != null) {
                // 使用Song类中的授权状态
                if (!song.isAuthorized()) {
                    Log.i(TAG, "尝试播放未授权歌曲: " + song.getTitle());
                    Toast.makeText(this, "该歌曲未授权，无法播放", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Log.i(TAG, "播放歌曲: " + song.getTitle());
                playMusic(song.getUri());
            } else {
                Log.e(TAG, "无法获取位置 " + position + " 的歌曲");
            }
        });

        btnPlay.setOnClickListener(v -> {
            Log.i(TAG, "点击播放按钮");
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                Log.i(TAG, "开始播放");
                updateProgressBar();
            } else {
                Log.i(TAG, "无法播放: mediaPlayer=" + (mediaPlayer == null ? "null" : "not null") + 
                      ", isPlaying=" + (mediaPlayer != null ? mediaPlayer.isPlaying() : "N/A"));
            }
        });

        btnPause.setOnClickListener(v -> {
            Log.i(TAG, "点击暂停按钮");
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                Log.i(TAG, "暂停播放");
            } else {
                Log.i(TAG, "无法暂停: mediaPlayer=" + (mediaPlayer == null ? "null" : "not null") + 
                      ", isPlaying=" + (mediaPlayer != null ? mediaPlayer.isPlaying() : "N/A"));
            }
        });

        btnStop.setOnClickListener(v -> {
            Log.i(TAG, "点击停止按钮");
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                progressBar.setProgress(0);
                Log.i(TAG, "停止播放并释放资源");
            } else {
                Log.i(TAG, "无法停止: mediaPlayer 为 null");
            }
        });

        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    Log.i(TAG, "进度条改变: " + progress);
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    Log.i(TAG, "开始拖动进度条，暂停播放");
                    mediaPlayer.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    Log.i(TAG, "停止拖动进度条，恢复播放");
                    mediaPlayer.start();
                }
            }
        });
        Log.i(TAG, "点击监听器设置完成");
    }

    private void playMusic(String uriString) {
        Log.i(TAG, "准备播放音乐: " + uriString);
        try {
            if (mediaPlayer != null) {
                Log.i(TAG, "释放之前的 MediaPlayer");
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(this);
            Log.i(TAG, "创建新的 MediaPlayer");

            Uri songUri = Uri.parse(uriString);
            Log.i(TAG, "解析 URI: " + songUri);
            
            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(songUri, "r");
            if (pfd != null) {
                Log.i(TAG, "成功打开文件描述符");
                mediaPlayer.setDataSource(pfd.getFileDescriptor());
                pfd.close();
                Log.i(TAG, "设置数据源并关闭文件描述符");
                
                // 使用异步准备
                mediaPlayer.prepareAsync();
                Log.i(TAG, "MediaPlayer 开始异步准备");
            } else {
                Log.e(TAG, "无法打开文件描述符");
                Toast.makeText(this, "无法读取音频文件", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "播放音乐时出错", e);
            Toast.makeText(this, "播放音乐时出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "MediaPlayer 准备完成");
        isPrepared = true;
        runOnUiThread(() -> {
            progressBar.setMax(mediaPlayer.getDuration());
            Log.i(TAG, "设置进度条最大值: " + mediaPlayer.getDuration());
            mediaPlayer.start();
            Log.i(TAG, "开始播放");
            updateProgressBar();
        });
    }

    private void updateProgressBar() {
        Log.i(TAG, "开始更新进度条");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    progressBar.setProgress(currentPosition);
                    Log.d(TAG, "更新进度条: " + currentPosition);
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "权限请求结果: " + requestCode);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "权限已授予");
                initializeMusicList();
            } else {
                Log.e(TAG, "权限被拒绝");
                Toast.makeText(this, "需要存储权限才能访问音乐文件", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        if (mediaPlayer != null) {
            Log.i(TAG, "释放 MediaPlayer 资源");
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (executorService != null) {
            executorService.shutdown();
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
