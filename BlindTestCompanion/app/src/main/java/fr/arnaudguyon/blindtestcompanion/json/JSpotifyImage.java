package fr.arnaudguyon.blindtestcompanion.json;

import androidx.annotation.NonNull;

import org.json.JSONObject;

public class JSpotifyImage extends JBase {

    JSpotifyImage(@NonNull JSONObject jsonObject) {
        super(jsonObject);
    }

    public int getWidth() {
        return getInt("width", 0);
    }

    public String getUrl() {
        return getString("url");
    }

}
