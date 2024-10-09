package com.example.rdsmartclipper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BluetoothService extends Service {

    private static final String TAG = "BluetoothService";
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final IBinder binder = new LocalBinder();
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private BluetoothDataCallback bluetoothDataCallback;
    private ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Bluetooth adapter
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void registerCallback(BluetoothDataCallback callback) {
        this.bluetoothDataCallback = callback;
    }

    public List<String> getPairedDevices() {
        List<String> devices = new ArrayList<>();

        // Check permissions
        if (checkBluetoothPermissions()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return devices;
            }
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                devices.add(device.getName() + " - " + device.getAddress());
            }
        } else {
            Log.e(TAG, "Bluetooth permissions are not granted");
            // Optionally, notify the activity via callback or through an Intent
        }

        return devices;
    }

    @SuppressLint("MissingPermission")
    public void connectToDevice(String macAddress) {
        // Ensure Bluetooth permissions are granted
        if (!checkBluetoothPermissions()) {
            Log.e(TAG, "Bluetooth permissions are not granted");
            return;
        }

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect(); // Blocking call to connect
            inputStream = bluetoothSocket.getInputStream();
            listenForData();
        } catch (IOException e) {
            Log.e(TAG, "Failed to connect to device", e);
            closeResources(); // Cleanup resources if connection fails
        }
    }

    private void listenForData() {
        executorService.execute(() -> {
            try {
                byte[] buffer = new byte[1024];
                int bytes;

                while (!Thread.currentThread().isInterrupted() && inputStream != null) {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        String data = new String(buffer, 0, bytes);
                        // Notify callback
                        if (bluetoothDataCallback != null) {
                            bluetoothDataCallback.onBluetoothDataReceived(data);
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error reading Bluetooth data", e);
            } finally {
                closeResources(); // Ensure resources are cleaned up
            }
        });
    }

    private boolean checkBluetoothPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
    }

    private void closeResources() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing Bluetooth resources", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeResources();
        executorService.shutdown();
    }

    public interface BluetoothDataCallback {
        void onBluetoothDataReceived(String data);
    }

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }
}
