package fr.arnaudguyon.blindtest.spotify;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.ArrayList;

import fr.arnaudguyon.okrest.OkRequest;

public class SpotUser extends JBase {

    public SpotUser(@NonNull JSONObject jsonObject) {
        super(jsonObject);
    }

    public String getId() {
        return getString("id");
    }

    public void getPlaylists(@NonNull Context context, @NonNull GetPlaylistsListener listener) {

        String userId = getId();
        String accessToken = SpotAuth.getInstance().getAccessToken();

        String url = "https://api.spotify.com/v1/users/" + userId + "/playlists";

        final OkRequest request = new OkRequest.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + accessToken)
                .addParam("limit", "50")
                .build();

        int requestCode = 1;
        request.execute(context, requestCode, (success, requestCode1, response) -> {
            ArrayList<SpotPlaylist> playlists = new ArrayList<>();
            if (success) {
                JSONObject jsonObject = response.getBodyJSON();
                if (jsonObject != null) {
                    Log.i(SpotConst.TAG, jsonObject.toString());
                    SpotPlaylists pls = new SpotPlaylists(jsonObject);
                    playlists = pls.getPlaylists();
                }
            } else {
                String result = "error " + response.getStatusCode();
                Log.i(SpotConst.TAG, result);
            }
            listener.onGetPlaylistsFinished(playlists);
        });

    }

    public interface GetPlaylistsListener {
        void onGetPlaylistsFinished(@NonNull ArrayList<SpotPlaylist> playlists);
    }
}
