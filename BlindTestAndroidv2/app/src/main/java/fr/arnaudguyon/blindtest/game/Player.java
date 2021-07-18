package fr.arnaudguyon.blindtest.game;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

public abstract class Player {

    @NonNull
    final private Team team;

    @NonNull TeamIcon teamIcon = TeamIcon.CAT;

    private int score = 0;

    public Player(@NonNull Team team) {
        this.team = team;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public abstract void setIcon(@NonNull Bitmap bitmap);

    @NonNull
    public Team getTeam() {
        return team;
    }

    public int getScore() {
        return score;
    }

    @NonNull
    public TeamIcon getTeamIcon() {
        return teamIcon;
    }

    public void setTeamIcon(@NonNull TeamIcon teamIcon) {
        this.teamIcon = teamIcon;
    }
}
