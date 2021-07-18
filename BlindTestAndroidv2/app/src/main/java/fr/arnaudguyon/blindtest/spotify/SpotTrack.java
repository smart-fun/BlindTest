package fr.arnaudguyon.blindtest.spotify;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

public class SpotTrack extends JBase {

    protected SpotTrack(@NonNull JSONObject jsonObject) {
        super(jsonObject);
    }

    public String getTitle() {
        return getString("name");
    }

    public String getSinger() {
        String result = "";
        JSONArray artists = getArray("artists");
        if (artists != null) {
            for(int i=0; i< artists.length(); ++i) {
                JSONObject artist = artists.optJSONObject(i);
                if (artist != null) {
                    String artistName = artist.optString("name");
                    if (!TextUtils.isEmpty(artistName)) {
                        if (TextUtils.isEmpty(result)) {
                            result = artistName;
                        } else {
                            result += ", " + artistName;
                        }
                    }
                }
            }
        }
        return result;
    }

    @Nullable
    public String getPictureUrl() {
        JSONObject album = root.optJSONObject("album");
        if (album != null) {
            JSONArray images = album.optJSONArray("images");
            if (images != null) {
                for(int i=0; i< images.length(); ++i) {
                    JSONObject image = images.optJSONObject(i);
                    if (image != null) {
                        int width = image.optInt("width", 0);
                        int height = image.optInt("height", 0);
                        String url = image.optString("url");
                        if ((width > 300) && (height > 300) && !TextUtils.isEmpty(url)) {
                            return url;
                        }
                    }
                }
            }
        }
        return null;
    }

    public String getMusicUrl() {
        return getString("uri");
        //return "spotify:track:" + getString("id");
    }

}
