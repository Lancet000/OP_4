package cn.edu.OP.OPMusic;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.LinkedList;

import cn.edu.OP.OPMusic.databinding.ActivityMainBinding;
import cn.edu.OP.OPMusic.service.DownloadService;

public class MainActivity extends AppCompatActivity {
    private boolean downloading;
    private ActivityMainBinding binding;
    private DownloadService downloadService;

    private final LinkedList<Song> songs = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent serviceIntent = new Intent(this, DownloadService.class);
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);

        songs.add(new Song("屋顶", "5257138"));
        songs.add(new Song("布拉格广场", "185790"));
        songs.add(new Song("刀马旦", "255020"));
        songs.add(new Song("入海", "1449782341"));
        songs.add(new Song("富士山下", "64517"));
        binding.songsList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songs));

        binding.songsList.setOnItemClickListener(((adapterView, view, i, l) -> {
            if (downloading) {
                Toast.makeText(this, "请等待歌曲下载完成", Toast.LENGTH_SHORT).show();
                return;
            }
            Song song = songs.get(i);
            File musicFile = new File(getFilesDir(), song.getSong_name() + ".mp3");
            if (!musicFile.exists()) {
                Toast.makeText(this, "音乐未下载", Toast.LENGTH_SHORT).show();
                downloading = true;
                downloadService.downloadMusic(song, (song1, success) -> {
                    downloading = false;
                    if (success) {
                        Intent intent = new Intent(this, PlayerActivity.class);
                        intent.putExtra("song", song1.getSong_name());
                        startActivity(intent);
                    }
                });
            } else {
                Intent intent = new Intent(this, PlayerActivity.class);
                intent.putExtra("song", song.getSong_name());
                startActivity(intent);
            }
        }));
    }

    //  回调onServiceConnected 函数，通过IBinder 获取 Service对象，实现Activity与 Service的绑定
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadService = ((DownloadService.DownloadBinder) (service)).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            downloadService = null;
        }
    };
}