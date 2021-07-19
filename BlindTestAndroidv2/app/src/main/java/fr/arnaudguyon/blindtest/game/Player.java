package fr.arnaudguyon.blindtest.game;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

public abstract class Player {

    @NonNull
    final private Team.TeamColor team;

    @NonNull TeamIcon teamIcon = TeamIcon.CAT;

    public Player(@NonNull Team.TeamColor team) {
        this.team = team;
    }

    public abstract void updateDisplay(@NonNull Bitmap bitmap);

    public abstract void printScore(@NonNull Context context, int score);

    @NonNull
    public final Team.TeamColor getTeam() {
        return team;
    }

    @NonNull
    public final TeamIcon getTeamIcon() {
        return teamIcon;
    }

    public final void setTeamIcon(@NonNull TeamIcon teamIcon) {
        this.teamIcon = teamIcon;
    }
}
