/*import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class AA extends Activity {

    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> discoveredDevices = new ArrayList<>();
    private ArrayAdapter<String> deviceListAdapter;
    private ListView deviceListView;
    private TextView statusLabel;

    private final BroadcastReceiver discoveryResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    discoveredDevices.add(device);
                    String deviceName = device.getName();
                    if (deviceName != null) {
                        deviceListAdapter.add(deviceName);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceListView = findViewById(R.id.deviceListView);
        deviceListView.setAdapter(deviceListAdapter);
        statusLabel = findViewById(R.id.statusLabel);

        Button connectButton = findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDiscovery();
            }
        });

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "User Enabled Bluetooth", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "User Did not enable Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startDiscovery() {
        deviceListAdapter.clear();
        discoveredDevices.clear();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, 1001);
            return;
        }

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryResult, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(discoveryResult);
    }
} */

