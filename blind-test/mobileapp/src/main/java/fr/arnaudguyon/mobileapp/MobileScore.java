package fr.arnaudguyon.mobileapp;

import android.support.annotation.NonNull;
import android.widget.TextView;

import fr.arnaudguyon.blindtest.Score;

public class MobileScore extends Score {

    private TextView textView;

    public MobileScore(@NonNull TextView textView) {
        super();
        this.textView = textView;
    }

    @Override
    public void setText(@NonNull String text) {
        super.setText(text);
        textView.setText(text);
    }

    @Override public void printScore() {
        super.printScore();
        textView.setText(getText());
    }

}
