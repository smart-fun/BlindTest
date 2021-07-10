package fr.arnaudguyon.blindtest.spotify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SpotifyActivity extends AppCompatActivity {

    private static final int SPOTIFY_AUTH_REQUEST = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SpotAuth spotAuth = SpotAuth.getInstance();
        if (spotAuth.isAuthenticated()) {
            onAuthenticated();
        } else {
            spotAuth.startAuthenticateActivity(this, SPOTIFY_AUTH_REQUEST);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPOTIFY_AUTH_REQUEST) {
            SpotAuth spotAuth = SpotAuth.getInstance();
            spotAuth.setAuthenticationResult(resultCode, data);
            if (!spotAuth.isAuthenticated()) {
                toast("Spotify Authentication failed");
            }
        }

    }

    private void onAuthenticated() {
        SpotPlay spotPlay = SpotPlay.getInstance();
        spotPlay.connect(this, new SpotPlay.SpotConnectListener() {
            @Override
            public void onSpotConnection(boolean success) {
                if (success) {
                    onSpotifyConnected();
                } else {
                    toast("Spotify Connection failed");
                }
            }
        });
    }

    private void onSpotifyConnected() {
        SpotPlay spotPlay = SpotPlay.getInstance();
        spotPlay.getUser(this, new SpotPlay.SpotGetUserListener() {
            @Override
            public void onGetUserFinished(SpotUser user) {
                if (user != null) {
                    onUserRetrieved(user);
                } else {
                    toast("Cannot retrieve Spotify User");
                }
            }
        });
    }

    private void onUserRetrieved(@NonNull SpotUser user) {
        user.getPlaylists(this, new SpotUser.GetPlaylistsListener() {
            @Override
            public void onGetPlaylistsFinished(@NonNull ArrayList<SpotPlaylist> playlists) {
                if (!playlists.isEmpty()) {
                    displayPlayLists(playlists);
                }
            }
        });
    }

    private void displayPlayLists(@NonNull ArrayList<SpotPlaylist> playlists) {
        for(SpotPlaylist playlist : playlists) {
            String name = playlist.getName();
            String imageUrl = playlist.getImageUrl(640);
            Log.i(SpotConst.TAG, "Playlist: " + name);
        }
    }

    private void toast(@NonNull String string) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }

}
