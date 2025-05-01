package com.edu.journal.ui;

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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.edu.journal.Constant;
import com.edu.journal.R;
import com.edu.journal.db.bean.Song;
import com.edu.journal.repository.SongRepository;
import com.edu.journal.ui.adapter.MusicAdapter;
import com.edu.journal.utils.MVUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener {
    private static final String TAG = "MusicActivity";
    private static final int PERMISSION_REQUEST_CODE = 1;

    private EditText edtUrl;
    private Button btnPlayUrl, btnPlayPause, btnPrevious, btnNext, btnRewind, btnForward;
    private ListView lvLocalSongs;
    private TextView tvSongInfo, tvCurrentTime, tvTotalTime;
    private SeekBar progressBar;
    private MediaPlayer mediaPlayer;
    private SongRepository songRepository;
    private MusicAdapter musicAdapter;
    private int currentSongPosition = -1;
    private Handler handler;
    private ExecutorService executorService;
    private boolean isPrepared = false;
    private boolean isSeeking = false;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate 开始");
        setContentView(R.layout.activity_music);

        executorService = Executors.newSingleThreadExecutor();
        initializeViews();
        
        // 检查是否需要请求权限
        if (checkAndRequestPermissions()) {
            // 如果不需要请求权限或权限已授予，初始化音乐列表
            initializeMusicList();
        }
        
        Log.i(TAG, "onCreate 完成");
    }

    /**
     * 检查并请求权限
     * @return 如果不需要请求权限或权限已授予，返回true；否则返回false
     */
    private boolean checkAndRequestPermissions() {
        // 获取存储权限
        String storagePermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            storagePermission = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        
        // 检查是否已经有权限
        if (ContextCompat.checkSelfPermission(this, storagePermission) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "已有存储权限，无需请求");
            return true;
        }
        
        // 检查是否有被拒绝的权限记录
        String deniedPermissionsJson = MVUtils.getString(Constant.DENIED_PERMISSIONS, "");
        if (!deniedPermissionsJson.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray(deniedPermissionsJson);
                List<String> permissionsToRequest = new ArrayList<>();
                
                // 检查存储权限是否在被拒绝的权限列表中
                boolean needStoragePermission = false;
                for (int i = 0; i < jsonArray.length(); i++) {
                    String permission = jsonArray.getString(i);
                    if (permission.equals(storagePermission)) {
                        needStoragePermission = true;
                        break;
                    }
                }
                
                // 如果需要请求存储权限，则请求
                if (needStoragePermission) {
                    Log.i(TAG, "请求存储权限");
                    ActivityCompat.requestPermissions(this, new String[]{storagePermission}, PERMISSION_REQUEST_CODE);
                    return false;
                }
            } catch (JSONException e) {
                Log.e(TAG, "解析被拒绝的权限列表失败", e);
            }
        }
        
        // 如果没有被拒绝的权限记录，也请求权限
        Log.i(TAG, "请求存储权限");
        ActivityCompat.requestPermissions(this, new String[]{storagePermission}, PERMISSION_REQUEST_CODE);
        return false;
    }

    private void initializeViews() {
        Log.i(TAG, "初始化视图");
        edtUrl = findViewById(R.id.edt_url);
        btnPlayUrl = findViewById(R.id.btn_play_url);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);
        btnRewind = findViewById(R.id.btn_rewind);
        btnForward = findViewById(R.id.btn_forward);
        lvLocalSongs = findViewById(R.id.lv_local_songs);
        tvSongInfo = findViewById(R.id.tv_song_info);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        progressBar = findViewById(R.id.seek_bar);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        handler = new Handler();
        
        // 设置下拉刷新
        swipeRefreshLayout.setOnRefreshListener(this::refreshMusicList);
        
        Log.i(TAG, "视图初始化完成");
    }

    /**
     * 刷新音乐列表
     */
    private void refreshMusicList() {
        Log.i(TAG, "刷新音乐列表");
        if (songRepository != null) {
            songRepository.loadSongs(songs -> {
                runOnUiThread(() -> {
                    if (songs.isEmpty()) {
                        Log.e(TAG, "没有找到任何歌曲");
                        Toast.makeText(this, "没有找到任何音乐文件", Toast.LENGTH_SHORT).show();
                    }
                    
                    if (musicAdapter != null) {
                        musicAdapter.updateSongs(songs);
                    } else {
                        musicAdapter = new MusicAdapter(this, songs);
                        lvLocalSongs.setAdapter(musicAdapter);
                    }
                    
                    swipeRefreshLayout.setRefreshing(false);
                    Log.i(TAG, "音乐列表刷新完成");
                });
            });
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void initializeMusicList() {
        Log.i(TAG, "初始化音乐列表");
        songRepository = SongRepository.getInstance(this);
        
        // 检查是否已经有加载好的歌曲
        List<Song> existingSongs = songRepository.getAllSongs();
        if (!existingSongs.isEmpty()) {
            Log.i(TAG, "使用已加载的歌曲列表，共 " + existingSongs.size() + " 首");
            musicAdapter = new MusicAdapter(this, existingSongs);
            lvLocalSongs.setAdapter(musicAdapter);
            setupClickListeners();
        } else {
            // 如果没有加载好的歌曲，则加载
            Log.i(TAG, "没有已加载的歌曲，开始加载");
            songRepository.loadSongs(songs -> {
                runOnUiThread(() -> {
                    if (songs.isEmpty()) {
                        Log.e(TAG, "没有找到任何歌曲");
                        Toast.makeText(this, "没有找到任何音乐文件", Toast.LENGTH_SHORT).show();
                    }
                    
                    musicAdapter = new MusicAdapter(this, songs);
                    lvLocalSongs.setAdapter(musicAdapter);
                    Log.i(TAG, "音乐列表初始化完成");
                    
                    // 在歌曲列表加载完成后设置点击监听器
                    setupClickListeners();
                });
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "存储权限已授予");
                // 权限已授予，初始化音乐列表
                initializeMusicList();
                
                // 从被拒绝的权限列表中移除已授予的权限
                removeGrantedPermission(permissions[0]);
            } else {
                Log.e(TAG, "存储权限被拒绝");
                Toast.makeText(this, "需要存储权限才能访问音乐文件", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * 从被拒绝的权限列表中移除已授予的权限
     * @param grantedPermission 已授予的权限
     */
    private void removeGrantedPermission(String grantedPermission) {
        String deniedPermissionsJson = MVUtils.getString(Constant.DENIED_PERMISSIONS, "");
        if (!deniedPermissionsJson.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray(deniedPermissionsJson);
                List<String> remainingPermissions = new ArrayList<>();
                
                // 检查权限是否在被拒绝的权限列表中
                for (int i = 0; i < jsonArray.length(); i++) {
                    String permission = jsonArray.getString(i);
                    if (!permission.equals(grantedPermission)) {
                        remainingPermissions.add(permission);
                    }
                }
                
                // 更新被拒绝的权限列表
                JSONArray newJsonArray = new JSONArray();
                for (String permission : remainingPermissions) {
                    newJsonArray.put(permission);
                }
                MVUtils.put(Constant.DENIED_PERMISSIONS, newJsonArray.toString());
            } catch (JSONException e) {
                Log.e(TAG, "更新被拒绝的权限列表失败", e);
            }
        }
    }

    private void setupClickListeners() {
        Log.i(TAG, "设置点击监听器");
        
        // 播放在线音乐
        btnPlayUrl.setOnClickListener(v -> {
            String url = edtUrl.getText().toString();
            if (!url.isEmpty()) {
                Log.i(TAG, "尝试播放在线音乐: " + url);
                downloadAndPlayOnlineMusic(url);
            } else {
                Toast.makeText(this, "请输入有效的音乐URL", Toast.LENGTH_SHORT).show();
            }
        });

        // 播放或暂停音乐
        btnPlayPause.setOnClickListener(v -> {
            if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                    Log.i(TAG, "暂停播放");
                mediaPlayer.pause();
                btnPlayPause.setText("播放");
            } else {
                    Log.i(TAG, "继续播放");
                mediaPlayer.start();
                btnPlayPause.setText("暂停");
                    updateProgressBar();
                }
            } else {
                Log.i(TAG, "无法播放: mediaPlayer 为 null");
            }
        });

        // 下一首
        btnNext.setOnClickListener(v -> playNextSong());

        // 上一首
        btnPrevious.setOnClickListener(v -> playPreviousSong());

        // 快退
        btnRewind.setOnClickListener(v -> {
            if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
                int rewindPosition = currentPosition - 10000; // 10秒
            if (rewindPosition < 0) rewindPosition = 0;
                Log.i(TAG, "快退: " + currentPosition + " -> " + rewindPosition);
            mediaPlayer.seekTo(rewindPosition);
            }
        });

        // 快进
        btnForward.setOnClickListener(v -> {
            if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
                int forwardPosition = currentPosition + 10000; // 10秒
            if (forwardPosition > mediaPlayer.getDuration()) forwardPosition = mediaPlayer.getDuration();
                Log.i(TAG, "快进: " + currentPosition + " -> " + forwardPosition);
            mediaPlayer.seekTo(forwardPosition);
            }
        });

        // 设置SeekBar监听器
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    Log.i(TAG, "进度条改变: " + progress);
                    mediaPlayer.seekTo(progress);
                    updateTimeDisplay(progress, mediaPlayer.getDuration());
                }
    }

    @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    Log.i(TAG, "开始拖动进度条，暂停播放");
                    isSeeking = true;
                    mediaPlayer.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    Log.i(TAG, "停止拖动进度条，恢复播放");
                    isSeeking = false;
                    mediaPlayer.start();
                }
            }
        });

        // 点击列表项播放音乐
        lvLocalSongs.setOnItemClickListener((parent, view, position, id) -> {
            Log.i(TAG, "点击了列表项: " + position);
            currentSongPosition = position;
            Song song = songRepository.getSong(position);
            if (song != null) {
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
        
        Log.i(TAG, "点击监听器设置完成");
    }

    private void downloadAndPlayOnlineMusic(String url) {
        Log.i(TAG, "开始下载在线音乐: " + url);
        Toast.makeText(this, "正在下载音乐...", Toast.LENGTH_SHORT).show();
        
        executorService.execute(() -> {
            try {
                // 创建MediaPlayer并设置数据源
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(url);
                mediaPlayer.setOnPreparedListener(this);
                
                // 异步准备
                mediaPlayer.prepareAsync();
                
                // 添加到本地列表
                String title = "在线音乐";
                songRepository.addOnlineSong(title, url);
                
                // 更新UI
                runOnUiThread(() -> {
                    Toast.makeText(this, "下载成功，开始播放", Toast.LENGTH_SHORT).show();
                    musicAdapter.notifyDataSetChanged();
                });
            } catch (IOException e) {
                Log.e(TAG, "下载在线音乐失败", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "无法下载该音乐: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
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
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "播放音乐时出错: what=" + what + ", extra=" + extra);
                runOnUiThread(() -> {
                    Toast.makeText(this, "无法播放此音乐文件", Toast.LENGTH_SHORT).show();
                    
                    // 如果当前播放的是本地音乐，将其标记为未授权
                    markCurrentSongAsUnauthorized();
                });
                return true;
            });
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
                
                // 如果当前播放的是本地音乐，将其标记为未授权
                markCurrentSongAsUnauthorized();
            }
        } catch (Exception e) {
            Log.e(TAG, "播放音乐时出错", e);
            Toast.makeText(this, "播放音乐时出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            
            // 如果当前播放的是本地音乐，将其标记为未授权
            markCurrentSongAsUnauthorized();
        }
    }

    /**
     * 将当前歌曲标记为未授权
     */
    private void markCurrentSongAsUnauthorized() {
        if (currentSongPosition >= 0 && currentSongPosition < songRepository.getSongCount()) {
            Song song = songRepository.getSong(currentSongPosition);
            if (song != null && !song.isOnline()) {
                Log.i(TAG, "将音乐 " + song.getTitle() + " 标记为未授权");
                song.setAuthorized(false);
                if (musicAdapter != null) {
                    musicAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "MediaPlayer 准备完成");
        isPrepared = true;
        runOnUiThread(() -> {
            if (currentSongPosition >= 0) {
                Song song = songRepository.getSong(currentSongPosition);
                if (song != null) {
                    tvSongInfo.setText("正在播放: " + song.getTitle());
                }
            } else {
                tvSongInfo.setText("正在播放: 在线音乐");
            }
            
            // 设置进度条最大值
            int duration = mediaPlayer.getDuration();
            progressBar.setMax(duration);
            tvTotalTime.setText(formatTime(duration));
            tvCurrentTime.setText("00:00");
            
            mediaPlayer.start();
            Log.i(TAG, "开始播放");
            btnPlayPause.setText("暂停");
            updateProgressBar();
        });
    }

    private void updateProgressBar() {
        Log.i(TAG, "开始更新进度条");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying() && !isSeeking) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    int duration = mediaPlayer.getDuration();
                    
                    // 更新进度条和时间显示
                    progressBar.setProgress(currentPosition);
                    updateTimeDisplay(currentPosition, duration);
                    
                    Log.d(TAG, "更新进度: " + currentPosition + "/" + duration);
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }
    
    private void updateTimeDisplay(int currentPosition, int duration) {
        tvCurrentTime.setText(formatTime(currentPosition));
        tvTotalTime.setText(formatTime(duration));
    }
    
    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void playNextSong() {
        Log.i(TAG, "播放下一首");
        if (currentSongPosition < songRepository.getSongCount() - 1) {
            currentSongPosition++;
            Song song = songRepository.getSong(currentSongPosition);
            if (song != null) {
                playMusic(song.getUri());
            }
        } else {
            Toast.makeText(this, "已经是最后一首歌曲", Toast.LENGTH_SHORT).show();
        }
    }

    private void playPreviousSong() {
        Log.i(TAG, "播放上一首");
        if (currentSongPosition > 0) {
            currentSongPosition--;
            Song song = songRepository.getSong(currentSongPosition);
            if (song != null) {
                playMusic(song.getUri());
            }
        } else {
            Toast.makeText(this, "已经是第一首歌曲", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            Log.i(TAG, "暂停播放");
            mediaPlayer.pause();
            btnPlayPause.setText("播放");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        // 如果音乐列表为空，重新初始化
        if (songRepository != null && songRepository.getSongCount() == 0) {
            Log.i(TAG, "音乐列表为空，重新初始化");
            initializeMusicList();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        try {
            if (mediaPlayer != null) {
                Log.i(TAG, "释放 MediaPlayer 资源");
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            }
            
            if (executorService != null && !executorService.isShutdown()) {
                Log.i(TAG, "关闭 executorService");
                executorService.shutdown();
                executorService = null;
            }
            
            if (handler != null) {
                Log.i(TAG, "清除 handler 消息");
                handler.removeCallbacksAndMessages(null);
                handler = null;
            }
            
            // 注意：不要在这里关闭 songRepository，因为它是一个单例，可能在其他地方还在使用
            // if (songRepository != null) {
            //     Log.i(TAG, "关闭 songRepository");
            //     songRepository.shutdown();
            //     songRepository = null;
            // }
        } catch (Exception e) {
            Log.e(TAG, "onDestroy 中发生异常", e);
        }
        Log.i(TAG, "onDestroy 完成");
    }
}