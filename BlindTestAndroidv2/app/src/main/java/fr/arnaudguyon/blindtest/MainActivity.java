package fr.arnaudguyon.blindtest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import fr.arnaudguyon.blindtest.spotify.SpotUser;
import fr.arnaudguyon.blindtest.spotify.SpotAuth;
import fr.arnaudguyon.blindtest.spotify.SpotConst;
import fr.arnaudguyon.blindtest.spotify.SpotPlay;

public class MainActivity extends AppCompatActivity {

    private static final int SPOTIFY_AUTH_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        findViewById(R.id.authenticate_spotify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpotAuth.getInstance().startAuthenticateActivity(MainActivity.this, SPOTIFY_AUTH_REQUEST);
            }
        });

        findViewById(R.id.connect_spotify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectSpotify();
            }
        });

        findViewById(R.id.get_user_spotify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUser();
            }
        });

        findViewById(R.id.get_playlists_spotify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPlaylists();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPOTIFY_AUTH_REQUEST) {
            SpotAuth spotAuth = SpotAuth.getInstance();
            spotAuth.setAuthenticationResult(resultCode, data);
            if (spotAuth.isAuthenticated()) {
                Log.i(SpotConst.TAG, "SPOTIFY_AUTH_REQUEST success");
                // continue
            } else {
                Log.i(SpotConst.TAG, "SPOTIFY_AUTH_REQUEST failuer");
            }
        }

    }

    private void connectSpotify() {
        SpotPlay spotPlay = SpotPlay.getInstance();
        spotPlay.connect(this, new SpotPlay.SpotConnectListener() {
            @Override
            public void onSpotConnection(boolean success) {
                Log.i(SpotConst.TAG, "connectSpotify: " + success);
            }
        });
    }

    private void getUser() {
        SpotPlay.getInstance().getSpotifyUser(this);
    }

    private void getPlaylists() {
        SpotUser user = SpotPlay.getInstance().getUser();
        if (user != null) {
            user.getPlaylists(this);
        }
    }
}