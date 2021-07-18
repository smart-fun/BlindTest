package fr.arnaudguyon.blindtest;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import fr.arnaudguyon.blindtest.game.MusicPlayer;

public class BlindApplication extends Application {

    private static BlindApplication instance;

    private MusicPlayer musicPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public void onTerminate() {
        instance = null;
        super.onTerminate();
    }

    public static void setMusicPlayer(@NonNull MusicPlayer musicPlayer) {
        if (instance != null) {
            instance.musicPlayer = musicPlayer;
        }
    }
    public static MusicPlayer getMusicPlayer() {
        if (instance != null) {
            return instance.musicPlayer;
        }
        return null;
    }

}
