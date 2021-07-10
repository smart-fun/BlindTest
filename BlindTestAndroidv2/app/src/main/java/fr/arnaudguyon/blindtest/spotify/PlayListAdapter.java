package fr.arnaudguyon.blindtest.spotify;

import fr.arnaudguyon.recycler.RecyclerAdapter;
import fr.arnaudguyon.recycler.RecyclerHolder;

public class PlayListAdapter extends RecyclerAdapter {

    @Override
    protected Class<? extends RecyclerHolder> getHolderClassForViewType(int viewType) {
        return PlayListItem.Holder.class;
    }

}
