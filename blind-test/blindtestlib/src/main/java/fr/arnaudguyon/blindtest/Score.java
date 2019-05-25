package fr.arnaudguyon.blindtest;

import android.support.annotation.NonNull;

import java.util.Locale;

public abstract class Score {

    private int scoreRed;
    private int scoreYellow;
    private String text;

    public void setText(@NonNull String text) {
        this.text = text;
    }

    public void printScore() {
        String leftScore = (scoreRed < 10) ? scoreRed + " " : "" + scoreRed;
        text = String.format(Locale.US, "%s %2d", leftScore, scoreYellow);
    }

    public String getText() {
        return text;
    }

    public int getScoreRed() {
        return scoreRed;
    }

    public void setScoreRed(int scoreRed) {
        this.scoreRed = scoreRed;
    }

    public int getScoreYellow() {
        return scoreYellow;
    }

    public void setScoreYellow(int scoreYellow) {
        this.scoreYellow = scoreYellow;
    }
}
