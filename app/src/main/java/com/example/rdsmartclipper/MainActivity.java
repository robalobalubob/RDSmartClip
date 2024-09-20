package com.example.rdsmartclipper;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

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

    private LineChart voltageChart, temperatureChart, rpmChart, currentChart;
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


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());


        voltageChart = findViewById(R.id.voltage_chart);
        temperatureChart = findViewById(R.id.temperature_chart);
        rpmChart = findViewById(R.id.rpm_chart);
        currentChart = findViewById(R.id.current_chart);
        if (!isDebugMode) {
            bluetoothManager.showDeviceListAndConnect(macAddress -> bluetoothManager.connectToDevice(macAddress));
        } else {
            readDataFromFile();
        }

    }

    private final SharedPreferences.OnSharedPreferenceChangeListener prefListener =
            (sharedPreferences, key) -> {
                assert key != null;
                if (key.equals("debug_mode")) {
                    updateChartsBasedOnDebugMode();
                }
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    private void readDataFromFile() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.data);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                parseCSVData(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        updateCharts();
    }

    private void updateChartsBasedOnDebugMode() {
        isDebugMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("debug_mode", false);

        voltageEntries.clear();
        temperatureEntries.clear();
        rpmEntries.clear();
        currentEntries.clear();

        if (isDebugMode) {
            readDataFromFile();
        } else {
            bluetoothManager.showDeviceListAndConnect(macAddress -> bluetoothManager.connectToDevice(macAddress));
            // Make sure to clear existing data and stop any ongoing Bluetooth data reading
        }

        updateCharts(); // Update charts with new data
    }

    private void updateCharts() {
        LineDataSet voltageDataSet = new LineDataSet(voltageEntries, "Voltage");
        LineDataSet temperatureDataSet = new LineDataSet(temperatureEntries, "Temperature");
        LineDataSet rpmDataSet = new LineDataSet(rpmEntries, "RPM");
        LineDataSet currentDataSet = new LineDataSet(currentEntries, "Current");

        voltageChart.setData(new LineData(voltageDataSet));
        temperatureChart.setData(new LineData(temperatureDataSet));
        rpmChart.setData(new LineData(rpmDataSet));
        currentChart.setData(new LineData(currentDataSet));

        voltageChart.invalidate(); // Refresh the chart
        temperatureChart.invalidate();
        rpmChart.invalidate();
        currentChart.invalidate();
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