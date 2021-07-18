package fr.arnaudguyon.blindtest.game;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public abstract class MusicPlayer {

    @NonNull
    public abstract ArrayList<TrackInfo> list();

    public abstract void play(@NonNull TrackInfo trackInfo);
    public abstract void pause();
    public abstract void resume();

    public interface MusicPlayerListener {
        void onPlayerReady();
    }
}
