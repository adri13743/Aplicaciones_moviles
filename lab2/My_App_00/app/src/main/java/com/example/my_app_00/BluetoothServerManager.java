package com.example.my_app_00;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothServerManager extends Thread {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothServerSocket bluetoothServerSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Handler handlerNetworkExecutorResult;
    public static Boolean exitCurrentConnection = false;
    MainActivity actividad;

    public BluetoothServerManager(BluetoothAdapter _bluetoothAdapter, Handler _handlerNetworkExecutorResult, MainActivity actividad) {
        this.bluetoothAdapter = _bluetoothAdapter;
        this.handlerNetworkExecutorResult = _handlerNetworkExecutorResult;
        this.actividad=actividad;
        try {
            UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            String NAME = "BTROBOTSERVERSIM";
            if (ActivityCompat.checkSelfPermission(actividad, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            bluetoothServerSocket = _bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) {
            Log.e("Error:", "Bluetooth connection:" + e);
        }
    }

    public void showDisplayMessage(String displayMessage) {
        Message msg = new Message();
        msg.arg1 = 0;
        msg.obj = displayMessage.replaceAll("_", " ");
        handlerNetworkExecutorResult.sendMessage(msg);
    }

    public void showSocketStateMessage(String displayMessage) {
        Message msg = new Message();
        msg.arg1 = 1;
        msg.obj = displayMessage.replaceAll("_", " ");
        handlerNetworkExecutorResult.sendMessage(msg);
    }

    public void run() {
        BluetoothSocket socket = null;
        while (true) {
            try {
                showSocketStateMessage("WAITING FOR CONNECTION");
                socket = bluetoothServerSocket.accept();
                String remoteDeviceAddress = socket.getRemoteDevice().getAddress();
                if (ActivityCompat.checkSelfPermission(actividad, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                String remoteDeviceName = socket.getRemoteDevice().getName();
                showSocketStateMessage("CONNECTED: NAME="+remoteDeviceName+": MAC="+remoteDeviceAddress);
            } catch (IOException e) {
                showSocketStateMessage("SOCKET ERROR:"+e);
                break;
            }
            if (socket != null) {
                manageConnectedSocket(socket);
            }
        }
    }
    public void manageConnectedSocket(BluetoothSocket socket){
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            exitCurrentConnection = false;
            while (!exitCurrentConnection) {
                String cadena = "";
                byte[] buffer = new byte[1024];
                for (int i=0; i<1024; i++){
                    buffer[i]=0;
                }
                inputStream.read(buffer);
                cadena = new String(buffer);
                if (cadena != null) {
                    if((cadena.equals("EXIT")||(cadena.equals("exit")))){
                        exitCurrentConnection = true;
                    }else{
                        showDisplayMessage(cadena);
                    }
                }
            }
        }catch(IOException e){
            Log.d("ERROR:",""+e);
        }
    }




}
