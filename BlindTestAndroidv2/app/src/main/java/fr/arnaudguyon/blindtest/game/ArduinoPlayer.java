package fr.arnaudguyon.blindtest.game;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.inventhys.blecentrallib.PeripheralRemote;
import com.inventhys.blecentrallib.transfer.WriteCharacteristicListener;
import com.inventhys.blecentrallib.transfer.WriteCharacteristicResult;
import com.inventhys.blecommonlib.ByteHelper;

import java.util.UUID;

import fr.arnaudguyon.blindtest.bluetooth.BleConst;

public class ArduinoPlayer extends Player {

    @NonNull
    private final PeripheralRemote peripheralRemote;

    public ArduinoPlayer(@NonNull Team team, @NonNull PeripheralRemote peripheralRemote) {
        super(team);
        this.peripheralRemote = peripheralRemote;
    }

    @Override
    public void setIcon(@NonNull Bitmap bitmap) {
        byte[] message = iconToMessage(bitmap); // put bitmap to 8x8 picture, and converts to 7 bits messages
        sendMessage(message);                   // sends message using BLE
    }

    private byte[] iconToMessage(@NonNull Bitmap bitmap) {
        byte prefix = (byte) ((getTeam() == Team.RED) ? 'R' : 'Y');
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
                    Log.i(BleConst.TAG, "pix " + x + ", " + y);
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
        data[index] = (byte)(lineValue | 0x80);
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
