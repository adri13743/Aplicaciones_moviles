package com.example.my_app_00;

import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.text.format.Formatter;
import android.view.View;
import android.widget.*;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;
import android.os.Handler;

import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
public class MainActivity extends Activity {
    private ArrayList<Object> deviceList = new ArrayList<Object>();
    BroadcastReceiver discoveryResult;
    InputStream inputStream;
    OutputStream outputStream;
    BluetoothSocket btSocket;
    public static Boolean bluetoothActive = false;

    Camera mCamera; //p5
    CameraPreview mCameraPreview; //p5
    Handler handlerNetworkExecutorResult;//p5
    String fileStr;//p5
    TextView BT_IP_TextView;//p5
    public android.hardware.Camera.PictureCallback mPicture;//p5
    public static Boolean guardado = false;
    public boolean isGuardado() {
        return guardado;
    }
    public void setGuardado(boolean guardado) {
        this.guardado = guardado;
    }

    byte[] resizeImage(byte[] input) { //p5
        Bitmap originalBitmap = BitmapFactory.decodeByteArray(input, 0, input.length);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 80, 107,
                true);
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, blob);
        return blob.toByteArray();
    } //p5


    public String readResourceTextFile() {//p5
        String fileStr = "";
        InputStream is = getResources().openRawResource(R.raw.index);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String readLine = null;
        try {
            while ((readLine = br.readLine()) != null) {
                fileStr = fileStr + readLine + "\r\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileStr;
    }//p5


    NetworkExecutor networkExecutor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectButton = (Button) findViewById(R.id.connectButton);
        disconnectButton = (Button) findViewById(R.id.disconnectButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        forwardButton = (Button) findViewById(R.id.forButton);
        F_LeftButton = (Button) findViewById(R.id.F_LeftButton);
        F_RightButton = (Button) findViewById(R.id.F_RightButton);
        backwardButton = (Button) findViewById(R.id.backButton);
        turnLeftForwardButton = (Button) findViewById(R.id.leftButton);
        turnRightForwardButton = (Button) findViewById(R.id.rightButton);
        V_1Button = (Button) findViewById(R.id.V_1Button);
        V_2Button = (Button) findViewById(R.id.V_2Button);
        V_3Button = (Button) findViewById(R.id.V_3Button);
        V_4Button = (Button) findViewById(R.id.V_4Button);
        this.BT_IP_TextView = (TextView) findViewById(R.id.BT_IP_TextView);

        cameraPreviewFrameLayout = (FrameLayout) findViewById(R.id.cameraView); //p5

        mCamera = getCameraInstance(); //p5
        mCameraPreview = new CameraPreview(this, mCamera); //p5
        cameraPreviewFrameLayout = (FrameLayout) findViewById(R.id.cameraView); //p5
        cameraPreviewFrameLayout.addView(mCameraPreview); //p5
        handlerNetworkExecutorResult = new Handler() {//p5
            @Override
            public void handleMessage(Message msg) {
                Log.d("handlerNetwExecResult", (String) msg.obj);
                if (msg != null) {
                    if (msg.obj.equals("FORWARD")) {
                        forward();
                    } else if (msg.obj.equals("BACKWARD")) {
                        backward();
                    } else if (msg.obj.equals("LEFT")) {
                        turnLeftForward();
                    } else if (msg.obj.equals("RIGHT")) {
                        turnRightForward();
                    } else if (msg.obj.equals("CAMERA")) {
                        captureCamera();
                    } else if (msg.obj.equals("STOP")) {
                        stop();
                    }
                }
            }
        };//p5
        fileStr = readResourceTextFile();//p5
        networkExecutor = new NetworkExecutor(fileStr,handlerNetworkExecutorResult);//p5
        networkExecutor.start();//p5
        showBT_IP_NAME(this);//p5
        mPicture = new android.hardware.Camera.PictureCallback() { //p5
            @Override
            public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
                byte[] resized = resizeImage(data);
                File pictureFile = getOutputMediaFile();
                if (pictureFile == null) {
                    return;
                }
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(resized);
                    fos.close();
                    setGuardado(true);
                } catch (Exception e) {
                    Log.e("onPictureTaken", "ERROR:" + e);
                }
            }
        };
        //statusLabel = (TextView) findViewById(R.id.statusLabel);
        final boolean[] conectado = {false};
        discoveryResult = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String remoteDeviceName =
                        intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                BluetoothDevice remoteDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi =
                        intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                deviceList.add(remoteDevice);
                if (remoteDeviceName!=null) {
                    Log.d("MyFirstApp", "Discovered " + remoteDeviceName);
                    Log.d("MyFirstApp", "RSSI " + rssi + "dBm");
                    if (remoteDeviceName.equals("ROBOTIS_210_D4") && !conectado[0]) { //p4
                        Log.d("MyFirstApp", "Discovered ROBOTIS_210_D4:connecting");
                        connect(remoteDevice);
                        conectado[0] = true;
                    }//p4
                }

            }
        };



        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("MyFirstApp", "connectButton Clicked");
                //statusLabel.setText("Connect button pressed");
                //String macAddressToConnect = "B8:63:BC:00:94:D4";//p4 ejercicio extra con direccion mac
                //connectByMacAddress(macAddressToConnect); //p4 ejercicio extra con direccion mac
                startDiscovery(); // para probar ejercicio extra con direccion mac comentar esta linea y descomentar las dos anteriores
            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("MyFirstApp", "disconnectButton Clicked");
                //statusLabel.setText("disconnectButton button pressed");
                disconnect();
                conectado[0] = false;
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Log.d("MyFirstApp", "forwardButton Clicked");
                //statusLabel.setText("forward button pressed");
                forward();
            }
        });
        F_LeftButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Log.d("MyFirstApp", "F_LeftButton Clicked");
                //statusLabel.setText("forward left button pressed");
                forward_left();
            }
        });
        F_RightButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Log.d("MyFirstApp", "F_RightButton Clicked");
                //statusLabel.setText("forward right button pressed");
                forward_right();
            }
        });
        backwardButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Log.d("MyFirstApp", "backwardButton Clicked");
                //statusLabel.setText("backward button pressed");
                backward();
            }
        });

        turnLeftForwardButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Log.d("MyFirstApp", "turnLeftForwardButton Clicked");
                //statusLabel.setText("turn left button pressed");
                turnLeftForward();
            }
        });

        turnRightForwardButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Log.d("MyFirstApp", "turnRightForwardButton Clicked");
                //statusLabel.setText("turn right button pressed");
                turnRightForward();
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Log.d("MyFirstApp", "stopButton Clicked");
                //statusLabel.setText("stop button pressed");
                stop();
            }
        });
        V_1Button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Log.d("MyFirstApp", "V_1Button Clicked");
                //statusLabel.setText("Velocity 1 button pressed");
                V1();
            }
        });
        V_2Button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Log.d("MyFirstApp", "V_2Button Clicked");
                //statusLabel.setText("Velocity 2 button pressed");

                V2();
            }
        });
        V_3Button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Log.d("MyFirstApp", "V_3Button Clicked");
                //statusLabel.setText("Velocity 3 button pressed");

                V3();
            }
        });
        V_4Button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Log.d("MyFirstApp", "V_4Button Clicked");
                //statusLabel.setText("Velocity 3.5 button pressed");

                V4();
            }
        });

        if (bluetooth.isEnabled()) {
            String address = bluetooth.getAddress();
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
            String name = bluetooth.getName();
            //Mostramos la datos en pantalla (Ththe screen)e information is shown in
            bluetoothActive = true;

            Toast.makeText(getApplicationContext(), "Bluetooth ENABLED:" + name + ":" + address,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth NOT enabled",
                    Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);

        }
    }

    private void showBT_IP_NAME(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        String ipAddress = Formatter.formatIpAddress(ip);

        //WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        //String ipAddress = String.valueOf(wifiInfo.getIpAddress());
        BT_IP_TextView.setText(ipAddress);
    }

    String Velocity = "1\n";
    public void V1() {
        Velocity = "1\n";//orden de posicio
    }
    public void V2() {
        Velocity = "2\n";//orden de posicio
    }
    public void V3() {
        Velocity = "3\n";//orden de posicio
    }
    public void V4() {
        Velocity = "3.5\n";//orden de posicio
    }

    public void forward() {
        try {
            String tmpStr = "w\n";//orden de posicion
            byte bytes[] = tmpStr.getBytes();
            if (outputStream != null) outputStream.write(bytes);
            if (outputStream != null) outputStream.flush();
        } catch (Exception e) {
            Log.e("forward", "ERROR:" + e);
        }
        try {

            byte bytes[] = Velocity.getBytes();
            if (outputStream != null) outputStream.write(bytes);
            if (outputStream != null) outputStream.flush();
        } catch (Exception e) {
            Log.e("forward", "ERROR:" + e);
        }
    }
    public void forward_left() {
        try {
            String tmpStr = "q\n";//orden de posicion
            byte bytes[] = tmpStr.getBytes();
            if (outputStream != null) outputStream.write(bytes);
            if (outputStream != null) outputStream.flush();
        } catch (Exception e) {
            Log.e("forward_left", "ERROR:" + e);
        }
        try {
            byte bytes[] = Velocity.getBytes();
            if (outputStream != null) outputStream.write(bytes);
            if (outputStream != null) outputStream.flush();
        } catch (Exception e) {
            Log.e("forward_left", "ERROR:" + e);
        }
    }
    public void forward_right() {
        try {
            String tmpStr = "e\n";//orden de posicion
            byte bytes[] = tmpStr.getBytes();
            if (outputStream != null) outputStream.write(bytes);
            if (outputStream != null) outputStream.flush();
        } catch (Exception e) {
            Log.e("forward_right", "ERROR:" + e);
        }
        try {
            byte bytes[] = Velocity.getBytes();
            if (outputStream != null) outputStream.write(bytes);
            if (outputStream != null) outputStream.flush();
        } catch (Exception e) {
            Log.e("forward_right", "ERROR:" + e);
        }
    }
    public void backward() {
        try {
            String tmpStr = "s\n";//orden de posicion
            byte bytes[] = tmpStr.getBytes();
            if (outputStream != null) outputStream.write(bytes);
            if (outputStream != null) outputStream.flush();
        } catch (Exception e) {
            Log.e("backward", "ERROR:" + e);
        }
        try {
            byte bytes[] = Velocity.getBytes();
            if (outputStream != null) outputStream.write(bytes);
            if (outputStream != null) outputStream.flush();
        } catch (Exception e) {
            Log.e("backward", "ERROR:" + e);
        }
    }

    public void turnLeftForward() {
        try {
            String tmpStr = "a\n";//orden de posicion
            byte bytes[] = tmpStr.getBytes();
            if (outputStream != null) outputStream.write(bytes);
            if (outputStream != null) outputStream.flush();
        } catch (Exception e) {
            Log.e("turnLeftForward", "ERROR:" + e);
        }
        try {
            byte bytes[] = Velocity.getBytes();
            if (outputStream != null) outputStream.write(bytes);
            if (outputStream != null) outputStream.flush();
        } catch (Exception e) {
            Log.e("turnLeftForward", "ERROR:" + e);
        }
    }
    public void turnRightForward() {
        try {
            String tmpStr = "d\n";//orden de posicion
            byte bytes[] = tmpStr.getBytes();
            if (outputStream != null) outputStream.write(bytes);
            if (outputStream != null) outputStream.flush();
        } catch (Exception e) {
            Log.e("turnRightForward", "ERROR:" + e);
        }
        try {
            byte bytes[] = Velocity.getBytes();
            if (outputStream != null) outputStream.write(bytes);
            if (outputStream != null) outputStream.flush();
        } catch (Exception e) {
            Log.e("turnRightForward", "ERROR:" + e);
        }
    }
    public void stop() {

        try {
            String tmpStr = "0\n";//orden de velocidad
            byte bytes[] = tmpStr.getBytes();
            if (outputStream != null) outputStream.write(bytes);
            if (outputStream != null) outputStream.flush();
        } catch (Exception e) {
            Log.e("stop", "ERROR:" + e);
        }
    }


    protected void connect(BluetoothDevice device) {
        try {
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
            btSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            Log.d("MyFirstApp", "address: "+ device.getAddress());
            bluetooth.cancelDiscovery();
            btSocket.connect();
            Log.d("MyFirstApp", "Client connected");
            inputStream = btSocket.getInputStream();
            outputStream = btSocket.getOutputStream();
        }catch (Exception e) {
            Log.e("ERROR: connect", ">>", e);

        }
    }
    protected void connectByMacAddress(String macAddress) { //p4 cuando descomenta las lineas llamara a esta funcion
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = btAdapter.getRemoteDevice(macAddress);
        if (device != null) {
            connect(device);
        } else {
            Log.e("MyFirstApp", "Device with MAC address " + macAddress + " not found.");
        }
    }

    protected void disconnect( ) {
        try {
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
            btSocket.close();
            Log.d("MyFirstApp", "Client disconnect");
            inputStream = btSocket.getInputStream();
            outputStream = btSocket.getOutputStream();
        }catch (Exception e) {
            Log.e("ERROR: connect", ">>", e);

        }
    }









    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) //Bluetooth permission request code
            if (resultCode == RESULT_OK) {
                bluetoothActive = true;
                Toast.makeText(getApplicationContext(), "User Enabled Bluetooth",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "User Did not enable Bluetooth",
                        Toast.LENGTH_SHORT).show();
            }
    }

    BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();


    private void startDiscovery() {
        if (bluetoothActive) {
            deviceList.clear();
            registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            requestBluetoothScanPermission();
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.


                return;
            }
            bluetooth.startDiscovery();
        }


    }
    private void requestBluetoothScanPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.BLUETOOTH_SCAN)) {
            // Puedes mostrar una explicación al usuario antes de solicitar los permisos nuevamente.
            // Por ejemplo, mostrar un diálogo con un mensaje explicativo.
            // Luego, puedes llamar a ActivityCompat.requestPermissions para solicitar los permisos nuevamente.
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, 1001);
        }
    }



   /* public void checkBTPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            switch (ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                case PackageManager.PERMISSION_DENIED:
                    if (ContextCompat.checkSelfPermission(getBaseContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                        this.requestPermissions(new
                                String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
                    }
                    break;
                case PackageManager.PERMISSION_GRANTED:
                    break;
            }
        }
    }*/

    private android.hardware.Camera getCameraInstance() { //p5
        android.hardware.Camera camera = null;
        try {
            camera = android.hardware.Camera.open(0);
        } catch (Exception e) {
// cannot get camera or does not exist
            Log.d("getCameraInstance", "ERROR" + e);
        }
        return camera;
    }

    private static File mediaStorageDir; //p5
    private static File mediaFile; //p5

    static File getOutputMediaFile() { //p5
        if (mediaStorageDir == null) {
            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("MyCameraApp", "failed to create directory");
                    return null;
                }
            }
        }

        if (mediaFile == null) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG.jpg");
        }

        return mediaFile;
    } //p5

    public void captureCamera(){
        if (mCamera!=null) {
            try {
                mCamera.takePicture(null, null, mPicture);
                mCamera.startPreview();
                mCameraPreview.clearFocus();
            }catch (Exception I){
                Log.d("ERROR", "camara");
            }
        }
    }


    TextView statusLabel;
    Button connectButton;
    Button disconnectButton;
    Button forwardButton;
    Button F_LeftButton;
    Button F_RightButton;
    Button backwardButton;

    Button turnLeftForwardButton;
    Button turnRightForwardButton;
    Button stopButton;

    Button V_1Button;
    Button V_2Button;
    Button V_3Button;
    Button V_4Button;


    FrameLayout cameraPreviewFrameLayout;//p5




}
