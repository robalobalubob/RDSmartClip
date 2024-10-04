package com.example.rdsmartclipper;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * BluetoothManager class
 * Handles listening for data over Bluetooth.
 * Handles creating the bluetooth connection
 */
public class myBluetoothManager {
    //Establish necessary objects
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private InputStream inputStream;
    private final Context context;
    private OnDataReceivedListener onDataReceivedListener;
    //Establish UUID
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private PermissionRequestCallback permissionRequestCallback;

    /**
     * Constructor for BluetoothManager
     * @param context Context of the application
     */
    public myBluetoothManager(Context context) {
        // Initialize the BluetoothAdapter
        this.context = context;
        BluetoothManager BluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = BluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Connects to a device over Bluetooth
     * @param macAddress MAC address of the device
     */
    public void connectToDevice(String macAddress) {
        // Check for Bluetooth connect permission
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            if (permissionRequestCallback != null) {
                permissionRequestCallback.requestBluetoothConnectPermission(1);
            }
            return;
        }
        // Check valid MAC address
        if(macAddress == null ||macAddress.isEmpty()){
            Toast.makeText(context, "Invalid MAC address", Toast.LENGTH_SHORT).show();
            return;
        }
        // Get the remote device
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
        // Attempt to connect
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

    /**
     * Listens for data over Bluetooth
     */
    private void listenForData() {
        // Create a new thread to listen for data
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AtomicBoolean isRunning = new AtomicBoolean(true);

        executor.submit(() -> {
            try {
                // Read data from the input stream
                byte[] buffer = new byte[1024];
                int bytes;

                while (isRunning.get() && !Thread.currentThread().isInterrupted()) {
                    bytes = inputStream.read(buffer);
                    String data = new String(buffer, 0, bytes);

                    handler.post(() -> {
                        if (onDataReceivedListener != null) {
                            onDataReceivedListener.onDataReceived(data);
                        }
                    });
                }
            } catch (IOException e) {
                Log.e("Bluetooth", "Error reading data: " + e.getMessage());
            } finally {
                closeResources();
            }
        });
    }

    /**
     * Shows a dialog to select a device from a list of paired devices
     * @param callback Callback to handle the selected device
     */
    public void showDeviceListAndConnect(DeviceSelectionCallback callback) {
        // Check for Bluetooth connect permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            if (permissionRequestCallback != null) {
                permissionRequestCallback.requestBluetoothConnectPermission(1);
            }
            return;
        }
        // Get a list of paired devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        List<String> deviceNames = new ArrayList<>();
        // Add paired devices to the list
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

        // Show a dialog to select a device
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

    /**
     * Interface for handling data received over Bluetooth
     */
    public interface OnDataReceivedListener {
        void onDataReceived(String data);
    }

    /**
     * Interface for handling device selection
     */
    public interface DeviceSelectionCallback {
        void onDeviceSelected(String macAddress);
    }

    /**
     * Interface for handling permission requests
     */
    public interface PermissionRequestCallback {
        void requestBluetoothConnectPermission(int requestCode);
    }

    /**
     * Sets the listener for data received over Bluetooth
     * @param listener Listener to set
     */
    public void setOnDataReceivedListener(OnDataReceivedListener listener) {
        this.onDataReceivedListener = listener;
    }

    /**
     * Sets the callback for permission requests
     * @param callback Callback to set
     */
    public void setPermissionRequestCallback(PermissionRequestCallback callback) {
        this.permissionRequestCallback = callback;
    }

    /**
     * Closes the input stream and the Bluetooth socket
     */
    private void closeResources() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            Log.e("Bluetooth", "Error closing resources: " + e.getMessage());
        }
    }
}
