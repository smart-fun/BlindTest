package fr.arnaudguyon.blindtest.game;

import android.content.Context;
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

    public final void setScore(int score) {
        this.score = score;
    }

    public abstract void updateDisplay(@NonNull Bitmap bitmap);

    public abstract void printScore(@NonNull Context context);

    @NonNull
    public final Team getTeam() {
        return team;
    }

    public final int getScore() {
        return score;
    }

    @NonNull
    public final TeamIcon getTeamIcon() {
        return teamIcon;
    }

    public final void setTeamIcon(@NonNull TeamIcon teamIcon) {
        this.teamIcon = teamIcon;
    }
}
