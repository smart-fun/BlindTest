package fr.arnaudguyon.blindtest.spotify;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SpotPlaylists extends JBase {

    public SpotPlaylists(@NonNull JSONObject jsonObject) {
        super(jsonObject);
    }

    public ArrayList<SpotPlaylist> getPlaylists() {
        ArrayList<SpotPlaylist> playlists = new ArrayList<>();
        JSONArray array = getArray("items");
        if (array != null) {
            for (int i = 0; i < array.length(); ++i) {
                try {
                    JSONObject object = array.getJSONObject(i);
                    SpotPlaylist playlist = new SpotPlaylist(object);
                    playlists.add(playlist);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return playlists;
    }


}
