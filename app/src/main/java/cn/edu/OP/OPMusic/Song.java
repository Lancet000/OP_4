package cn.edu.OP.OPMusic;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Song implements Serializable {
    private final String song_name;
    private final String url;
    public Song(String name, String url) {
        this.song_name = name;
        this.url = url;
    }

    @NonNull
    @Override
    public String toString() {
        return song_name;
    }

    public String getSong_name() {
        return song_name;
    }
    public String getUrl() {
        return url;
    }
}
