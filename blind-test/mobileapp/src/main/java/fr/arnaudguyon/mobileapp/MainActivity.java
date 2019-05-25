package fr.arnaudguyon.mobileapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import fr.arnaudguyon.blindtest.Score;

public class MainActivity extends AppCompatActivity {

    private Score score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        TextView scoreTextView = findViewById(R.id.scoreTextView);
        score = new MobileScore(scoreTextView);

        score.setScoreRed(0);
        score.setScoreYellow(0);
        score.printScore();

    }

}
