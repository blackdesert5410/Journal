package com.edu.journal.repository;

import com.edu.journal.db.bean.Song;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SongRepository {
    private static final String TAG = "SongRepository";
    private static SongRepository instance;
    private List<Song> songs;
    private Context context;
    private ExecutorService executorService;
    private boolean isShuttingDown = false;

    private SongRepository(Context context) {
        this.context = context.getApplicationContext();
        this.songs = new ArrayList<>();
        Log.i(TAG, "SongRepository 初始化");
    }

    public static synchronized SongRepository getInstance(Context context) {
        if (instance == null) {
            Log.i(TAG, "创建新的 SongRepository 实例");
            instance = new SongRepository(context);
        } else {
            Log.i(TAG, "返回现有的 SongRepository 实例");
        }
        return instance;
    }

    private ExecutorService getExecutorService() {
        if (isShuttingDown) {
            Log.w(TAG, "SongRepository 正在关闭，无法创建新的 ExecutorService");
            return null;
        }
        
        if (executorService == null || executorService.isShutdown()) {
            Log.i(TAG, "创建新的 ExecutorService");
            executorService = Executors.newSingleThreadExecutor();
        }
        return executorService;
    }

    public void loadSongs(OnSongsLoadedListener listener) {
        Log.i(TAG, "开始加载音乐文件");
        ExecutorService executor = getExecutorService();
        if (executor == null) {
            Log.e(TAG, "无法加载歌曲：ExecutorService 不可用");
            if (listener != null) {
                listener.onSongsLoaded(new ArrayList<>());
            }
            return;
        }
        
        executor.execute(() -> {
            ContentResolver contentResolver = context.getContentResolver();
            
            // 根据Android版本使用不同的URI
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10及以上版本
                uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
                Log.i(TAG, "使用Android 10+ URI: " + uri);
            } else {
                // Android 9及以下版本
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                Log.i(TAG, "使用旧版URI: " + uri);
            }
            
            String[] projection = {
                AudioColumns.TITLE,
                AudioColumns.ARTIST,
                AudioColumns.ALBUM,
                AudioColumns.DURATION,
                AudioColumns._ID,
                AudioColumns.DATA
            };

            String selection = AudioColumns.IS_MUSIC + " != 0";
            String sortOrder = AudioColumns.TITLE + " ASC";
            Log.i(TAG, "查询条件: " + selection);

            try (Cursor cursor = contentResolver.query(uri, projection, selection, null, sortOrder)) {
                if (cursor != null) {
                    Log.i(TAG, "查询成功，找到 " + cursor.getCount() + " 首歌曲");
                    
                    int titleColumn = cursor.getColumnIndex(AudioColumns.TITLE);
                    int artistColumn = cursor.getColumnIndex(AudioColumns.ARTIST);
                    int albumColumn = cursor.getColumnIndex(AudioColumns.ALBUM);
                    int durationColumn = cursor.getColumnIndex(AudioColumns.DURATION);
                    int idColumn = cursor.getColumnIndex(AudioColumns._ID);
                    int dataColumn = cursor.getColumnIndex(AudioColumns.DATA);
                    
                    Log.i(TAG, "列索引: title=" + titleColumn + ", artist=" + artistColumn + 
                          ", album=" + albumColumn + ", duration=" + durationColumn + 
                          ", id=" + idColumn + ", data=" + dataColumn);

                    songs.clear();
                    int count = 0;
                    while (cursor.moveToNext()) {
                        String title = cursor.getString(titleColumn);
                        String artist = cursor.getString(artistColumn);
                        String album = cursor.getString(albumColumn);
                        String duration = cursor.getString(durationColumn);
                        long id = cursor.getLong(idColumn);
                        String filePath = cursor.getString(dataColumn);
                        
                        Log.i(TAG, "加载歌曲: " + title + " - " + artist + " (" + album + ")");
                        Log.i(TAG, "文件路径: " + filePath);

                        Uri songUri = Uri.withAppendedPath(uri, String.valueOf(id));
                        Log.i(TAG, "歌曲 URI: " + songUri);
                        
                        Song song = new Song(title, artist, album, duration, songUri.toString(), filePath);
                        
                        // 设置歌曲授权状态
                        boolean isAuthorized = checkAuthorization(filePath);
                        song.setAuthorized(isAuthorized);
                        Log.i(TAG, "歌曲授权状态: " + (isAuthorized ? "已授权" : "未授权"));
                        
                        songs.add(song);
                        count++;
                    }
                    Log.i(TAG, "成功加载 " + count + " 首歌曲");
                    
                    if (listener != null) {
                        listener.onSongsLoaded(songs);
                    }
                } else {
                    Log.e(TAG, "查询返回的 Cursor 为空");
                    if (listener != null) {
                        listener.onSongsLoaded(new ArrayList<>());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "加载音乐文件时出错", e);
                if (listener != null) {
                    listener.onSongsLoaded(new ArrayList<>());
                }
            }
            
            Log.i(TAG, "歌曲列表大小: " + songs.size());
        });
    }

    /**
     * 检查歌曲是否为已授权的格式
     * @param filePath 文件路径
     * @return 如果是已授权的格式返回true，否则返回false
     */
    private boolean checkAuthorization(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false; // 如果文件路径为空，默认为未授权
        }
        
        // 检查是否为QQ音乐加密格式
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".mflac0") || 
            lowerPath.endsWith(".mflac") ||
            lowerPath.endsWith(".qmc0") ||
            lowerPath.endsWith(".qmc3") ||
            lowerPath.endsWith(".tkm")) {
            Log.i(TAG, "文件 " + filePath + " 是加密格式，标记为未授权");
            return false;
        }
        
        // 检查文件是否存在且可访问
        java.io.File file = new java.io.File(filePath);
        if (!file.exists() || !file.canRead()) {
            Log.i(TAG, "文件 " + filePath + " 不存在或无法访问，标记为未授权");
            return false;
        }
        
        // 检查文件大小是否为0
        if (file.length() == 0) {
            Log.i(TAG, "文件 " + filePath + " 大小为0，标记为未授权");
            return false;
        }
        
        // 暂时注释掉文件扩展名检查，以支持更多不常规的格式
        /*
        // 检查文件扩展名是否为常见音频格式
        String extension = getFileExtension(lowerPath);
        if (!isValidAudioExtension(extension)) {
            Log.i(TAG, "文件 " + filePath + " 不是有效的音频格式，标记为未授权");
            return false;
        }
        */
        
        Log.i(TAG, "文件 " + filePath + " 通过授权检查");
        return true;
    }

    /**
     * 获取文件扩展名
     * @param filePath 文件路径
     * @return 文件扩展名（小写）
     */
    private String getFileExtension(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filePath.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * 检查是否为有效的音频扩展名
     * @param extension 文件扩展名
     * @return 如果是有效的音频扩展名返回true，否则返回false
     */
    private boolean isValidAudioExtension(String extension) {
        if (extension.isEmpty()) {
            return false;
        }
        
        // 常见音频格式
        String[] validExtensions = {
            "mp3", "wav", "ogg", "flac", "aac", "m4a", "wma", "mid", "midi"
        };
        
        for (String validExt : validExtensions) {
            if (validExt.equals(extension)) {
                return true;
            }
        }
        
        return false;
    }

    public List<Song> getAllSongs() {
        Log.i(TAG, "获取所有歌曲，当前有 " + songs.size() + " 首歌曲");
        return new ArrayList<>(songs);
    }

    public Song getSong(int position) {
        if (position >= 0 && position < songs.size()) {
            Song song = songs.get(position);
            Log.i(TAG, "获取位置 " + position + " 的歌曲: " + song.getTitle());
            return song;
        }
        Log.e(TAG, "尝试获取无效位置的歌曲: " + position + ", 歌曲总数: " + songs.size());
        return null;
    }

    public int getSongCount() {
        Log.i(TAG, "获取歌曲总数: " + songs.size());
        return songs.size();
    }

    public void addOnlineSong(String title, String url) {
        Log.i(TAG, "添加在线歌曲: " + title + ", URL: " + url);
        Song song = new Song(title, url);
        songs.add(0, song); // 添加到列表开头
        Log.i(TAG, "在线歌曲已添加到列表开头");
    }

    public void shutdown() {
        Log.i(TAG, "开始关闭 SongRepository");
        isShuttingDown = true;
        
        if (executorService != null && !executorService.isShutdown()) {
            Log.i(TAG, "关闭 ExecutorService");
            executorService.shutdown();
            executorService = null;
        }
        
        isShuttingDown = false;
        Log.i(TAG, "SongRepository 关闭完成");
    }

    public interface OnSongsLoadedListener {
        void onSongsLoaded(List<Song> songs);
    }
}
