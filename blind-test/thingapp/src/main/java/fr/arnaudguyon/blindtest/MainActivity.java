package fr.arnaudguyon.blindtest;

import android.app.Activity;
import android.os.Bundle;

import java.io.IOException;
import java.util.List;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SEGMENT_ADDRESS = 0x70;

    private I2cDevice mI2cDevice;
    private HT16K33Device mHT16K33Device;
    private Pwm mPWMDevice;
    private Score score;
    private Handler mHandler = new Handler();

    private int mColor = 0x93FF00;  //GRB
    private int mBit = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setupNumericDisplay();
        setContentView(R.layout.main_activity);

        TextView textView = findViewById(R.id.log);

        PeripheralManager manager = PeripheralManager.getInstance();

        // gpio 24 leds ring
        // see https://github.com/androidthings/sample-simplepio/tree/master/java/pwm/src/main/java/com/example/androidthings/simplepio
        List<String> pwmList = manager.getPwmList();
        if (pwmList.isEmpty()) {
            Log.i(TAG, "No PWM port available on this device.");
            textView.setText("No PWM port available on this device.");
        } else {
            Log.i(TAG, "List of PWM available ports: " + pwmList);
            textView.setText("List of PWM available ports: " + pwmList);
            try {
                mPWMDevice = manager.openPwm("PWM1");

                mPWMDevice.setPwmFrequencyHz(100);
                //mPWMDevice.setPwmDutyCycle(25);

                // Enable the PWM signal
                //mPWMDevice.setEnabled(true);
                mHandler.post(mChangePWMRunnable);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // i2c 7-segment

//        List<String> deviceList = manager.getI2cBusList();
//        if (deviceList.isEmpty()) {
//            Log.i(TAG, "No I2C bus available on this device.");
//            textView.setText("No I2C found");
//        } else {
//            Log.i(TAG, "List of available devices: " + deviceList);
//            textView.setText("I2C devices " + deviceList);
//
//            String name1 = deviceList.get(0);
//            try {
//                mI2cDevice = manager.openI2cDevice(name1, SEGMENT_ADDRESS);
//                Log.i(TAG, "opened device " + name1);
//                textView.setText("opened device " + name1);
//            } catch (IOException e) {
//                Log.w(TAG, "Unable to access I2C device " + name1, e);
//            }
//        }
//
//        if (mI2cDevice != null) {
//            mHT16K33Device = new HT16K33Device(mI2cDevice);
//            mHT16K33Device.init();
//
//            score = new ThingScore(mHT16K33Device);
//            score.setScoreRed(0);
//            score.setScoreYellow(0);
//            //score.printScore();
//            //score.setText("LE A ");
//            score.setText("-H I-");
//        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mPWMDevice != null) {
            try {
                mPWMDevice.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mPWMDevice = null;
        }

        if (mHT16K33Device != null) {
            mHT16K33Device.deinit();
            mHT16K33Device = null;
        }

        if (mI2cDevice != null) {
            try {
                mI2cDevice.close();
                mI2cDevice = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close I2C device", e);
            }
        }
    }

    private Runnable mChangePWMRunnable = new Runnable() {
        @Override
        public void run() {

//            if (mBit == 23) {
//                try {
//                    mPWMDevice.setEnabled(true);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }

            Log.i(TAG, "Start Runnable");

            for(int bit=23; bit>=0; --bit) {
                boolean one = ((mColor & (1 << bit)) != 0);
                try {
                    if (one) {
                        mPWMDevice.setPwmDutyCycle(0.5);
                        //Log.i(TAG, "bit " + bit + " -> 0.5");
                    } else {
                        mPWMDevice.setPwmDutyCycle(0.25);
                        //Log.i(TAG, "bit " + bit + " -> 0.25");
                    }
                    try {
                        Thread.sleep(0, 1250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                mPWMDevice.setPwmDutyCycle(0.25);
                Thread.sleep(0, 50000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

//            try {
//                mPWMDevice.setEnabled(false);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }


//
//            if (mBit >= 0) {
//                boolean one = (mBit & 1) != 0; // ((mColor & (1 << mBit)) != 0);
//                try {
//                    if (one) {
//                        mPWMDevice.setPwmDutyCycle(0.5);
//                        Log.i(TAG, "bit " + mBit + " -> 0.5");
//                    } else {
//                        mPWMDevice.setPwmDutyCycle(0.25);
//                        Log.i(TAG, "bit " + mBit + " -> 0.25");
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                --mBit;
//                mHandler.postDelayed(this, 10);
//            } else {
//                // end?
////                try {
////                    mPWMDevice.setEnabled(false);
////                    Log.i(TAG, "end");
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
//            }
            Log.i(TAG, "End Of Runnable");
        }
    };
}
