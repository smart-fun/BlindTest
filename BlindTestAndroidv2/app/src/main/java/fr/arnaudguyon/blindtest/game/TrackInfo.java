package fr.arnaudguyon.blindtest.game;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TrackInfo {

    @NonNull
    private final String id;
    @NonNull
    private final String title;
    @NonNull
    private final String singer;
    @Nullable
    private final String pictureUrl;

    public TrackInfo(@NonNull String id, @NonNull String title, @NonNull String singer, @Nullable String pictureUrl) {
        this.id = id;
        this.title = title;
        this.singer =singer;
        this.pictureUrl = pictureUrl;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getSinger() {
        return singer;
    }

    @Nullable
    public String getPictureUrl() {
        return pictureUrl;
    }
}
