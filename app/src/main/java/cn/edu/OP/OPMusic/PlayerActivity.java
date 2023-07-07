package cn.edu.OP.OPMusic;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.MotionEvent;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import cn.edu.OP.OPMusic.databinding.ActivityPlayerBinding;
import cn.edu.OP.OPMusic.service.PlayerService;


public class PlayerActivity extends AppCompatActivity {
    private ActivityPlayerBinding binding;
    private final SimpleDateFormat formatter = new SimpleDateFormat("mm:ss", Locale.getDefault());
    private PlayerService playerService;
    private String songName;
    public final static String TAG = "MainActivity";
    //快进快退
    private final Handler updaterHandler = new Handler();
    public int value;
    private boolean forward = false;
    private boolean backward = false;

    private class forwardRunnable implements Runnable {
        public void run() {
            if (forward) {
                value += 10;
                playerService.player.seekTo(playerService.player.getCurrentPosition() + value);
                playerService.player.pause();
                updaterHandler.postDelayed(new forwardRunnable(), 50);
            }
            if (backward) {
                value -= 10;
                playerService.player.seekTo(playerService.player.getCurrentPosition() - value);
                playerService.player.pause();
                updaterHandler.postDelayed(new forwardRunnable(), 50);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        songName = getIntent().getStringExtra("song");
        bindServiceConnection();
        bindListener();
    }

    //  在Activity中调用 bindService 保持与 Service 的通信
    private void bindServiceConnection() {
        Intent intent = new Intent(this, PlayerService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    //  回调onServiceConnected 函数，通过IBinder 获取 Service对象，实现Activity与 Service的绑定
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playerService = ((PlayerService.MyMusicBinder) (service)).getService();
            try {
                binding.Title.setText(playerService.play(songName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            handler.postDelayed(refresh, 200);
            refreshMusicView();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playerService = null;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private void bindListener() {
        //拉动进度条
        binding.MusicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    playerService.player.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //下一首监听
        binding.Next.setOnClickListener(v -> {
            try {
                String name = playerService.playNext(PlayerService.NEXT);
                binding.Title.setText(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        //上一首监听
        binding.Pre.setOnClickListener(v -> {
            try {
                String name = playerService.playNext(PlayerService.PREVIOUS);
                binding.Title.setText(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        //快进监听
        binding.For.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setPressed(true);
                forward = true;
                updaterHandler.post(new forwardRunnable());
                return false;
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.setPressed(false);
                if (forward) {
                    forward = false;
                }
                playerService.player.start();
                return false;
            }
            return false;
        });

        //快退监听
        binding.Back.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setPressed(true);
                backward = true;
                updaterHandler.post(new forwardRunnable());
                return false;
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.setPressed(false);
                if (backward) {
                    backward = false;
                }
                playerService.player.start();
                return false;
            }
            return false;
        });

        //播放/暂停监听
        binding.Stop.setOnClickListener(v -> {
            if (playerService.player != null) {
                binding.MusicSeekBar.setProgress(playerService.player.getCurrentPosition());
                binding.MusicSeekBar.setMax(playerService.player.getDuration());
            }
            playerService.playOrPause();
            //  由tag的变换来控制事件的调用
        });

    }

    //  通过 Handler 更新 UI 上的组件状态
    private final Handler handler = new Handler();
    private final Runnable refresh = new Runnable() {
        @Override
        public void run() {
            refreshMusicView();
            handler.postDelayed(refresh, 200);
        }
    };

    @SuppressLint("SetTextI18n")
    private void refreshMusicView() {
        binding.Time.setText(formatter.format(playerService.player.getCurrentPosition()));
        binding.Total.setText(formatter.format(playerService.player.getDuration()));
        binding.MusicSeekBar.setProgress(playerService.player.getCurrentPosition());
        binding.MusicSeekBar.setMax(playerService.player.getDuration());
        if (playerService.isPlaying()) {
            binding.Stop.setImageResource(android.R.drawable.ic_media_pause);
            binding.Status.setText("播放中");
        } else {
            binding.Stop.setImageResource(android.R.drawable.ic_media_play);
            binding.Status.setText("已暂停");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(refresh);
        unbindService(serviceConnection);
    }
}

