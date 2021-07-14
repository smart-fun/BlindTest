package fr.arnaudguyon.blindtest.spotify;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fr.arnaudguyon.okrest.OkRequest;

public class SpotPlaylist extends JBase {

    public SpotPlaylist(@NonNull JSONObject jsonObject) {
        super(jsonObject);
    }

    public String getId() {
        return getString("id");
    }

    public String getName() {
        return getString("name");
    }

    public String getImageUrl(int idealWidth) {
        ArrayList<SpotImage> images = new ArrayList<>();
        try {
            JSONArray array = root.getJSONArray("images");
            for (int i=0; i<array.length(); ++i) {
                JSONObject object = array.getJSONObject(i);
                if (object != null) {
                    SpotImage image = new SpotImage(object);
                    images.add(image);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SpotImage bestImage = null;
        int bestDiff = 0;
        for(SpotImage image : images) {
            int diff = Math.abs(image.getWidth() - idealWidth);
            if ((bestImage == null) || (diff < bestDiff)) {
                bestImage = image;
                bestDiff = diff;
            }
        }
        if (bestImage != null) {
            return bestImage.getUrl();
        }
        return null;
    }

    public void requestTracks(@NonNull Context context, @NonNull RequestTracksListener listener) {

        String playlistId = getId();
        String url = "https://api.spotify.com/v1/playlists/" + playlistId;
        String accessToken = SpotAuth.getInstance().getAccessToken();

        final OkRequest request = new OkRequest.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        int requestCode = 1;
        request.execute(context, requestCode, (success, requestCode1, response) -> {
            SpotTracks tracks = null;
            if (success) {
                JSONObject jsonObject = response.getBodyJSON();
                if (jsonObject != null) {
                    Log.i(SpotConst.TAG, jsonObject.toString());
                    tracks = new SpotTracks(jsonObject);
                }
            } else {
                String result = "error " + response.getStatusCode();
                Log.i(SpotConst.TAG, result);
            }
            listener.onRequestTracksEnded(tracks);
        });

    }

    public interface RequestTracksListener {
        void onRequestTracksEnded(@Nullable SpotTracks spotTracks);
    }

}