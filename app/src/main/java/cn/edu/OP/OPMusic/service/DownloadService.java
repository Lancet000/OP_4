package cn.edu.OP.OPMusic.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;

import cn.edu.OP.OPMusic.Song;

public class DownloadService extends Service {
    public interface Listener {
        public void callBack(Song song, boolean success);
    }

    private static final String TAG = "DownloadService";
    private static final String MUSIC_URL_PREFIX = "http://music.163.com/song/media/outer/url?id=";
    private static final String MUSIC_URL_POSTFIX = ".mp3";
    private final HashSet<String> set = new HashSet<>();

    public void downloadMusic(Song song, Listener listener) {
        if (set.contains(song.getSong_name()))
            return;
        set.add(song.getSong_name());
        new Thread(() -> {
            Looper.prepare();
            // 检查私有文件夹中是否已存在该音乐文件
            File musicFile = new File(getFilesDir(), song.getSong_name() + ".mp3");
            try {
                if (musicFile.exists()) {
                    Log.i(TAG, "已存在音乐文件: " + musicFile.getAbsolutePath());
                    return;
                }
                // 创建URL对象并建立连接
                URL url = new URL(MUSIC_URL_PREFIX + song.getUrl() + MUSIC_URL_POSTFIX);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.connect();

                // 创建输出流和输入流
                FileOutputStream fileOutputStream = new FileOutputStream(musicFile);
                InputStream inputStream = connection.getInputStream();

                // 从输入流读取数据并写入文件
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
                // 关闭流
                fileOutputStream.close();
                inputStream.close();
                Toast.makeText(this, "音乐下载完成", Toast.LENGTH_SHORT).show();
                listener.callBack(song, true);
            } catch (Exception e) {
                e.printStackTrace();
                musicFile.delete();
                Toast.makeText(this, "音乐下载失败", Toast.LENGTH_SHORT).show();
                listener.callBack(song, false);
            }
        }).start();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return binder;
    }

    private final DownloadBinder binder = new DownloadBinder();

    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }
}