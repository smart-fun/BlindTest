package fr.arnaudguyon.blindtest.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.inventhys.blecentrallib.PeripheralRemote;
import com.inventhys.blecentrallib.transfer.WriteCharacteristicListener;
import com.inventhys.blecentrallib.transfer.WriteCharacteristicResult;
import com.inventhys.blecommonlib.ByteHelper;

import java.util.UUID;

import fr.arnaudguyon.blindtest.R;
import fr.arnaudguyon.blindtest.bluetooth.BleConst;
import fr.arnaudguyon.blindtest.tools.Bmp;

public class ArduinoPlayer extends Player {

    @DrawableRes
    private final int numbers[] = {R.drawable.n0, R.drawable.n1, R.drawable.n2, R.drawable.n3, R.drawable.n4, R.drawable.n5, R.drawable.n6, R.drawable.n7, R.drawable.n8, R.drawable.n9};

    @NonNull
    private final PeripheralRemote peripheralRemote;

    public ArduinoPlayer(@NonNull Team.TeamColor teamColor, @NonNull PeripheralRemote peripheralRemote) {
        super(teamColor);
        this.peripheralRemote = peripheralRemote;
    }

    @Override
    public void updateDisplay(@NonNull Bitmap bitmap) {
        byte[] message = iconToMessage(bitmap); // put bitmap to 8x8 picture, and converts to 7 bits messages
        sendMessage(message);                   // sends message using BLE
    }

    @Override
    public void printScore(@NonNull Context context, int score) {
        @DrawableRes int resId = numbers[score % 10];
        Bitmap bitmap = Bmp.resIdToBitmap(context, resId);
        if (bitmap != null) {
            byte[] message = iconToMessage(bitmap);
            sendMessage(message);
        }
    }

    private byte[] iconToMessage(@NonNull Bitmap bitmap) {
        byte prefix = (byte) ((getTeam() == Team.TeamColor.RED) ? 'R' : 'Y');
        byte[] data = new byte[12];
        data[0] = prefix;
        int index = 1;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int stepX = width / 8;
        int stepY = height / 8;
        int bit = 0;
        int lineValue = 0;
        for (int y = 0; y < height; y += stepY) {
            for (int x = 0; x < width; x += stepX) {
                int color = bitmap.getPixel(x, y);
                int red = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int blue = (color & 0xFF);
                lineValue <<= 1;
                if (red + green + blue > 128 * 3) {
                    lineValue += 1;
                    //Log.i(BleConst.TAG, "pix " + x + ", " + y);
                }
                ++bit;
                if (bit >= 7) {
                    lineValue |= 0x80;
                    data[index] = (byte) lineValue;
                    lineValue = 0;
                    bit = 0;
                    ++index;
                }
            }
        }
        lineValue <<= (7 - bit);
        data[index] = (byte) (lineValue | 0x80);
        data[index + 1] = 0;
        return data;
    }

    private void sendMessage(@NonNull byte[] message) {
        peripheralRemote.writeCharacteristic(BleConst.NORDIC_UART_SERVICE, BleConst.RX_WRITE, message, new WriteCharacteristicListener() {
            @Override
            public void onCharacteristicWrite(@NonNull WriteCharacteristicResult result, @NonNull UUID uuid, @NonNull UUID uuid1, @Nullable byte[] bytes) {
                if (result == WriteCharacteristicResult.SUCCESS) {
                    Log.i(BleConst.TAG, "Sent " + ByteHelper.byteArrayToHexaString(message));
                } else {
                    Log.i(BleConst.TAG, result.name());
                }
            }
        });
    }


}
