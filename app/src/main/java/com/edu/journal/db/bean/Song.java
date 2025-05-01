package com.edu.journal.db.bean;

public class Song {
    private String title;       // 歌曲标题
    private String artist;      // 艺术家
    private String album;       // 专辑名
    private String duration;    // 时长
    private String uri;         // 文件URI
    private String filePath;    // 文件路径
    private boolean isAuthorized; // 是否已授权
    private boolean isOnline;   // 是否为在线音乐

    public Song(String title, String artist, String album, String duration, String uri, String filePath) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.uri = uri;
        this.filePath = filePath;
        this.isAuthorized = false; // 默认为未授权，由Repository设置
        this.isOnline = false;     // 默认为本地音乐
    }

    // 在线音乐的构造函数
    public Song(String title, String uri) {
        this.title = title;
        this.artist = "未知艺术家";
        this.album = "未知专辑";
        this.duration = "0";
        this.uri = uri;
        this.filePath = null;
        this.isAuthorized = true;  // 在线音乐默认为已授权
        this.isOnline = true;      // 标记为在线音乐
    }

    // Getters
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getDuration() { return duration; }
    public String getUri() { return uri; }
    public String getFilePath() { return filePath; }
    public boolean isAuthorized() { return isAuthorized; }
    public boolean isOnline() { return isOnline; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setArtist(String artist) { this.artist = artist; }
    public void setAlbum(String album) { this.album = album; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setUri(String uri) { this.uri = uri; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setAuthorized(boolean authorized) { this.isAuthorized = authorized; }
    public void setOnline(boolean online) { this.isOnline = online; }
} 