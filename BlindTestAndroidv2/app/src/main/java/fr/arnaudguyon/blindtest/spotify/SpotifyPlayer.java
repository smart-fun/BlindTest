package fr.arnaudguyon.blindtest.spotify;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import fr.arnaudguyon.blindtest.game.MusicPlayer;
import fr.arnaudguyon.blindtest.game.TrackInfo;

public class SpotifyPlayer extends MusicPlayer {

    private SpotPlaylist playlist;
    private SpotTracks tracks;

    public SpotifyPlayer(@NonNull Context context, @NonNull SpotPlaylist playlist, @NonNull MusicPlayerListener listener) {
        this.playlist = playlist;
        playlist.requestTracks(context, new SpotPlaylist.RequestTracksListener() {
            @Override
            public void onRequestTracksEnded(@Nullable SpotTracks spotTracks) {
                SpotifyPlayer.this.tracks = spotTracks;
                listener.onPlayerReady();
            }
        });
    }

    @NonNull
    @Override
    public ArrayList<TrackInfo> list() {
        ArrayList<TrackInfo> result = new ArrayList<>();
        ArrayList<SpotTrack> trackList = tracks.getTracks();
        for(SpotTrack spotTrack : trackList) {
            String id = spotTrack.getMusicUrl();
            String title = spotTrack.getTitle();
            String singer = spotTrack.getSinger();
            String pictureUlr = spotTrack.getPictureUrl();
            TrackInfo trackInfo = new TrackInfo(id, title, singer, pictureUlr);
            result.add(trackInfo);
        }
        return result;
    }

    @Override
    public void play(@NonNull TrackInfo trackInfo) {
        String trackId = trackInfo.getId();
        SpotPlay.getInstance().playTrack(trackId);
    }

    @Override
    public void pause() {
        SpotPlay.getInstance().pause();
    }

}
