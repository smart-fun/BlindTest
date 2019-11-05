package fr.arnaudguyon.blindtestcompanion.json;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JBase {

    final protected JSONObject root;

    protected JBase(@NonNull JSONObject jsonObject) {
        root = jsonObject;
    }

    protected @Nullable String getString(@NonNull String key) {
        try {
            return root.getString(key);
        } catch (JSONException e) {
        }
        return null;
    }

    protected int getInt(@NonNull String key, int defaultValue) {
        try {
            return root.getInt(key);
        } catch (JSONException e) {
        }
        return defaultValue;
    }

    protected @Nullable JSONArray getArray(@NonNull String key) {
        try {
            return root.getJSONArray(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
