package fr.arnaudguyon.blindtest.spotify;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;

import fr.arnaudguyon.blindtest.R;
import fr.arnaudguyon.recycler.RecyclerHolder;
import fr.arnaudguyon.recycler.RecyclerItem;

public class PlayListItem extends RecyclerItem {

    @NonNull
    private final SpotPlaylist playlist;

    public PlayListItem(@NonNull SpotPlaylist playlist) {
        super();
        this.playlist = playlist;
    }

    @Override
    public void updateView(RecyclerHolder parentHolder, int position) {
        Holder holder = (Holder) parentHolder;
        holder.name.setText(playlist.getName());

        Glide.with(holder.picture).load(playlist.getImageUrl(640)).into(holder.picture);

    }

    @Override
    public int getViewResId() {
        return R.layout.item_playlist;
    }

    public static class Holder extends RecyclerHolder {

        private final AppCompatImageView picture;
        private final AppCompatTextView name;

        public Holder(View itemView) {
            super(itemView);
            picture = itemView.findViewById(R.id.picture);
            name = itemView.findViewById(R.id.name);
        }

    }
}
