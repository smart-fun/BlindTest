package fr.arnaudguyon.blindtest.game;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public interface MusicProvider {

    @NonNull
    ArrayList<TrackInfo> list();

    void play(@NonNull TrackInfo trackInfo);
    void pause();

}
