package fr.arnaudguyon.blindtest;

import android.support.annotation.NonNull;

import com.google.android.things.pio.I2cDevice;

import java.io.IOException;

// see https://cdn-shop.adafruit.com/datasheets/ht16K33v110.pdf
// see https://github.com/lpaseen/ht16k33/blob/master/src/ht16k33.cpp
// see https://github.com/adafruit/Adafruit_LED_Backpack/blob/master/Adafruit_LEDBackpack.cpp

public class HT16K33Device {

    // REGISTERS
    private static final int DISPLAY_DATA_REGISTER  = 0x00;
    private static final int SYSTEM_SETUP_REGISTER  = 0x20;
    private static final int DISPLAY_SETUP_REGISTER = 0x80;
    private static final int ROW_INT_SET_REGISTER   = 0xA0;

//    private static final int COMMAND_DISPLAY_DATA   = 0b00000000;   // 0x00
//    private static final int COMMAND_SYSTEM_SETUP   = 0b00100000;   // 0x20
//    private static final int COMMAND_KEY_DATA       = 0b01000000;
//    private static final int COMMAND_INT_FLAG       = 0b01100000;
//    private static final int COMMAND_DISPLAY_SETUP  = 0b10000000;   // 0x80
//    private static final int COMMAND_ROW_INT_SET    = 0b10100000;   // 0xA0
//    private static final int COMMAND_DIMMING        = 0b11100000;   // BRIGHTNESS

    private static final int SYSTEM_SETUP_OFF = 0;
    private static final int SYSTEM_SETUP_ON = 1;

    private static final int DISPLAY_SETUP_OFF = 0;
    private static final int DISPLAY_SETUP_ON = 1;
    private static final int DISPLAY_SETUP_BLINK_NONE = 0;
    private static final int DISPLAY_SETUP_BLINK_500MS = 2;
    private static final int DISPLAY_SETUP_BLINK_1000MS = 4;
    private static final int DISPLAY_SETUP_BLINK_2000MS = 6;

    private I2cDevice device;

    public HT16K33Device(@NonNull I2cDevice device) {
        this.device = device;
    }

    public void init() {
        writeCommand(SYSTEM_SETUP_REGISTER, SYSTEM_SETUP_ON);
        writeCommand(DISPLAY_SETUP_REGISTER, DISPLAY_SETUP_ON | DISPLAY_SETUP_BLINK_NONE);

// highest bit = dot

        // bits: top, topR, botR, bot, botL, topL, mid, dot
        // bytes: array of index, value. indexes: 0,1,3,4 for numbers, 2 for ":"

        byte[] test = {
                0, 0x39,
                1, 0x3F,
                2, 0x0,
                3, 0x3F,
                4, 0x38
        //                ,
//                5, 0x6D,
//                6, 0x7D,
//                7, 0x7D

        };
        writeDisplayData(test);

//        static const uint8_t numbertable[] = {
//                0x3F, /* 0 */
//                0x06, /* 1 */
//                0x5B, /* 2 */
//                0x4F, /* 3 */
//                0x66, /* 4 */
//                0x6D, /* 5 */
//                0x7D, /* 6 */
//                0x07, /* 7 */
//                0x7F, /* 8 */
//                0x6F, /* 9 */
//                0x77, /* a */
//                0x7C, /* b */
//                0x39, /* C */
//                0x5E, /* d */
//                0x79, /* E */
//                0x71, /* F */
//        };
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
