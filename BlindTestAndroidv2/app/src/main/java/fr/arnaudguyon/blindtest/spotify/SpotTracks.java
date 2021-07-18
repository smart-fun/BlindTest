package fr.arnaudguyon.blindtest.spotify;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SpotTracks extends JBase {

    public SpotTracks(@NonNull JSONObject jsonObject) {
        super(jsonObject);
    }

    @NonNull
    public ArrayList<SpotTrack> getTracks() {
        ArrayList<SpotTrack> result = new ArrayList<>();
        JSONObject tracks = root.optJSONObject("tracks");
        if (tracks != null) {
            JSONArray items = tracks.optJSONArray("items");
            if (items != null) {
                for(int i=0; i<items.length(); ++i) {
                    JSONObject item = items.optJSONObject(i);
                    if (item != null) {
                        JSONObject track = item.optJSONObject("track");
                        if (track != null) {
                            result.add(new SpotTrack(track));
                        }
                    }
                }
            }
        }
        return result;
    }

}
