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
import fr.arnaudguyon.blindtest.tools.Led8x8View;

public class GameActivity extends AppCompatActivity implements Game.GameListener {

    private static final String TAG = "GameActivity";

    private Game game = new Game(this);
    private ArrayList<Team> teams;

    private TextView titleView;
    private TextView singerView;
    private View answerLayout;
    private TextView redScore;
    private TextView yellowScore;
    private Led8x8View teamPressIcon;
    private TextView noticeView;
    private View playBar;
    private View playButton;
    private View pauseButton;
    private View resumeButton;
    private Led8x8View redLeds;
    private Led8x8View yellowLeds;

    protected void onActivityReady(@NonNull ArrayList<Team> teams) {

        this.teams = teams;

        setContentView(R.layout.activity_game);

        titleView = findViewById(R.id.title);
        singerView = findViewById(R.id.singer);
        answerLayout = findViewById(R.id.answerLayout);
        redScore = findViewById(R.id.redScore);
        yellowScore = findViewById(R.id.yellowScore);
        teamPressIcon = findViewById(R.id.teamPressIcon);
        noticeView = findViewById(R.id.notice);
        playBar = findViewById(R.id.playBar);
        playButton = playBar.findViewById(R.id.play);
        pauseButton = playBar.findViewById(R.id.pause);
        resumeButton = playBar.findViewById(R.id.resume);
        redLeds = findViewById(R.id.redLeds);
        yellowLeds = findViewById(R.id.yellowLeds);

        answerLayout.setVisibility(View.INVISIBLE);

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

        View startGame = findViewById(R.id.startGame);
        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame.setVisibility(View.GONE);
                noticeView.setText(R.string.notice_next_chosen);
                playBar.setVisibility(View.VISIBLE);
                game.start(GameActivity.this);
                choseNextTrack();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                playTest();
            }
        });

        findViewById(R.id.pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseButton.setVisibility(View.GONE);
                resumeButton.setVisibility(View.VISIBLE);
                MusicPlayer musicPlayer = BlindApplication.getMusicPlayer();
                if (musicPlayer != null) {
                    musicPlayer.pause();
                }
            }
        });

        findViewById(R.id.resume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resumeButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                MusicPlayer musicPlayer = BlindApplication.getMusicPlayer();
                if (musicPlayer != null) {
                    musicPlayer.resume();
                }
            }
        });

        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayer musicPlayer = BlindApplication.getMusicPlayer();
                if (musicPlayer != null) {
                    musicPlayer.pause();
                }
                resumeButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.GONE);
                playButton.setVisibility(View.VISIBLE);
                noticeView.setText(R.string.notice_next_chosen);
                choseNextTrack();
            }
        });

        game.reset(GameActivity.this, teams);
        updateLeds();

    }

    protected void onActivityNotReady() {
        game.stop();
    }

    protected void redPressed() {
        Log.i(TAG, "Red Pressed");
        game.buttonPressed(GameActivity.this, Team.TeamColor.RED);
    }

    protected void yellowPressed() {
        Log.i(TAG, "Yellow Pressed");
        game.buttonPressed(GameActivity.this, Team.TeamColor.YELLOW);
    }

    private void playTest() {
        noticeView.setText(R.string.notice_playing);
        MusicPlayer musicPlayer = BlindApplication.getMusicPlayer();
        if (musicPlayer != null) {
            TrackInfo trackInfo = game.getCurrentTrack();
            if (trackInfo != null) {
                Log.i(TAG, "Play " + trackInfo.getTitle());
                musicPlayer.play(trackInfo);
                //toast("Play " + trackInfo.getTitle());
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
            //toast("Chose " + trackInfo.getTitle());
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
    public void onIconChanged() {
        updateLeds();
    }

    @Override
    public void onWaitResponse(@NonNull Team team) {
        //toast(team.name() + " Pressed button !");

        playBar.setVisibility(View.GONE);
        answerLayout.setVisibility(View.VISIBLE);
        final MusicPlayer musicPlayer = BlindApplication.getMusicPlayer();
        if (musicPlayer != null) {
            musicPlayer.pause();
        }

        teamPressIcon.setVisibility(View.VISIBLE);
        teamPressIcon.setLedColor(getColor(team.getTeamColor().getColorResId()));
        teamPressIcon.setLedResId(team.getIconResId());
        if (team.getTeamColor() == Team.TeamColor.RED) {
            noticeView.setText(R.string.notice_red_pressed);
        } else {
            noticeView.setText(R.string.notice_yellow_pressed);
        }

        answerLayout.findViewById(R.id.correct).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                teamPressIcon.setVisibility(View.GONE);
                if (team.getTeamColor() == Team.TeamColor.RED) {
                    noticeView.setText(R.string.notice_good_red);
                } else {
                    noticeView.setText(R.string.notice_good_yellow);
                }
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.GONE);
                resumeButton.setVisibility(View.VISIBLE);
                playBar.setVisibility(View.VISIBLE);
                answerLayout.setVisibility(View.INVISIBLE);
                game.goodResponse(team);
                printScores();
                game.printScores(GameActivity.this);
            }
        });
        answerLayout.findViewById(R.id.wrong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                teamPressIcon.setVisibility(View.GONE);
                noticeView.setText(R.string.notice_playing);
                answerLayout.setVisibility(View.INVISIBLE);
                if (musicPlayer != null) {
                    musicPlayer.resume();
                }
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                resumeButton.setVisibility(View.GONE);
                playBar.setVisibility(View.VISIBLE);
                game.printScores(GameActivity.this);
                game.onResume();
            }
        });
    }

    private void printScores() {
        for (Team team : teams) {
            int score = team.getScore();
            switch (team.getTeamColor()) {
                case RED:
                    redScore.setText("" + score);
                    break;
                case YELLOW:
                    yellowScore.setText("" + score);
                    break;
            }
        }
    }

    private void updateLeds() {
        for (Team team : teams) {
            int resId = team.getIconResId();
            if (team.getTeamColor() == Team.TeamColor.RED) {
                redLeds.setLedColor(getColor(R.color.red_team));
                redLeds.setLedResId(resId);
            } else {
                yellowLeds.setLedColor(getColor(R.color.yellow_team));
                yellowLeds.setLedResId(resId);
            }
        }
    }
}
