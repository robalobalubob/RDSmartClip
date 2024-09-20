package com.example.rdsmartclipper;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import android.content.Intent;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
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
import java.util.Set;
import java.util.UUID;

import com.example.rdsmartclipper.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private Handler handler = new Handler();

    private final UUID MY_UUID = UUID.fromString("00000000-0000-1000-8000-00805F9B34FB");
    private LineChart voltageChart, temperatureChart, rpmChart, currentChart;
    private List<Entry> voltageEntries = new ArrayList<>();
    private List<Entry> temperatureEntries = new ArrayList<>();
    private List<Entry> rpmEntries = new ArrayList<>();
    private List<Entry> currentEntries = new ArrayList<>();

    private boolean isDebugMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefListener);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!bluetoothAdapter.isEnabled()) {
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
            bluetoothAdapter.enable();
        }
        isDebugMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("debug_mode", false);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());


        voltageChart = findViewById(R.id.voltage_chart);
        temperatureChart = findViewById(R.id.temperature_chart);
        rpmChart = findViewById(R.id.rpm_chart);
        currentChart = findViewById(R.id.current_chart);
        if (!isDebugMode) {
            connectToDevice();
        } else {
            readDataFromFile();
        }

    }

    private SharedPreferences.OnSharedPreferenceChangeListener prefListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    assert key != null;
                    if (key.equals("debug_mode")) {
                        updateChartsBasedOnDebugMode();
                    }
                }
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }
    private void connectToDevice() {
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
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals("Name")) {
                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    bluetoothSocket.connect();
                    inputStream = bluetoothSocket.getInputStream();
                    listenForData();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
    }

    private void listenForData() {
        if(isDebugMode) return;
        final byte[] buffer = new byte[1024];
        final int[] bytes = new int[1];

        Thread thread = new Thread(() -> {
            while(true) {
                try {
                    bytes[0] = inputStream.read(buffer);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String data = new String(buffer, 0, bytes[0]);
                            parseCSVData(data);
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
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
            connectToDevice();
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
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}