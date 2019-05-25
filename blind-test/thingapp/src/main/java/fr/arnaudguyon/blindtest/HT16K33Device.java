package fr.arnaudguyon.blindtest;

import android.support.annotation.NonNull;

import com.google.android.things.pio.I2cDevice;

import java.io.IOException;
import java.util.HashMap;

// see https://cdn-shop.adafruit.com/datasheets/ht16K33v110.pdf
// see https://github.com/lpaseen/ht16k33/blob/master/src/ht16k33.cpp
// see https://github.com/adafruit/Adafruit_LED_Backpack/blob/master/Adafruit_LEDBackpack.cpp

public class HT16K33Device {

    // REGISTERS
    private static final int DISPLAY_DATA_REGISTER  = 0x00;
    private static final int SYSTEM_SETUP_REGISTER  = 0x20;
    private static final int DISPLAY_SETUP_REGISTER = 0x80;
    private static final int ROW_INT_SET_REGISTER   = 0xA0;
    private static final int BRIGTHNESS_REGISTER    = 0xE0;

//    private static final int COMMAND_DISPLAY_DATA   = 0b00000000;   // 0x00
//    private static final int COMMAND_SYSTEM_SETUP   = 0b00100000;   // 0x20
//    private static final int COMMAND_KEY_DATA       = 0b01000000;
//    private static final int COMMAND_INT_FLAG       = 0b01100000;
//    private static final int COMMAND_DISPLAY_SETUP  = 0b10000000;   // 0x80
//    private static final int COMMAND_ROW_INT_SET    = 0b10100000;   // 0xA0
//

    private static final int SYSTEM_SETUP_OFF = 0;
    private static final int SYSTEM_SETUP_ON = 1;

    private static final int DISPLAY_SETUP_OFF = 0;
    private static final int DISPLAY_SETUP_ON = 1;
    private static final int DISPLAY_SETUP_BLINK_NONE = 0;
    private static final int DISPLAY_SETUP_BLINK_500MS = 2;
    private static final int DISPLAY_SETUP_BLINK_1000MS = 4;
    private static final int DISPLAY_SETUP_BLINK_2000MS = 6;

    // bits: top, topR, botR, bot, botL, topL, mid, dot
    // highest bit 0x80 = dot after character
    private HashMap<String, Integer> characters = new HashMap<String, Integer>() {{
        put("0", 0x3F);
        put("1", 0x06);
        put("2", 0x5B);
        put("3", 0x4F);
        put("4", 0x66);
        put("5", 0x6D);
        put("6", 0x7D);
        put("7", 0x07);
        put("8", 0x7F);
        put("9", 0x6F);
        put("A", 0x77);
        put("B", 0x7F);
        put("C", 0x39);
        put("d", 0x5E);
        put("E", 0x79);
        put("F", 0x71);
        put("G", 0x7D);
        put("H", 0x76);
        put("I", 0x06);
        put("J", 0x0E);
        put("L", 0x38);
        put("O", 0x3F);
        put("o", 0x5C);
        put("P", 0x73);
        put("S", 0x6D);
        put("U", 0x3E);
        put("Y", 0x66);
        put("Z", 0x5B);
        put(" ", 0x00);
        put(":", 0x01); // used for 3rd character only
        put("-", 0x40);
        put("_", 0x08);
    }};
    // GOOd
    // BAD
    // COOL
    // DEAD

    private I2cDevice device;

    public HT16K33Device(@NonNull I2cDevice device) {
        this.device = device;
    }

    public void init() {
        writeCommand(SYSTEM_SETUP_REGISTER, SYSTEM_SETUP_ON);
        writeCommand(DISPLAY_SETUP_REGISTER, DISPLAY_SETUP_ON | DISPLAY_SETUP_BLINK_NONE);
        writeCommand(BRIGTHNESS_REGISTER, 7);


        // bytes: array of index, value. indexes: 0,1,3,4 for numbers, 2 for ":"

//        byte[] test = {
//                0, 0x39,
//                1, 0x3F,
//                2, 0x0,
//                3, 0x3F,
//                4, 0x38
//        };
//        writeDisplayData(test);

        print("PA:PA");

    }

    public void deinit() {
        writeCommand(DISPLAY_SETUP_REGISTER, DISPLAY_SETUP_OFF | DISPLAY_SETUP_BLINK_NONE);
        writeCommand(SYSTEM_SETUP_REGISTER, SYSTEM_SETUP_OFF);
    }

    private boolean writeCommand(int register, int value) {
        byte data = (byte)(register | value);
        byte[] bytes = new byte[1];
        bytes[0] = data;
        try {
            device.write(bytes, 1);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean print(@NonNull String text) {
        int len = text.length();
        if (len > 0) {
            byte[] bytes = new byte[len*2];
            int index = 0;
            for(int i=0; i<len; ++i) {
                bytes[index] = (byte)i;
                Integer value = characters.get(text.substring(i, i+1));
                if (value != null) {
                    bytes[index + 1] = value.byteValue();
                } else {
                    bytes[index + 1] = 0;
                }
                index += 2;
            }
            return writeDisplayData(bytes);
        }
        return false;
    }

    private boolean writeDisplayData(byte[] data) {
        if (writeCommand(DISPLAY_DATA_REGISTER, 0)) {
            try {
                device.write(data, data.length);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
