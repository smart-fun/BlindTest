package fr.arnaudguyon.blindtest.game;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import java.util.ArrayList;

import fr.arnaudguyon.blindtest.R;

public class Team {

    public enum TeamColor {
        RED(R.color.red_team),
        YELLOW(R.color.yellow_team);

        @ColorInt
        final int colorResId;

        TeamColor(@ColorInt int color) {
            this.colorResId = color;
        }

        public int getColorResId() {
            return colorResId;
        }

    }

    private int score;
    private final TeamColor teamColor;
    @DrawableRes
    int iconResId;
    @NonNull
    private final ArrayList<Player> players = new ArrayList<>();

    public Team(@NonNull TeamColor teamColor) {
        this.teamColor = teamColor;
    }

    public int getScore() {
        return score;
    }

    public void resetScore() {
        this.score = 0;
    }

    public int incScore() {
        return (score++);
    }

    public TeamColor getTeamColor() {
        return teamColor;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(@NonNull ArrayList<Player> players) {
        this.players.clear();
        this.players.addAll(players);
    }

    public void addPlayer(@NonNull Player player) {
        players.add(player);
    }

}
