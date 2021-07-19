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

    public TeamIcon next() {
        return next(resId);
    }

    public static TeamIcon next(int currentResId) {
        for(int i=0; i<TeamIcon.values().length; ++i) {
            TeamIcon teamIcon = TeamIcon.values()[i];
            if (teamIcon.resId == currentResId) {
                ++i;
                if (i < TeamIcon.values().length) {
                    return TeamIcon.values()[i];
                } else {
                    return TeamIcon.values()[0];
                }
            }
        }
        return TeamIcon.values()[0];
    }
}
