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
    private final ArrayList<Team> teams = new ArrayList<>();

    private GameListener listener;
    private ArrayList<TrackInfo> tracks;
    private TrackInfo currentTrack;

    public Game(@NonNull GameListener listener) {
        super();
        this.listener = listener;
    }

    public void reset(@NonNull Context context, @NonNull ArrayList<Team> teams) {
        this.teams.clear();
        this.teams.addAll(teams);
        state = State.CHOOSE_ICON;
        for (Team team : teams) {
            int resId = team.getIconResId();
            Bitmap bitmap = Bmp.resIdToBitmap(context, resId);
            if (bitmap != null) {
                for (Player player : team.getPlayers()) {
                    player.updateDisplay(bitmap);
                }
            }
        }
    }

    public void start(@NonNull Context context) {
        state = State.WAITING;
        for (Team team : teams) {
            team.resetScore();
        }
        printScores(context);
        MusicPlayer musicPlayer = BlindApplication.getMusicPlayer();
        if (musicPlayer != null) {
            if ((tracks == null) || tracks.isEmpty()) {
                tracks = musicPlayer.list();
            }
            //currentTrack = randomNextTrack();
        } else {
            // TODO : error
        }
    }

    public void reloadPlayist() {
        MusicPlayer musicPlayer = BlindApplication.getMusicPlayer();
        if (musicPlayer != null) {
            tracks = musicPlayer.list();
        }
    }

    public TrackInfo getCurrentTrack() {
        return currentTrack;
    }

    public ArrayList<Team> getTeams() {
        return teams;
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

    public int songsLeft() {
        return (tracks != null) ? tracks.size() : 0;
    }

    public void stop() {

    }

    public void onPlayPressed() {
        state = State.PLAYING;
    }

    public void buttonPressed(@NonNull Context context, @NonNull Team.TeamColor teamColor) {
        Log.i(TAG, "buttonPressed Team " + teamColor.name());

        Team team = null;
        for (Team other : teams) {
            if (other.getTeamColor() == teamColor) {
                team = other;
                break;
            }
        }

        if ((team != null) && !gameEnded()) {
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
    }

    public void goodResponse(Team team) {
        team.incScore();
    }

    public boolean gameEnded() {
        for (Team team : teams) {
            if (team.getScore() >= 9) {
                return true;
            }
        }
        return false;
    }

    public void onResume() {
        state = State.PLAYING;
    }

    private void displayTeamPressIcon(@NonNull Context context, @NonNull Team team) {
        Bitmap bitmapNone = Bmp.resIdToBitmap(context, R.drawable.none);
        int resId = team.getIconResId();
        Bitmap bitmap = Bmp.resIdToBitmap(context, resId);
        if ((bitmap != null) && (bitmapNone != null)) {
            for (Team otherTeam : teams) {
                Bitmap selectedBitmap = (otherTeam.getTeamColor() == team.getTeamColor()) ? bitmap : bitmapNone;
                for (Player player : otherTeam.getPlayers()) {
                    player.updateDisplay(selectedBitmap);
                }
            }
        }
    }

    private void selectNextIcon(@NonNull Context context, @NonNull Team team) {
        TeamIcon teamIcon = TeamIcon.next(team.getIconResId());
        int resId = teamIcon.getResId();
        team.setIconResId(resId);
        Bitmap bitmap = Bmp.resIdToBitmap(context, resId);
        if (bitmap != null) {
            for (Player player : team.getPlayers()) {
                player.updateDisplay(bitmap);
            }
        }
        listener.onIconChanged();
    }

    public void printScores(@NonNull Context context) {
        for (Team team : teams) {
            int score = team.getScore();
            for (Player player : team.getPlayers()) {
                player.printScore(context, score);
            }
        }
    }

    public interface GameListener {
        void onIconChanged();

        void onWaitResponse(@NonNull Team team);
    }

}
