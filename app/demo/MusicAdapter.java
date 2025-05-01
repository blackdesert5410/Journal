package com.edu.practise.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.edu.practise.R;
import com.edu.practise.model.Song;

import java.util.List;

public class MusicAdapter extends ArrayAdapter<Song> {
    private static final String TAG = "MusicAdapter";

    private Context context;
    private List<Song> songs;

    public MusicAdapter(Context context, List<Song> songs) {
        super(context, 0, songs);
        this.context = context;
        this.songs = songs;
        Log.i(TAG, "MusicAdapter 初始化，歌曲数量: " + songs.size());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "获取位置 " + position + " 的视图");
        
        if (convertView == null) {
            Log.d(TAG, "创建新的视图");
            convertView = LayoutInflater.from(context).inflate(R.layout.music_item, parent, false);
        } else {
            Log.d(TAG, "重用现有视图");
        }

        Song song = songs.get(position);
        Log.d(TAG, "显示歌曲: " + song.getTitle());

        TextView titleTextView = convertView.findViewById(R.id.songTitle);
        TextView artistTextView = convertView.findViewById(R.id.songArtist);
        TextView albumTextView = convertView.findViewById(R.id.songAlbum);
        TextView durationTextView = convertView.findViewById(R.id.songDuration);
        TextView authStatusTextView = convertView.findViewById(R.id.songAuthStatus);

        titleTextView.setText(song.getTitle());
        artistTextView.setText(song.getArtist());
        albumTextView.setText(song.getAlbum());
        
        // 使用Song类中的授权状态
        if (!song.isAuthorized()) {
            Log.d(TAG, "未授权歌曲: " + song.getTitle());
            authStatusTextView.setText("未授权");
            authStatusTextView.setVisibility(View.VISIBLE);
            durationTextView.setText("--:--");
        } else {
            Log.d(TAG, "已授权歌曲: " + song.getTitle());
            authStatusTextView.setText("已授权");
            authStatusTextView.setVisibility(View.VISIBLE);
            durationTextView.setText(formatDuration(song.getDuration()));
        }
        
        Log.d(TAG, "设置文本: 标题=" + song.getTitle() + 
              ", 艺术家=" + song.getArtist() + 
              ", 专辑=" + song.getAlbum() + 
              ", 时长=" + (!song.isAuthorized() ? "--:--" : formatDuration(song.getDuration())));

        return convertView;
    }

    private String formatDuration(String duration) {
        try {
            if (duration == null || duration.isEmpty()) {
                Log.e(TAG, "时长为空");
                return "00:00";
            }
            long durationMs = Long.parseLong(duration);
            long minutes = (durationMs / 1000) / 60;
            long seconds = (durationMs / 1000) % 60;
            return String.format("%02d:%02d", minutes, seconds);
        } catch (NumberFormatException e) {
            Log.e(TAG, "格式化时长出错: " + duration, e);
            return "00:00";
        }
    }
}