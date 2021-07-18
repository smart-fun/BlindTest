package fr.arnaudguyon.blindtest.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Bmp {

    @Nullable
    public static Bitmap resIdToBitmap(@NonNull Context context, @DrawableRes int resId) {
        return BitmapFactory.decodeResource(context.getResources(), resId);
    }

}
