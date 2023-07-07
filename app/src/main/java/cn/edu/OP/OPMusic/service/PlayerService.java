package cn.edu.OP.OPMusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerService extends Service {
    public MediaPlayer player;
    public static final String TAG = "MusicService";
    //播放下一首
    public static final int NEXT = 1;
    //播放上一首
    public static final int PREVIOUS = -1;
    //用于显示播放列表的数据源
    private List<SongInfo> list = new ArrayList<>();
    private Map<String, Integer> map = new HashMap<>();
    //当前播放的歌曲索引
    private int currentIndex = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
        getList();
    }

    //  通过 Binder 来保持 Activity 和 Service 的通信
    public MyMusicBinder binder = new MyMusicBinder();

    public class MyMusicBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
    }

    //获取歌曲列表
    public void getList() {
        File internalDir = getFilesDir();
        File[] files = internalDir.listFiles((file, name) -> name.endsWith(".mp3"));
        list.clear();
        if (files != null) {
            int index = 0;
            for (File musicFile : files) {
                //获取当前目录的名称和绝对路径
                String path = musicFile.getAbsolutePath();
                String name = musicFile.getName().replace(".mp3", "");
                list.add(new SongInfo(name, path));
                map.put(name, index++);
            }
        }
    }

    private static class SongInfo {
        String name;
        String path;

        public SongInfo(String name, String path) {
            this.path = path;
            this.name = name;
        }
    }

    //播放音乐
    private String play(int index) throws IOException {
        SongInfo info = list.get(index);
        player.reset();
        player.setDataSource(info.path);
        player.prepare();
        player.start();
        currentIndex = index;
        return info.name;
    }

    public String play(String name) throws IOException {
        if (map.get(name) != null)
            return play(map.get(name));
        return "没有歌曲";
    }

    //按照播放模式播放音乐
    public String playNext(int action) throws IOException {
        if (list.size() > 0) {
            int newIndex = 0;
            switch (action) {
                case NEXT:
                    newIndex = (currentIndex + 1) % list.size();
                    break;
                case PREVIOUS:
                    newIndex = currentIndex - 1;
                    if (newIndex < 0) {
                        newIndex = list.size() - 1;
                    }
                    break;
            }
            return play(newIndex);
        }
        return "没有歌曲";
    }

    //播放/暂停
    public void playOrPause() {
        if (player.isPlaying())
            player.pause();
        else
            player.start();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }
}