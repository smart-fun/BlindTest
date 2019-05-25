package fr.arnaudguyon.blindtest;

import android.support.annotation.NonNull;

public class ThingScore extends Score {

    private HT16K33Device device;

    public ThingScore(@NonNull HT16K33Device device) {
        super();
        this.device = device;
    }

    @Override
    public void setText(@NonNull String text) {
        super.setText(text);
        device.print(text);
    }

    @Override
    public void printScore() {
        super.printScore();
        device.print(getText());
    }

}
