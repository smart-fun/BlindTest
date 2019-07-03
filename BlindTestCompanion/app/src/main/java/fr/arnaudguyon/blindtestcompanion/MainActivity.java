package fr.arnaudguyon.blindtestcompanion;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import fr.arnaudguyon.perm.Perm;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSIONS = 2;

    private static final String PERMISSIONS[] = {Manifest.permission.ACCESS_FINE_LOCATION};

    private boolean permissionBleChecked = false;
    private boolean permissionLocationChecked = false;

    private enum BleState {
        IDLE,
        SCANNING
    }

    private BleState bleState = BleState.IDLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        if (checkPermissions()) {
            if (bleState == BleState.IDLE) {
                BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                startScan(bluetoothAdapter);
            }
        } else {
            if (permissionBleChecked && permissionLocationChecked) {
                finish();
            }
        }

    }

    private boolean checkPermissions() {

        // BLE
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            if (!permissionBleChecked) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            return false;
        }

        // LOCATION
        final Perm perm = new Perm(this, PERMISSIONS);
        if (!perm.areGranted()) {
            if (!permissionLocationChecked) {
                perm.askPermissions(REQUEST_PERMISSIONS);
            }
            return false;
        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mReceiver);
        permissionBleChecked = false;
        permissionLocationChecked = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            permissionBleChecked = true;
        } else if (requestCode == REQUEST_PERMISSIONS) {
            permissionLocationChecked = true;
        }

        if (checkPermissions()) {
            if (bleState == BleState.IDLE) {
                BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                startScan(bluetoothAdapter);
            }
        } else {
            if (permissionBleChecked && permissionLocationChecked) {
                finish();
            }
        }
    }

    private void startScan(@NonNull BluetoothAdapter bluetoothAdapter) {
        if (bleState != BleState.IDLE) {
            return;
        }
        Log.i(TAG, "Start Scan");
        bleState = BleState.SCANNING;

        bluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Toast.makeText(MainActivity.this, "Found device " + device.getName(), Toast.LENGTH_SHORT).show();
            }
        }
    };

}
