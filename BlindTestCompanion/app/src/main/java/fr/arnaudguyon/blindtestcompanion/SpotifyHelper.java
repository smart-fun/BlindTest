package fr.arnaudguyon.blindtestcompanion;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.protocol.mappers.gson.GsonMapper;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fr.arnaudguyon.blindtestcompanion.json.JSpotifyPlaylist;
import fr.arnaudguyon.blindtestcompanion.json.JSpotifyPlaylists;
import fr.arnaudguyon.blindtestcompanion.json.JSpotifyUser;
import fr.arnaudguyon.okrest.OkRequest;
import fr.arnaudguyon.okrest.OkResponse;
import fr.arnaudguyon.okrest.RequestListener;

// Spotify Authentication API is not necessary for simple music play / control

public class SpotifyHelper {

    private static final String TAG = "SpotifyHelper";

    private static final String SCHEME = "blindtest";
    private static final String AUTHORITY = "home";

    private String accessToken;
    private String userId;
    private SpotifyAppRemote spotifyAppRemote;

    public void authenticate(@NonNull Activity activity, int requestCode) {
        String clientId = activity.getString(R.string.spotify_client_id);
        Uri redirectUri = new Uri.Builder().scheme(SCHEME).authority(AUTHORITY).build();
        AuthenticationRequest request = new AuthenticationRequest.Builder(clientId, AuthenticationResponse.Type.TOKEN, redirectUri.toString())
                .setShowDialog(false)
                .setScopes(new String[]{"user-read-email", "playlist-read-private"})
                //.setCampaign("your-campaign-token")
                .build();
        AuthenticationClient.openLoginActivity(activity, requestCode, request);
    }

    public String getAccessToken(int resultCode, @Nullable Intent data) {
        final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
        accessToken = response.getAccessToken();
        return accessToken;
    }

    public void connect(@NonNull Context context, final @NonNull OnConnectListener listener) {

        SpotifyAppRemote.disconnect(spotifyAppRemote);

        String clientId = context.getString(R.string.spotify_client_id);
        Uri redirectUri = new Uri.Builder().scheme(SCHEME).authority(AUTHORITY).build();

        // TODO: Remote API uses also same Auth params ? Auth API useful ??
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(clientId)
                        .setRedirectUri(redirectUri.toString())
                        .showAuthView(true)
                        .setJsonMapper(GsonMapper.create())
                        .build();

        SpotifyAppRemote.connect(context, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        SpotifyHelper.this.spotifyAppRemote = spotifyAppRemote;
                        listener.onSpotifyConnection(OnConnectListener.SpotifyConnectionResult.SUCCESS);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        if (throwable instanceof CouldNotFindSpotifyApp) {
                            listener.onSpotifyConnection(OnConnectListener.SpotifyConnectionResult.SPOTIFY_APP_MISSING);
                        } else {
                            listener.onSpotifyConnection(OnConnectListener.SpotifyConnectionResult.OTHER_ERROR);
                        }

                    }
                });
    }

    public void disconnect() {
        if (spotifyAppRemote != null) {
            SpotifyAppRemote.disconnect(spotifyAppRemote);
        }
    }

    private void play(@NonNull String playlistId) {
        // Play a playlist
        //mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");

        // Subscribe to PlayerState
//        mSpotifyAppRemote.getPlayerApi()
//                .subscribeToPlayerState()
//                .setEventCallback(playerState -> {
//                    final Track track = playerState.track;
//                    if (track != null) {
//                        Log.d("MainActivity", track.name + " by " + track.artist.name);
//                    }
//                });


    }

    public interface OnConnectListener {
        enum SpotifyConnectionResult {
            SUCCESS,
            SPOTIFY_APP_MISSING,
            OTHER_ERROR
        }
        void onSpotifyConnection(SpotifyConnectionResult result);
    }

    public String getUserId() {
        return userId;
    }

    public void getUser(final @NonNull Context context, final @NonNull getUserListener listener) {

        String url = "https://api.spotify.com/v1/me";

        OkRequest request = new OkRequest.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        int requestCode = 1;
        request.execute(context, requestCode, new RequestListener() {
            @Override
            public void onRequestResponse(boolean success, int requestCode, OkResponse response) {
                JSpotifyUser user = null;
                if (success) {
                    String result = response.getBodyJSON().toString();
                    Log.i(TAG, result);
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        user = new JSpotifyUser(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    String result = "error " + response.getStatusCode();
                    Log.i(TAG, result);
                }
                listener.onGetUserFinished(user);
            }

        });
    }

    public interface getUserListener {
        void onGetUserFinished(@Nullable JSpotifyUser user);
    }

    public void getPlaylists(@NonNull Context context, @NonNull String userId, @NonNull final getPlaylistsListener listener) {

        String url = "https://api.spotify.com/v1/users/" + userId + "/playlists";

        final OkRequest request = new OkRequest.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        int requestCode = 1;
        request.execute(context, requestCode, new RequestListener() {
            @Override
            public void onRequestResponse(boolean success, int requestCode, OkResponse response) {
                ArrayList<JSpotifyPlaylist> playlists = new ArrayList<>();
                if (success) {
                    String result = response.getBodyJSON().toString();
                    Log.i(TAG, result);
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSpotifyPlaylists pls = new JSpotifyPlaylists(jsonObject);
                        playlists = pls.getPlaylists();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    String result = "error " + response.getStatusCode();
                    Log.i(TAG, result);
                }
                listener.onGetPlaylistsFinished(playlists);
            }

        });

    }

    public interface getPlaylistsListener {
        void onGetPlaylistsFinished(@NonNull ArrayList<JSpotifyPlaylist> playlists);
    }

    public void playPlaylist(@NonNull String playlistId) {
        String id = "spotify:playlist:" + playlistId;
        spotifyAppRemote.getPlayerApi().play(id);
    }

}
