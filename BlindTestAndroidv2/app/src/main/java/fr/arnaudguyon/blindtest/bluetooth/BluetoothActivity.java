package fr.arnaudguyon.blindtest.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.location.LocationManagerCompat;

import com.google.android.material.snackbar.Snackbar;
import com.inventhys.blecentrallib.Central;
import com.inventhys.blecentrallib.Helper;
import com.inventhys.blecentrallib.PeripheralRemote;
import com.inventhys.blecentrallib.connection.ConnectListener;
import com.inventhys.blecentrallib.connection.ConnectionState;
import com.inventhys.blecentrallib.connection.MtuUpdateListener;
import com.inventhys.blecentrallib.connection.PhyUpdateListener;
import com.inventhys.blecentrallib.scan.ScanListener;
import com.inventhys.blecentrallib.scan.ScanResult;
import com.inventhys.blecentrallib.scan.StopScanListener;
import com.inventhys.blecentrallib.scan.StopScanResult;
import com.inventhys.blecentrallib.transfer.RegisterForNotificationListener;
import com.inventhys.blecentrallib.transfer.RegisterForNotificationResult;
import com.inventhys.blecommonlib.ByteHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.arnaudguyon.blindtest.R;
import fr.arnaudguyon.blindtest.game.ArduinoPlayer;
import fr.arnaudguyon.blindtest.game.GameActivity;
import fr.arnaudguyon.blindtest.game.Player;
import fr.arnaudguyon.blindtest.game.Team;
import fr.arnaudguyon.perm.Perm;
import fr.arnaudguyon.perm.PermResult;

public class BluetoothActivity extends GameActivity implements RegisterForNotificationListener, MtuUpdateListener, PhyUpdateListener {

    private static final String TAG = "BluetoothActivity";
    private static final int PERMISSIONS_REQUEST = 1;
    private static final int ENABLE_BLUETOOTH_REQUEST = 2;
    private static final int ENABLE_LOCATION_REQUEST = 3;
    private static final String[] PERMISSIONS = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION};

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
    private Snackbar snackbar;

    public static Intent createIntent(@NonNull Context context) {
        Intent intent = new Intent(context, BluetoothActivity.class);
        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_empty);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Subscribe to bluetooth and location features changes
        IntentFilter featureEventFilter = new IntentFilter();
        featureEventFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        featureEventFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(bleAndLoctionReceiver, featureEventFilter);
    }

    @Override
    protected void onStop() {
        unregisterReceiver(bleAndLoctionReceiver);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (checkBluetoothActivated()) {
            onBleActive();
        } else {
            requestEnableBle();
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
            if (state != State.CONNECTED) {
                scan();
            }
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
                    Central.getInstance().updatePhyListener(peripheralRemote, BluetoothActivity.this);
                    Central.getInstance().changeMtuSize(BluetoothActivity.this, peripheralRemote, 255);
                    Central.getInstance().setPreferredPhy(peripheralRemote, BluetoothDevice.PHY_LE_2M_MASK, BluetoothDevice.PHY_OPTION_NO_PREFERRED);
                    peripheralRemote.setCallbackThread(BleConst.NORDIC_UART_SERVICE, BleConst.TX_NOTIFY, handler);
                    peripheralRemote.registerForNotification(BleConst.NORDIC_UART_SERVICE, BleConst.TX_NOTIFY, null, BluetoothActivity.this);
                } else {
                    if (state == State.CONNECTED) {
                        toast("Disconnected");
                        state = State.DISCONNECTED;
                        onActivityNotReady();
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

        Team redTeam = new Team(Team.TeamColor.RED);
        Player playerRed = new ArduinoPlayer(Team.TeamColor.RED, peripheralRemote);
        redTeam.addPlayer(playerRed);

        Team yellowTeam = new Team(Team.TeamColor.YELLOW);
        Player playerYellow = new ArduinoPlayer(Team.TeamColor.YELLOW, peripheralRemote);
        yellowTeam.addPlayer(playerYellow);

        ArrayList<Team> teams = new ArrayList<>();
        teams.add(redTeam);
        teams.add(yellowTeam);
        onActivityReady(teams);

    }

    @Override
    public void onNotification(@NonNull UUID uuid, @NonNull UUID uuid1, @Nullable byte[] bytes) {
        if ((bytes != null) && (bytes.length > 0)) {
            String text = ByteHelper.byteArrayToHexaString(bytes);
            Log.i(TAG, text);
            for (byte b : bytes) {
                if (b == 'r') {
                    redPressed();
                } else if (b == 'y') {
                    yellowPressed();
                }
            }
        }
    }


    @Override
    public void onMtuUpdate(@NonNull PeripheralRemote peripheralRemote, int mtu) {
        Log.i(TAG, "Mtu Update " + mtu);
    }

    @Override
    public void onPhyUpdate(@NonNull PeripheralRemote peripheralRemote, int txPhy, int rxPhy) {
        Log.i(TAG, "Phy TX: " + txPhy + ", Phy RX: " + rxPhy);
    }

    private void requestEnableBle() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        showSnack(R.string.snack_disabled_ble, Snackbar.LENGTH_INDEFINITE, R.string.snack_enable_ble, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnack(R.string.snack_enabling_ble, Snackbar.LENGTH_INDEFINITE);
                bluetoothAdapter.enable();
            }
        });
    }

    private void requestEnableLocation() {
        showSnack(R.string.snack_disabled_location, Snackbar.LENGTH_INDEFINITE, R.string.snack_enable_location, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), ENABLE_LOCATION_REQUEST);
                hideSnack();
            }
        });
    }

    private void hideSnack() {
        if (snackbar != null) {
            snackbar.dismiss();
            snackbar = null;
        }
    }

    private void showSnack(int message, int duration) {
        showSnack(message, duration, -1, null);
    }

    private void showSnack(int message, int duration, int action, View.OnClickListener actionCallback) {
        hideSnack();
        snackbar = Snackbar.make(findViewById(R.id.mainLayout), message, duration);
        if (action > -1) {
            snackbar.setAction(action, actionCallback);
        }
        snackbar.show();
    }

    private boolean checkBluetoothActivated() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            return (adapter.isEnabled());
        } else {
            finish();
        }
        return false;
    }

    private boolean checkLocationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm != null) {
            return LocationManagerCompat.isLocationEnabled(lm);
        } else {
            finish();
            return false;
        }
    }

    private final BroadcastReceiver bleAndLoctionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action == null) {
                return;
            }

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        finish();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        showSnack(R.string.snack_enabled_ble, Snackbar.LENGTH_SHORT);
                        onBleActive();
                        break;
                }

            } else if (action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                boolean isGpsEnabled = false;
                boolean isNetworkEnabled = false;
                if (locationManager != null) {
                    isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                }

                if (isGpsEnabled || isNetworkEnabled) {
                    showSnack(R.string.snack_enabled_location, Snackbar.LENGTH_SHORT);
                    checkPermissions();
                } else {
                    finish();
                }
            }
        }
    };

    private void onBleActive() {
        if (checkLocationEnabled()) {
            checkPermissions();
        } else {
            requestEnableLocation();
        }
    }

}
