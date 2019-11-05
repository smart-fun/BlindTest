package fr.arnaudguyon.blindtestcompanion.json;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JSpotifyPlaylist extends JBase {

    public JSpotifyPlaylist(@NonNull JSONObject jsonObject) {
        super(jsonObject);
    }

    public String getId() {
        return getString("id");
    }

    public String getName() {
        return getString("name");
    }

    public String getImageUrl(int idealWidth) {
        ArrayList<JSpotifyImage> images = new ArrayList<>();
        try {
            JSONArray array = root.getJSONArray("images");
            for (int i=0; i<array.length(); ++i) {
                JSONObject object = array.getJSONObject(i);
                if (object != null) {
                    JSpotifyImage image = new JSpotifyImage(object);
                    images.add(image);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSpotifyImage bestImage = null;
        int bestDiff = 0;
        for(JSpotifyImage image : images) {
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

}
