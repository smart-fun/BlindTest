package fr.arnaudguyon.blindtestcompanion;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.protocol.error.SpotifyAppRemoteException;
import com.spotify.protocol.mappers.gson.GsonMapper;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class SpotifyHelper {

    private static final String SCHEME = "blindtest";
    private static final String AUTHORITY = "home";

    private String accessToken;
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

    public interface OnConnectListener {
        enum SpotifyConnectionResult {
            SUCCESS,
            SPOTIFY_APP_MISSING,
            OTHER_ERROR
        }
        void onSpotifyConnection(SpotifyConnectionResult result);
    }

}
