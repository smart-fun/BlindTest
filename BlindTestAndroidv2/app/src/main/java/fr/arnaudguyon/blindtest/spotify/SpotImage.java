package fr.arnaudguyon.blindtest.spotify;

import androidx.annotation.NonNull;

import org.json.JSONObject;

public class SpotImage extends JBase {

    SpotImage(@NonNull JSONObject jsonObject) {
        super(jsonObject);
    }

    public int getWidth() {
        return getInt("width", 0);
    }

    public String getUrl() {
        return getString("url");
    }

}
