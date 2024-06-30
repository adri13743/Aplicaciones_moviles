package com.example.my_app_00;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.*;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends Activity {

    TextView receivedEditText;
    EditText sendTextEditText;
    Button acceptConnectionButton;
    Button closeConnectionButton;
    Button sendTextButton;
    TextView BT_MAC_TextView;
    TextView BT_Name_TextView;
    TextView BT_ConnectionState_TextView;
    BluetoothAdapter mBluetoothAdapter;
    int REQUEST_ENABLE_BT = 1;
    android.os.Handler handlerNetworkExecutorResult;
    BluetoothServerManager btServerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.receivedEditText = (TextView) findViewById(R.id.receivedEditText);
        this.BT_MAC_TextView = (TextView) findViewById(R.id.BT_MAC_TextView);
        this.BT_Name_TextView = (TextView) findViewById(R.id.BT_Name_TextView);
        this.BT_ConnectionState_TextView = (TextView) findViewById(R.id.BT_ConnectionState_TextView);
        this.sendTextEditText = (EditText) findViewById(R.id.sendTextEditText);
        this.acceptConnectionButton = (Button) findViewById(R.id.acceptConnectionButton);
        this.closeConnectionButton = (Button) findViewById(R.id.closeConnectionButton);
        this.sendTextButton = (Button) findViewById(R.id.sendTextButton);
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.acceptConnectionButton.setEnabled(true);
        this.closeConnectionButton.setEnabled(false);
        this.sendTextButton.setEnabled(false);
        this.sendTextEditText.setEnabled(false);
        showBT_MAC_NAME();
        this.BT_ConnectionState_TextView.setText("NOT CONNECTED");
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); //REQUEST_ENABLE_BT = 1;
        }
        handlerNetworkExecutorResult = new android.os.Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = (String) msg.obj;
                if (msg != null) {
                    if (msg.arg1 == 0) { //Datos recibidos por el Bluetooth desde elcliente
                        receivedEditText.append(message + "\n");
                    } else if (msg.arg1 == 1) { //Informacion del estado del socket
                        BT_ConnectionState_TextView.setText(message);
                    }
                }
            }
        };



    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth enabled",
                        Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth NOT enabled",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showBT_MAC_NAME() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        String BT_name = this.mBluetoothAdapter.getName();
        String BT_MAC = this.mBluetoothAdapter.getAddress();
        this.BT_MAC_TextView.setText("BT MAC:"+BT_MAC);
        this.BT_Name_TextView.setText("BT Name:"+BT_name);
    }
    private void initializeBlueetoothServerManager(){

        if (this.btServerManager == null) {
            this.btServerManager = new BluetoothServerManager(this.mBluetoothAdapter, handlerNetworkExecutorResult, this);
            this.btServerManager.start();
        }else{
            this.btServerManager.exitCurrentConnection = true;
            //this.btServerManager.cancel();
        }
    }
    public void acceptConnectionButtonOnClick(View v) {
        initializeBlueetoothServerManager();
        this.acceptConnectionButton.setEnabled(false);
        this.closeConnectionButton.setEnabled(true);
        this.sendTextButton.setEnabled(true);
        this.sendTextEditText.setEnabled(true);
    }
    public void closeConnectionButtonOnClick(View v) {
        initializeBlueetoothServerManager();
        this.acceptConnectionButton.setEnabled(true);
        this.closeConnectionButton.setEnabled(false);
        this.sendTextButton.setEnabled(false);
        this.sendTextEditText.setEnabled(false);
        this.BT_ConnectionState_TextView.setText("NOT CONNECTED");
    }








}
