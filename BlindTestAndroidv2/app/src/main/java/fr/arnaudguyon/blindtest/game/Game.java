package fr.arnaudguyon.blindtest.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import fr.arnaudguyon.blindtest.BlindApplication;
import fr.arnaudguyon.blindtest.R;
import fr.arnaudguyon.blindtest.tools.Bmp;

public class Game {

    private static final String TAG = "Game";

    private enum State {
        NO_STARTED,
        CHOOSE_ICON,
        WAITING,
        PLAYING
    }

    @NonNull
    private State state = State.NO_STARTED;

    @NonNull
    private final ArrayList<Player> players = new ArrayList<>();

    private GameListener listener;
    private ArrayList<TrackInfo> tracks;
    private TrackInfo currentTrack;

    public Game(@NonNull GameListener listener) {
        super();
        this.listener = listener;
    }

    public void reset(@NonNull Context context, @NonNull ArrayList<Player> players) {
        this.players.clear();
        this.players.addAll(players);
        state = State.CHOOSE_ICON;
        for (Player player : this.players) {
            int resId = player.getTeamIcon().getResId();
            Bitmap bitmap = Bmp.resIdToBitmap(context, resId);
            if (bitmap != null) {
                player.updateDisplay(bitmap);
            }
        }
    }

    public void start(@NonNull Context context) {
        state = State.WAITING;
        printScores(context);
        MusicPlayer musicPlayer = BlindApplication.getMusicPlayer();
        if (musicPlayer != null) {
            tracks = musicPlayer.list();
            //currentTrack = randomNextTrack();
        } else {
            // TODO : error
        }
    }

    public TrackInfo getCurrentTrack() {
        return currentTrack;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    private TrackInfo randomNextTrack() {
        if ((tracks != null) && !tracks.isEmpty()) {
            int index = (int) (Math.random() * tracks.size());
            currentTrack = tracks.get(index);
            tracks.remove(index);
        }
        return currentTrack;
    }

    public TrackInfo nextTrack() {
        return randomNextTrack();
    }

    public void stop() {

    }

    public void onPlayPressed() {
        state = State.PLAYING;
    }

    public void buttonPressed(@NonNull Context context, @NonNull Team team) {
        Log.i(TAG, "buttonPressed Team " + team.name());
        switch (state) {
            case CHOOSE_ICON:
                selectNextIcon(context, team);
                break;
            case PLAYING:
                state = State.WAITING;
                listener.onWaitResponse(team);
                displayTeamPressIcon(context, team);
                break;
        }
    }

    public void goodResponse(Team team) {
        for (Player player : players) {
            if (player.getTeam() == team) {
                player.setScore(player.getScore() + 1);
            }
        }
    }

    public void onResume() {
        state = State.PLAYING;
    }

    private void displayTeamPressIcon(@NonNull Context context, @NonNull Team team) {
        Bitmap bitmapNone = Bmp.resIdToBitmap(context, R.drawable.none);
        TeamIcon teamIcon = null;
        for (Player player : players) {
            if (player.getTeam() == team) {
                teamIcon = player.getTeamIcon();
                break;
            }
        }
        if (teamIcon != null) {
            int resId = teamIcon.getResId();
            Bitmap bitmap = Bmp.resIdToBitmap(context, resId);
            for (Player player : players) {
                if (player.getTeam() == team) {
                    if (bitmap != null) {
                        player.updateDisplay(bitmap);
                    }
                } else {
                    if (bitmapNone != null) {
                        player.updateDisplay(bitmapNone);
                    }
                }
            }
        }

    }

    private void selectNextIcon(@NonNull Context context, @NonNull Team team) {
        TeamIcon teamIcon = null;
        for (Player player : players) {
            if (player.getTeam() == team) {
                teamIcon = player.getTeamIcon().next();
                break;
            }
        }
        // apply to all players of the team
        if (teamIcon != null) {
            int resId = teamIcon.getResId();
            Bitmap bitmap = Bmp.resIdToBitmap(context, resId);
            for (Player player : players) {
                if (player.getTeam() == team) {
                    player.setTeamIcon(teamIcon);
                    if (bitmap != null) {
                        player.updateDisplay(bitmap);
                    }
                }
            }
        }
        listener.onIconChanged();
    }

    public void printScores(@NonNull Context context) {
        for (Player player : players) {
            player.printScore(context);
        }
    }

    public interface GameListener {
        void onIconChanged();
        void onWaitResponse(@NonNull Team team);
    }

}
