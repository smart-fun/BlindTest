package fr.arnaudguyon.blindtest.spotify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import fr.arnaudguyon.blindtest.BlindApplication;
import fr.arnaudguyon.blindtest.R;
import fr.arnaudguyon.blindtest.bluetooth.BluetoothActivity;
import fr.arnaudguyon.blindtest.game.MusicPlayer;

public class SpotifyActivity extends AppCompatActivity {

    private static final int SPOTIFY_AUTH_REQUEST = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_spotify);
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
                } else {
                    toast("No Playlist found");
                }
            }
        });
    }

    private void displayPlayLists(@NonNull ArrayList<SpotPlaylist> playlists) {

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        PlayListAdapter adapter = new PlayListAdapter();
        recyclerView.setAdapter(adapter);

        for(SpotPlaylist playlist : playlists) {
            String name = playlist.getName();
            PlayListItem playListItem = new PlayListItem(playlist, new PlayListItem.PlayListListener() {
                @Override
                public void onPlayListChosen(@NonNull PlayListItem playListItem) {
                    createMusicPlayer(playListItem.getPlaylist());
                }
            });
            adapter.addItem(playListItem);
            Log.i(SpotConst.TAG, "Playlist: " + name);
        }
    }

    private void toast(@NonNull String string) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }

    private void createMusicPlayer(@NonNull SpotPlaylist spotPlaylist) {
        SpotifyPlayer player = new SpotifyPlayer(this, spotPlaylist, new MusicPlayer.MusicPlayerListener() {
            @Override
            public void onPlayerReady() {
                startBluetoothActivity();
            }
        });
        BlindApplication.setMusicPlayer(player);
    }

    private void startBluetoothActivity() {
        Intent intent = BluetoothActivity.createIntent(this);
        startActivity(intent);
        finish();
    }

}
