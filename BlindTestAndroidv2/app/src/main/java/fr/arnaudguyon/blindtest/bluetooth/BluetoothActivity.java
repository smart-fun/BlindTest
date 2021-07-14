package fr.arnaudguyon.blindtest.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.inventhys.blecentrallib.Central;
import com.inventhys.blecentrallib.Helper;
import com.inventhys.blecentrallib.PeripheralRemote;
import com.inventhys.blecentrallib.connection.ConnectListener;
import com.inventhys.blecentrallib.connection.ConnectOptions;
import com.inventhys.blecentrallib.connection.ConnectionState;
import com.inventhys.blecentrallib.connection.MtuUpdateListener;
import com.inventhys.blecentrallib.scan.ScanListener;
import com.inventhys.blecentrallib.scan.ScanResult;
import com.inventhys.blecentrallib.scan.StopScanListener;
import com.inventhys.blecentrallib.scan.StopScanResult;
import com.inventhys.blecentrallib.transfer.RegisterForNotificationListener;
import com.inventhys.blecentrallib.transfer.RegisterForNotificationResult;
import com.inventhys.blecentrallib.transfer.WriteCharacteristicListener;
import com.inventhys.blecentrallib.transfer.WriteCharacteristicResult;
import com.inventhys.blecommonlib.ByteHelper;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

import fr.arnaudguyon.blindtest.R;
import fr.arnaudguyon.perm.Perm;
import fr.arnaudguyon.perm.PermResult;

public class BluetoothActivity extends AppCompatActivity implements RegisterForNotificationListener, MtuUpdateListener {

    // TODO: check bluetooth is ON, location is ON

    private static final String TAG = "BluetoothActivity";
    private static final int PERMISSIONS_REQUEST = 1;
    private static final String[] PERMISSIONS = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION};

    private static final UUID NORDIC_UART_SERVICE = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID RX_WRITE = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID TX_NOTIFY = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    private enum State {
        IDLE,
        PERMISSION,
        SCANNING,
        DISCONNECTED,
        CONNECTED
    }

    private State state = State.IDLE;
    private PeripheralRemote peripheralRemote;
    private Handler handler = new Handler();

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
                BluetoothActivity.this.peripheralRemote = peripheralRemote;
                if (connectionState == ConnectionState.READY) {
                    state = State.CONNECTED;
                    // continue
                    toast("Connected, Ready to play");
                    BluetoothGatt gatt = Helper.getBluetoothGatt(peripheralRemote);
                    if (gatt != null) {
                        List<BluetoothGattService> services = gatt.getServices();
                        for (BluetoothGattService service : services) {
                            Log.i(TAG, "Service " + service.getUuid().toString());
                            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                            for (BluetoothGattCharacteristic characteristic : characteristics) {
                                Log.i(TAG, "    Chara " + characteristic.getUuid().toString());
                            }
                        }
                    }
                    Central.getInstance().updateMtuListener(peripheralRemote, BluetoothActivity.this);
                    Central.getInstance().changeMtuSize(BluetoothActivity.this, peripheralRemote, 255);
                    Central.getInstance().setPreferredPhy(peripheralRemote, BluetoothDevice.PHY_LE_2M_MASK, BluetoothDevice.PHY_OPTION_NO_PREFERRED);
                    peripheralRemote.setCallbackThread(NORDIC_UART_SERVICE, TX_NOTIFY, handler);
                    peripheralRemote.registerForNotification(NORDIC_UART_SERVICE, TX_NOTIFY, null, BluetoothActivity.this);
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

    @Override
    public void onRegisterForNotification(@NonNull RegisterForNotificationResult result, @NonNull UUID uuid, @NonNull UUID uuid1) {


        setContentView(R.layout.activity_bluetooth);

        findViewById(R.id.redTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRandom64('R');
            }
        });

        findViewById(R.id.yellowTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRandom64('Y');
            }
        });

    }

    @Override
    public void onNotification(@NonNull UUID uuid, @NonNull UUID uuid1, @Nullable byte[] bytes) {
        if ((bytes != null) && (bytes.length > 0)) {
            String text = ByteHelper.byteArrayToHexaString(bytes);
            Log.i(TAG, text);
            for(byte b : bytes) {
                if (b == 'r') {
                    redPressed();
                } else if (b == 'y') {
                    yellowPressed();
                }
            }
        }
    }

    private void sendRandom64(char prefix) {
        if (peripheralRemote != null) {
            byte[] data = new byte[20];
            data[0] = (byte) prefix;
            int index = 0;
            long rnd = (long) (Math.random() * Long.MAX_VALUE);
            while (rnd > 0) {
                long value = rnd % 26;
                ++index;
                byte b = (byte) ('a' + value);
                data[index] = b;
                rnd /= 26;
            }
            ++index;
            data[index] = 0;
            int size = index + 1;
            byte[] tosend = new byte[size];
            ByteBuffer.wrap(tosend).put(data, 0, size);
            peripheralRemote.writeCharacteristic(NORDIC_UART_SERVICE, RX_WRITE, tosend, new WriteCharacteristicListener() {
                @Override
                public void onCharacteristicWrite(@NonNull WriteCharacteristicResult result, @NonNull UUID uuid, @NonNull UUID uuid1, @Nullable byte[] bytes) {
                    if (result == WriteCharacteristicResult.SUCCESS) {
                        Log.i(TAG, "Sent " + ByteHelper.byteArrayToHexaString(tosend));
                    } else {
                        Log.i(TAG, result.name());
                    }
                }
            });
        }

    }

    private void redPressed() {
        Log.i(TAG, "Red Pressed");
    }

    private void yellowPressed() {
        Log.i(TAG, "Yellow Pressed");
    }

    @Override
    public void onMtuUpdate(@NonNull PeripheralRemote peripheralRemote, int mtu) {
        Log.i(TAG, "Mtu Update " + mtu);
    }

}
