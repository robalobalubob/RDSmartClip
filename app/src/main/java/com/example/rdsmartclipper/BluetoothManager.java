package com.example.rdsmartclipper;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import java.security.Permission;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class BluetoothManager {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private Handler handler = new Handler();
    private InputStream inputStream;
    private Context context;
    private OnDataReceivedListener onDataReceivedListener;

    private final UUID MY_UUID = UUID.fromString("00000000-0000-1000-8000-00805F9B34FB");

    private PermissionRequestCallback permissionRequestCallback;

    public BluetoothManager(Context context) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
        }
    }

    public void connectToDevice(String macAddress) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if(macAddress == null ||macAddress.isEmpty()){
            Toast.makeText(context, "Invalid MAC address", Toast.LENGTH_SHORT).show();
            return;
        }
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            inputStream = bluetoothSocket.getInputStream();
            listenForData();
        }  catch (Exception e) {
            Log.e("Bluetooth", "Error connecting to device: " + e.getMessage());
            Toast.makeText(context, "Unable to connect to device", Toast.LENGTH_SHORT).show();
        }

    }

    private void listenForData() {
        final byte[] buffer = new byte[1024];
        final int[] bytes = new int[1];
        AtomicBoolean isRunning = new AtomicBoolean(true);

        Thread thread = new Thread(() -> {
            while (isRunning.get()) {
                try {
                    bytes[0] = inputStream.read(buffer);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String data = new String(buffer, 0, bytes[0]);
                            if (onDataReceivedListener != null) {
                                onDataReceivedListener.onDataReceived(data);
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.e("Bluetooth", "Error reading data: " + e.getMessage());
                    Toast.makeText(context, "Error reading data", Toast.LENGTH_SHORT).show();
                    isRunning.set(false);
                    try {
                        if (bluetoothSocket != null) {
                            bluetoothSocket.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException closeException) {
                        Log.e("Bluetooth", "Error closing resources: " + closeException.getMessage());
                    }
                }
                if (Thread.interrupted()) {
                    isRunning.set(false);
                    try {
                        if (bluetoothSocket != null) {
                            bluetoothSocket.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException closeException) {
                        Log.e("Bluetooth", "Error closing resources: " + closeException.getMessage());
                    }
                }
            }
        });
        thread.start();
    }

    public void showDeviceListAndConnect(DeviceSelectionCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            if (permissionRequestCallback != null) {
                permissionRequestCallback.requestBluetoothConnectPermission(1);
            }
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        List<String> deviceNames = new ArrayList<>();
        if (!pairedDevices.isEmpty()) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                deviceNames.add(deviceName + " - " + deviceHardwareAddress);
            }
        } else {
            Toast.makeText(context, "No paired devices found", Toast.LENGTH_SHORT).show();
            Log.e("Bluetooth", "No paired devices found");
            return; // Or handle the case with no paired devices
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select a device");
        builder.setItems(deviceNames.toArray(new String[0]), (dialog, which) -> {
            String selectedDevice = deviceNames.get(which);
            String macAddress = selectedDevice.substring(selectedDevice.length() - 17);
            callback.onDeviceSelected(macAddress);
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public interface OnDataReceivedListener {
        void onDataReceived(String data);
    }

    public interface DeviceSelectionCallback {
        void onDeviceSelected(String macAddress);
    }

    public interface PermissionRequestCallback {
        void requestBluetoothConnectPermission(int requestCode);
    }

    public void setOnDataReceivedListener(OnDataReceivedListener listener) {
        this.onDataReceivedListener = listener;
    }

    public void setPermissionRequestCallback(PermissionRequestCallback callback) {
        this.permissionRequestCallback = callback;
    }
}
