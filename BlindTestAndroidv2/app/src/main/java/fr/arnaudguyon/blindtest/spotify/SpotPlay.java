package fr.arnaudguyon.blindtest.spotify;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

public class SpotPlay {

    @NonNull
    private static final SpotPlay instance = new SpotPlay();

    @Nullable
    private SpotifyAppRemote spotifyAppRemote;

    @NonNull
    public static SpotPlay getInstance() {
        return instance;
    }

    public void connect(@NonNull Context context, @NonNull SpotConnectListener listener) {
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(SpotConst.CLIENT_ID)
                        .setRedirectUri(SpotConst.REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(context, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        SpotPlay.this.spotifyAppRemote = spotifyAppRemote;
                        listener.onSpotConnection(true);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        throwable.printStackTrace();
                        listener.onSpotConnection(false);
                    }
                });
    }

    public interface SpotConnectListener {
        void onSpotConnection(boolean success);
    }

}
