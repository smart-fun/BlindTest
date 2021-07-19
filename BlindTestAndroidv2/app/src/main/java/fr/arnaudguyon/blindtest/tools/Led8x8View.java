package fr.arnaudguyon.blindtest.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import fr.arnaudguyon.blindtest.R;

public class Led8x8View extends AppCompatImageView {

    @DrawableRes
    private int ledResId;

    private boolean[] data;
    private final Paint paint = new Paint();

    public Led8x8View(@NonNull Context context) {
        super(context);
        init();
    }

    public Led8x8View(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Led8x8View(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (isInEditMode()) {
            setLedColor(getResources().getColor(R.color.red_team));
            setLedResId(R.drawable.cat);
        }
    }

    public void setLedColor(int ledColor) {
        if (ledColor != paint.getColor()) {
            this.paint.setColor(ledColor);
            data = null;
            invalidate();
        }
    }

    public void setLedResId(@DrawableRes int ledResId) {
        if (ledResId != this.ledResId) {
            this.ledResId = ledResId;
            data = null;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if ((paint.getColor() != 0) && (ledResId != 0)) {
            if (data == null) {
                Bitmap bitmap = Bmp.resIdToBitmap(getContext(), ledResId);
                if (bitmap != null) {
                    data = new boolean[64];
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    int stepX = width / 8;
                    int stepY = height / 8;
                    int index = 0;
                    for (int y = 0; y < height; y += stepY) {
                        for (int x = 0; x < width; x += stepX) {
                            int color = bitmap.getPixel(x, y);
                            int red = (color >> 16) & 0xFF;
                            int green = (color >> 8) & 0xFF;
                            int blue = (color & 0xFF);
                            boolean value = (red + green + blue > 128 * 3) ? true : false;
                            data[index] = value;
                            ++index;
                        }
                    }

                }
            }
            if (data != null) {
                int width = canvas.getWidth();
                int height = canvas.getHeight();
                float stepX = width / 8.f;
                float stepY = height / 8.f;
                for (int y = 0; y < 8; ++y) {
                    for (int x = 0; x < 8; ++x) {
                        if (data[(y * 8) + x]) {
                            float left = (width * x) / 8.f;
                            float top = (height * y) / 8.f;
                            float right = left + stepX;
                            float bot = top + stepY;
                            canvas.drawRect(left, top, right, bot, paint);
                        }
                    }
                }
            }
        }
    }
}
