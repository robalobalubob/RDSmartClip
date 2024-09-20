package com.example.rdsmartclipper;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        connectToDevice();

        voltageChart = findViewById(R.id.voltage_chart);
        temperatureChart = findViewById(R.id.temperature_chart);
        rpmChart = findViewById(R.id.rpm_chart);
        currentChart = findViewById(R.id.current_chart);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        // Using findViewById because NavigationView exists in different layout files
        // between w600dp and w1240dp
        NavigationView navView = findViewById(R.id.nav_view);
        if (navView == null) {
            // The navigation drawer already has the items including the items in the overflow menu
            // We only inflate the overflow menu if the navigation drawer isn't visible
            getMenuInflater().inflate(R.menu.overflow, menu);
        }
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_settings) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_settings);
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