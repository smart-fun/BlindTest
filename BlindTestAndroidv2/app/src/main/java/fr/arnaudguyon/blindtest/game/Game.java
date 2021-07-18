package fr.arnaudguyon.blindtest.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class Game {

    private static final String TAG = "Game";

    private enum State {
        NO_STARTED,
        CHOOSE_ICON
    }

    @NonNull
    private State state = State.NO_STARTED;

    @NonNull
    private final ArrayList<Player> players = new ArrayList<>();

    public void start(@NonNull Context context, @NonNull ArrayList<Player> players) {
        this.players.clear();
        this.players.addAll(players);
        state = State.CHOOSE_ICON;
        for (Player player : this.players) {
            int resId = player.getTeamIcon().getResId();
            Bitmap bitmap = resIdToBitmap(context, resId);
            if (bitmap != null) {
                player.setIcon(bitmap);
            }
        }
    }

    public void stop() {

    }

    public void buttonPressed(@NonNull Team team) {
        Log.i(TAG, "buttonPressed Team " + team.name());
    }

    @Nullable
    private Bitmap resIdToBitmap(@NonNull Context context, @DrawableRes int resId) {
        return BitmapFactory.decodeResource(context.getResources(), resId);
    }

}
