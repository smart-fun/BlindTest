package fr.arnaudguyon.blindtest.game;

import androidx.annotation.DrawableRes;

import fr.arnaudguyon.blindtest.R;

public enum TeamIcon {
    CAT(R.drawable.cat),
    GHOST(R.drawable.ghost),
    HEART(R.drawable.heart),
    PACMAN(R.drawable.pacman),
    SUN(R.drawable.sun),
    UNICORN(R.drawable.unicorn);

    @DrawableRes
    private final int resId;
    TeamIcon(int resId) {
        this.resId = resId;
    }

    public int getResId() {
        return resId;
    }
}
