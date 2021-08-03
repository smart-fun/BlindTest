package fr.arnaudguyon.blindtest.game;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import fr.arnaudguyon.blindtest.BlindApplication;
import fr.arnaudguyon.blindtest.BuildConfig;
import fr.arnaudguyon.blindtest.R;
import fr.arnaudguyon.blindtest.tools.Led8x8View;

public class GameActivity extends AppCompatActivity implements Game.GameListener {

    private static final String TAG = "GameActivity";

    private Game game = new Game(this);
    private ArrayList<Team> teams;

    private ImageView picture;
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
    private View startGame;
    private View nextButton;
    private Led8x8View redLeds;
    private Led8x8View yellowLeds;
    private TextView songsLeft;

    protected void onActivityReady(@NonNull ArrayList<Team> teams) {

        this.teams = teams;

        setContentView(R.layout.activity_game);

        //getSupportActionBar().hide();

        picture = findViewById(R.id.picture);
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
        songsLeft = findViewById(R.id.songsLeft);

        answerLayout.setVisibility(View.INVISIBLE);

        if (BuildConfig.DEBUG) {
            redScore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    redPressed();

                }
            });

            yellowScore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    yellowPressed();
                }
            });
        }

        startGame = findViewById(R.id.startGame);
        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playBar.setVisibility(View.VISIBLE);
                nextButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.GONE);
                resumeButton.setVisibility(View.GONE);
                playButton.setVisibility(View.VISIBLE);

                startGame.setVisibility(View.GONE);
                noticeView.setText(R.string.notice_next_chosen);
                game.start(GameActivity.this);
                choseNextTrack();
                printScores();
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

        nextButton = findViewById(R.id.next);
        nextButton.setOnClickListener(new View.OnClickListener() {
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
        int count = game.songsLeft();
        songsLeft.setText(getString(R.string.songs_left, count));
        if (trackInfo != null) {
            //toast("Chose " + trackInfo.getTitle());
            singerView.setText(trackInfo.getSinger());
            titleView.setText(trackInfo.getTitle());
            String url = trackInfo.getPictureUrl();
            if (!TextUtils.isEmpty(url)) {
                picture.setVisibility(View.VISIBLE);
                Glide.with(picture).load(url).into(picture);
            } else {
                picture.setVisibility(View.INVISIBLE);
            }
        } else {
            picture.setVisibility(View.INVISIBLE);
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
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.GONE);
                resumeButton.setVisibility(View.VISIBLE);
                playBar.setVisibility(View.VISIBLE);
                answerLayout.setVisibility(View.INVISIBLE);
                game.goodResponse(team);
                printScores();
                game.printScores(GameActivity.this);
                if (game.gameEnded()) {
                    if (team.getTeamColor() == Team.TeamColor.RED) {
                        noticeView.setText(R.string.notice_red_wins);
                    } else {
                        noticeView.setText(R.string.notice_yellow_wins);
                    }
                    nextButton.setVisibility(View.GONE);
                    startGame.setVisibility(View.VISIBLE);
                } else {
                    if (team.getTeamColor() == Team.TeamColor.RED) {
                        noticeView.setText(R.string.notice_good_red);
                    } else {
                        noticeView.setText(R.string.notice_good_yellow);
                    }
                }
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
