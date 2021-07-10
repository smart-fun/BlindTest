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
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONObject;

import java.util.ArrayList;

import fr.arnaudguyon.blindtestcompanion.json.JSpotifyPlaylist;
import fr.arnaudguyon.blindtestcompanion.json.JSpotifyPlaylists;
import fr.arnaudguyon.blindtestcompanion.json.JSpotifyTracks;
import fr.arnaudguyon.blindtestcompanion.json.JSpotifyUser;
import fr.arnaudguyon.okrest.OkRequest;

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
        AuthorizationRequest request = new AuthorizationRequest.Builder(clientId, AuthorizationResponse.Type.TOKEN, redirectUri.toString())
                .setShowDialog(false)
                .setScopes(new String[]{"user-read-email", "playlist-read-private"})
                //.setCampaign("your-campaign-token")
                .build();
        AuthorizationClient.openLoginActivity(activity, requestCode, request);
    }

    public String getAccessToken(int resultCode, @Nullable Intent data) {
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);
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
        request.execute(context, requestCode, (success, requestCode1, response) -> {
            JSpotifyUser user = null;
            if (success) {
                JSONObject jsonObject = response.getBodyJSON();
                if (jsonObject != null) {
                    Log.i(TAG, jsonObject.toString());
                    user = new JSpotifyUser(jsonObject);
                }
            } else {
                String result = "error " + response.getStatusCode();
                Log.i(TAG, result);
            }
            listener.onGetUserFinished(user);
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
        request.execute(context, requestCode, (success, requestCode1, response) -> {
            ArrayList<JSpotifyPlaylist> playlists = new ArrayList<>();
            if (success) {
                JSONObject jsonObject = response.getBodyJSON();
                if (jsonObject != null) {
                    Log.i(TAG, jsonObject.toString());
                    JSpotifyPlaylists pls = new JSpotifyPlaylists(jsonObject);
                    playlists = pls.getPlaylists();
                }
            } else {
                String result = "error " + response.getStatusCode();
                Log.i(TAG, result);
            }
            listener.onGetPlaylistsFinished(playlists);
        });

    }

    public interface getPlaylistsListener {
        void onGetPlaylistsFinished(@NonNull ArrayList<JSpotifyPlaylist> playlists);
    }

    public void getTracks(@NonNull Context context, @NonNull String playlistId, @NonNull final getTracksListener listener) {

        String url = "https://api.spotify.com/v1/playlists/" + playlistId;

        final OkRequest request = new OkRequest.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        int requestCode = 1;
        request.execute(context, requestCode, (success, requestCode1, response) -> {
            JSpotifyTracks tracks = null;
            if (success) {
                JSONObject jsonObject = response.getBodyJSON();
                if (jsonObject != null) {
                    Log.i(TAG, jsonObject.toString());
                    tracks = new JSpotifyTracks(jsonObject);
                }
            } else {
                String result = "error " + response.getStatusCode();
                Log.i(TAG, result);
            }
            listener.onGetTraksFinished(tracks);
        });

    }

    public interface getTracksListener {
        void onGetTraksFinished(@NonNull JSpotifyTracks tracks);
    }


    public void playPlaylist(@NonNull String playlistId) {
        String id = "spotify:playlist:" + playlistId;
        spotifyAppRemote.getPlayerApi().play(id);
        //spotifyAppRemote.getPlayerApi().
    }

    public void pauseMusic() {
        spotifyAppRemote.getPlayerApi().pause();
    }
    public void resumeMusic() {
        spotifyAppRemote.getPlayerApi().resume();
    }
    public void skipMusic() {
        spotifyAppRemote.getPlayerApi().skipNext();
    }

}
