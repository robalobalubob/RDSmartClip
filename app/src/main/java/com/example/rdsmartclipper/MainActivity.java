package com.example.rdsmartclipper;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.lifecycle.ViewModelProvider;

import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.example.rdsmartclipper.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements BluetoothManager.OnDataReceivedListener, BluetoothManager.PermissionRequestCallback {


    private BluetoothManager bluetoothManager;
    private SharedViewModel sharedViewModel;

    private final List<Entry> voltageEntries = new ArrayList<>();
    private final List<Entry> temperatureEntries = new ArrayList<>();
    private final List<Entry> rpmEntries = new ArrayList<>();
    private final List<Entry> currentEntries = new ArrayList<>();

    private boolean isDebugMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefListener);

        isDebugMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("debug_mode", false);

        bluetoothManager = new BluetoothManager(this);

        bluetoothManager.setOnDataReceivedListener(this);
        bluetoothManager.setPermissionRequestCallback(requestCode -> ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, requestCode));

        if (!isDebugMode) {
            bluetoothManager.showDeviceListAndConnect(macAddress -> bluetoothManager.connectToDevice(macAddress));
        } else {
            readDataFromFile();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        Button voltageButton = findViewById(R.id.button_voltage_chart);
        Button temperatureButton = findViewById(R.id.button_temperature_chart);
        Button rpmButton = findViewById(R.id.button_rpm_chart);
        Button currentButton = findViewById(R.id.button_current_chart);
        voltageButton.setOnClickListener(v -> openFragment(new VoltageChartFragment()));
        temperatureButton.setOnClickListener(v -> openFragment(new TemperatureChartFragment()));
        rpmButton.setOnClickListener(v -> openFragment(new RPMChartFragment()));
        currentButton.setOnClickListener(v -> openFragment(new CurrentChartFragment()));

    }

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container,fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    private final SharedPreferences.OnSharedPreferenceChangeListener prefListener = (sharedPreferences, key) -> {
        if ("debug_mode".equals(key)) {
            // Update mode
            isDebugMode = sharedPreferences.getBoolean("debug_mode", false);
            // Handle mode change
            handleModeChange();
        }
    };

    private void handleModeChange() {
        // Clear existing data
        clearData();

        if (isDebugMode) {
            // Disconnect Bluetooth if connected
            // Read data from file
            readDataFromFile();
        } else {
            // Initialize Bluetooth manager if not already done
            if (bluetoothManager == null) {
                bluetoothManager = new BluetoothManager(this);
                bluetoothManager.setOnDataReceivedListener(this);
                bluetoothManager.setPermissionRequestCallback(requestCode -> ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, requestCode));
            }
            // Start Bluetooth connection
            bluetoothManager.showDeviceListAndConnect(macAddress -> bluetoothManager.connectToDevice(macAddress));
        }
    }

    private void clearData() {
        // Clear data in ViewModel
        sharedViewModel.clearAllData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    private void readDataFromFile() {
        new Thread(() -> {
            try {
                InputStream inputStream = getResources().openRawResource(R.raw.data);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    parseCSVData(line);
                }
            } catch (IOException e) {
                Log.e("MainActivity", "Failed to read data file.", e);
                runOnUiThread(() -> Toast.makeText(this, "Failed to read data file.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void parseCSVData(String data) {
        CSVParser csvParser = new CSVParser();
        List<DataPoint> dataPoints = csvParser.parseCSV(data);

        for (DataPoint point : dataPoints) {
            voltageEntries.add(new Entry(point.time, point.voltage));
            temperatureEntries.add(new Entry(point.time, point.temperature));
            rpmEntries.add(new Entry(point.time, point.rpm));
            currentEntries.add(new Entry(point.time, point.current));
        }

        runOnUiThread(() -> {
            sharedViewModel.addVoltageEntries(voltageEntries);
            sharedViewModel.addTemperatureEntries(temperatureEntries);
            sharedViewModel.addRPMEntries(rpmEntries);
            sharedViewModel.addCurrentEntries(currentEntries);
        });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDataReceived(String data) {
        runOnUiThread(() -> parseCSVData(data));
    }

    @Override
    public void requestBluetoothConnectPermission(int requestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bluetoothManager.showDeviceListAndConnect(macAddress -> bluetoothManager.connectToDevice(macAddress));
            } else {
                Toast.makeText(this, "Bluetooth connect permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}