package fr.arnaudguyon.blindtestcompanion.json;

import androidx.annotation.NonNull;

import org.json.JSONObject;

public class JSpotifyUser extends JBase {

    public JSpotifyUser(@NonNull JSONObject jsonObject) {
        super(jsonObject);
    }

    public String getId() {
        return getString("id");
    }

}
