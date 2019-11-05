package fr.arnaudguyon.blindtestcompanion.json;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JSpotifyPlaylists extends JBase {

    public JSpotifyPlaylists(@NonNull JSONObject jsonObject) {
        super(jsonObject);
    }

    public ArrayList<JSpotifyPlaylist> getPlaylists() {
        ArrayList<JSpotifyPlaylist> playlists = new ArrayList<>();
        JSONArray array = getArray("items");
        if (array != null) {
            for (int i = 0; i < array.length(); ++i) {
                try {
                    JSONObject object = array.getJSONObject(i);
                    JSpotifyPlaylist playlist = new JSpotifyPlaylist(object);
                    playlists.add(playlist);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return playlists;
    }


}
