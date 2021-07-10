package fr.arnaudguyon.blindtest.spotify;

import androidx.annotation.NonNull;

public class SpotAuth {

    @NonNull
    private static final SpotAuth instance = new SpotAuth();

    @NonNull
    public static SpotAuth getInstance() {
        return instance;
    }

}
