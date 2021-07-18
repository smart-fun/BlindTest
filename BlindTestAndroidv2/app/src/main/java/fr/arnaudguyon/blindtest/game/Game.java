package fr.arnaudguyon.blindtest.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import fr.arnaudguyon.blindtest.R;
import fr.arnaudguyon.blindtest.tools.Bmp;

public class Game {

    private static final String TAG = "Game";

    private enum State {
        NO_STARTED,
        CHOOSE_ICON,
        WAITING
    }

    @NonNull
    private State state = State.NO_STARTED;

    @NonNull
    private final ArrayList<Player> players = new ArrayList<>();

    public void reset(@NonNull Context context, @NonNull ArrayList<Player> players) {
        this.players.clear();
        this.players.addAll(players);
        state = State.CHOOSE_ICON;
        for (Player player : this.players) {
            int resId = player.getTeamIcon().getResId();
            Bitmap bitmap = Bmp.resIdToBitmap(context, resId);
            if (bitmap != null) {
                player.setIcon(bitmap);
            }
        }
    }

    public void start(@NonNull Context context) {
        state = State.WAITING;
        for (Player player : players) {
            player.printScore(context);
        }
    }

    public void stop() {

    }

    public void buttonPressed(@NonNull Context context, @NonNull Team team) {
        Log.i(TAG, "buttonPressed Team " + team.name());
        for (Player player : players) {
            int resId = (player.getTeam() == team) ? player.getTeamIcon().getResId() : R.drawable.none;
            Bitmap bitmap = Bmp.resIdToBitmap(context, resId);
            if (bitmap != null) {
                player.setIcon(bitmap);
            }
        }
    }



}
