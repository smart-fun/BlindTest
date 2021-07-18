package fr.arnaudguyon.blindtest.game;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import fr.arnaudguyon.blindtest.BlindApplication;
import fr.arnaudguyon.blindtest.R;

public class GameActivity extends AppCompatActivity implements Game.GameListener {

    private static final String TAG = "GameActivity";

    private Game game = new Game();

    private TextView titleView;
    private TextView singerView;

    protected void onActivityReady(@NonNull ArrayList<Player> players) {

        setContentView(R.layout.activity_bluetooth);

        titleView = findViewById(R.id.title);
        singerView = findViewById(R.id.singer);

        findViewById(R.id.redTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redPressed();

            }
        });

        findViewById(R.id.yellowTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yellowPressed();
            }
        });

        findViewById(R.id.startGame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.start(GameActivity.this, GameActivity.this);
                choseNextTrack();
            }
        });

        findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTest();
            }
        });

        findViewById(R.id.pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayer musicPlayer = BlindApplication.getMusicPlayer();
                if (musicPlayer != null) {
                    musicPlayer.pause();
                }
            }
        });

        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choseNextTrack();
            }
        });

        game.reset(GameActivity.this, players);
    }

    protected void onActivityNotReady() {
        game.stop();
    }

    protected void redPressed() {
        Log.i(TAG, "Red Pressed");
        game.buttonPressed(GameActivity.this, Team.RED);
    }

    protected void yellowPressed() {
        Log.i(TAG, "Yellow Pressed");
        game.buttonPressed(GameActivity.this, Team.YELLOW);
    }

    private void playTest() {
        MusicPlayer musicPlayer = BlindApplication.getMusicPlayer();
        if (musicPlayer != null) {
            TrackInfo trackInfo = game.getCurrentTrack();
            if (trackInfo != null) {
                Log.i(TAG, "Play " + trackInfo.getTitle());
                musicPlayer.play(trackInfo);
                toast("Play " + trackInfo.getTitle());
            } else {
                // TODO: end of game ?
                toast("Play error");
            }
        }
        game.onPlayPressed();
    }

    private void choseNextTrack() {
        TrackInfo trackInfo = game.nextTrack();
        if (trackInfo != null) {
            toast("Chose " + trackInfo.getTitle());
            singerView.setText(trackInfo.getSinger());
            titleView.setText(trackInfo.getTitle());
        } else {
            toast("No Track to play");
        }
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWaitResponse(@NonNull Team team) {
        toast(team.name() + " Pressed button !");
        MusicPlayer musicPlayer = BlindApplication.getMusicPlayer();
        if (musicPlayer != null) {
            musicPlayer.pause();
        }
        // TODO: check response and validate or continue
    }

}
