package fr.arnaudguyon.blindtest;

import android.app.Activity;
import android.os.Bundle;

import java.io.IOException;
import java.util.List;

import android.util.Log;
import android.widget.TextView;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SEGMENT_ADDRESS = 0x70;

    private I2cDevice mI2cDevice;
    private HT16K33Device mHT16K33Device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setupNumericDisplay();
        setContentView(R.layout.main_activity);

        TextView textView = findViewById(R.id.log);

        PeripheralManager manager = PeripheralManager.getInstance();
        List<String> deviceList = manager.getI2cBusList();
        if (deviceList.isEmpty()) {
            Log.i(TAG, "No I2C bus available on this device.");
            textView.setText("No I2C found");
        } else {
            Log.i(TAG, "List of available devices: " + deviceList);
            textView.setText("I2C devices " + deviceList);

            String name1 = deviceList.get(0);
            try {
                mI2cDevice = manager.openI2cDevice(name1, SEGMENT_ADDRESS);
                Log.i(TAG, "opened device " + name1);
                textView.setText("opened device " + name1);
            } catch (IOException e) {
                Log.w(TAG, "Unable to access I2C device " + name1, e);
            }
        }

        if (mI2cDevice != null) {
            mHT16K33Device = new HT16K33Device(mI2cDevice);
            mHT16K33Device.init();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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

}
