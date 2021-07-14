package fr.arnaudguyon.blindtest.bluetooth;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.inventhys.blecentrallib.Central;
import com.inventhys.blecentrallib.PeripheralRemote;
import com.inventhys.blecentrallib.connection.ConnectListener;
import com.inventhys.blecentrallib.connection.ConnectionState;
import com.inventhys.blecentrallib.scan.ScanListener;
import com.inventhys.blecentrallib.scan.ScanResult;
import com.inventhys.blecentrallib.scan.StopScanListener;
import com.inventhys.blecentrallib.scan.StopScanResult;

import java.util.List;

import fr.arnaudguyon.perm.Perm;
import fr.arnaudguyon.perm.PermResult;

public class BluetoothActivity extends AppCompatActivity {

    // TODO: check bluetooth is ON, location is ON

    private static final String TAG = "BluetoothActivity";
    private static final int PERMISSIONS_REQUEST = 1;
    private static final String[] PERMISSIONS = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION};

    private enum State {
        IDLE,
        PERMISSION,
        SCANNING,
        DISCONNECTED,
        CONNECTED
    }

    private State state = State.IDLE;

    public static Intent createIntent(@NonNull Context context) {
        Intent intent = new Intent(context, BluetoothActivity.class);
        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (state != State.CONNECTED) {
            checkPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermResult permResult = new PermResult(permissions, grantResults);
        if (permResult.areGranted()) {
            scan();
        } else {
            finish();
        }
    }

    private void checkPermissions() {
        final Perm perm = new Perm(this, PERMISSIONS);
        if (perm.areGranted()) {
            scan();
        } else {
            state = State.PERMISSION;
            perm.askPermissions(PERMISSIONS_REQUEST);
        }
    }

    private void scan() {
        state = State.SCANNING;
        final Central central = Central.getInstance();
        central.scan(this, 20, null, new ScanListener() {
            @Override
            public void onScanFinish(ScanResult scanResult, @Nullable List<PeripheralRemote> list) {

            }

            @Override
            public void onPeripheralDiscover(@NonNull PeripheralRemote peripheralRemote, @NonNull android.bluetooth.le.ScanResult scanResult) {
                String name = peripheralRemote.getName();
                Log.i(TAG, "Found BLE device: " + name);
                if (TextUtils.equals(name, "Blind Test")) {
                    toast("Found Blind Test");
                    central.stopScan(BluetoothActivity.this, new StopScanListener() {
                        @Override
                        public void onScanStopped(StopScanResult stopScanResult, @Nullable List<PeripheralRemote> list) {
                            connect(peripheralRemote);
                        }
                    });
                }
            }

            @Override
            public void onPeripheralUpdate(@NonNull PeripheralRemote peripheralRemote, @NonNull android.bluetooth.le.ScanResult scanResult) {

            }
        });
    }

    private void connect(@NonNull PeripheralRemote peripheralRemote) {
        state = State.DISCONNECTED;
        Central.getInstance().connect(this, peripheralRemote, null, new ConnectListener() {
            @Override
            public void onConnectionChange(@NonNull PeripheralRemote peripheralRemote, @NonNull ConnectionState connectionState) {
                if (connectionState == ConnectionState.READY) {
                    state = State.CONNECTED;
                    // continue
                    toast("Connected, Ready to play");
                } else {
                    if (state == State.CONNECTED) {
                        toast("Disconnected");
                        // TODO: stop / restart something?
                        state = State.DISCONNECTED;
                    }
                }
            }
        });
    }

    private void toast(@NonNull String string) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }

}
