package fr.arnaudguyon.blindtest.spotify;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class SpotAuth {

    @NonNull
    private static final SpotAuth instance = new SpotAuth();

    @Nullable
    private String accessToken;

    @NonNull
    public static SpotAuth getInstance() {
        return instance;
    }

    public void startAuthenticateActivity(@NonNull Activity activity, int requestCode) {
        final AuthorizationRequest request =
                new AuthorizationRequest.Builder(SpotConst.CLIENT_ID, AuthorizationResponse.Type.TOKEN, SpotConst.REDIRECT_URI)
                .setShowDialog(false)
                .setScopes(new String[]{"user-read-email", "playlist-read-private"})
                //.setCampaign("your-campaign-token")
                .build();
        AuthorizationClient.openLoginActivity(activity, requestCode, request);
    }

    public void setAuthenticationResult(int resultCode, @Nullable Intent data) {
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);
        accessToken = response.getAccessToken();
    }

    public boolean isAuthenticated() {
        return !TextUtils.isEmpty(accessToken);
    }

    @NonNull
    public String getAccessToken() {
        return accessToken;
    }

}
