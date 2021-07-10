package fr.arnaudguyon.blindtest.spotify;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import org.json.JSONObject;

import fr.arnaudguyon.okrest.OkRequest;

public class SpotPlay {

    @NonNull
    private static final SpotPlay instance = new SpotPlay();

    @Nullable
    private SpotifyAppRemote spotifyAppRemote;

    @Nullable
    SpotUser user;

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

    public void getUser(@NonNull Context context, @NonNull SpotGetUserListener listener) {

        if (user != null) {
            listener.onGetUserFinished(user);
            return;
        }

        String url = "https://api.spotify.com/v1/me";

        OkRequest request = new OkRequest.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + SpotAuth.getInstance().getAccessToken())
                .build();

        int requestCode = 1;
        request.execute(context, requestCode, (success, requestCode1, response) -> {
            user = null;
            if (success) {
                JSONObject jsonObject = response.getBodyJSON();
                if (jsonObject != null) {
                    Log.i(SpotConst.TAG, jsonObject.toString());
                    user = new SpotUser(jsonObject);
                }
            } else {
                String result = "error " + response.getStatusCode();
                Log.i(SpotConst.TAG, result);
            }
            listener.onGetUserFinished(user);
        });
    }

    public interface SpotConnectListener {
        void onSpotConnection(boolean success);
    }

    public interface SpotGetUserListener {
        void onGetUserFinished(SpotUser user);
    }

}
