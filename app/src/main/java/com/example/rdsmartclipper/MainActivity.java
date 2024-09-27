package com.example.rdsmartclipper;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.content.Intent;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.lifecycle.ViewModelProvider;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.example.rdsmartclipper.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements BluetoothManager.OnDataReceivedListener, BluetoothManager.PermissionRequestCallback {

    private ActivityMainBinding binding;

    private BluetoothManager bluetoothManager;
    private SharedViewModel sharedViewModel;

    private TextView textVoltageCurrent;
    private TextView textTemperatureRpm;

    private final List<Entry> voltageEntries = new ArrayList<>();
    private final List<Entry> temperatureEntries = new ArrayList<>();
    private final List<Entry> rpmEntries = new ArrayList<>();
    private final List<Entry> currentEntries = new ArrayList<>();

    private String voltageText = "Voltage: N/A";
    private String currentText = "Current: N/A";
    private String temperatureText = "Temperature: N/A";
    private String rpmText = "RPM: N/A";

    private boolean isDebugMode = false;
    private boolean isFullscreenMode = true;
    private boolean lastKnownFullscreenMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefListener);

        isDebugMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("debug_mode", false);
        isFullscreenMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("fullscreen_mode", true);
        lastKnownFullscreenMode = isFullscreenMode;

        if (!isDebugMode) {
            bluetoothManager = new BluetoothManager(this);

            bluetoothManager.setOnDataReceivedListener(this);
            bluetoothManager.setPermissionRequestCallback(requestCode -> ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, requestCode));
            bluetoothManager.showDeviceListAndConnect(macAddress -> bluetoothManager.connectToDevice(macAddress));
        } else {
            readDataFromFile();
        }

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        Button voltageButton = binding.buttonVoltageChart;
        Button temperatureButton = binding.buttonTemperatureChart;
        Button rpmButton = binding.buttonRpmChart;
        Button currentButton = binding.buttonCurrentChart;

        textVoltageCurrent = binding.textVoltageCurrent;
        textTemperatureRpm = binding.textTemperatureRpm;

        voltageButton.setOnClickListener(v -> openFragment(new VoltageChartFragment(),isFullscreenMode));
        temperatureButton.setOnClickListener(v -> openFragment(new TemperatureChartFragment(),isFullscreenMode));
        rpmButton.setOnClickListener(v -> openFragment(new RPMChartFragment(),isFullscreenMode));
        currentButton.setOnClickListener(v -> openFragment(new CurrentChartFragment(),isFullscreenMode));

        observeData();

        handleBackNavigation();
    }

    private void openFragment(Fragment fragment, Boolean fullscreen) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        String fragmentTag = fullscreen ? "FULLSCREEN" : "NON_FULLSCREEN";

        Bundle args = new Bundle();
        args.putBoolean("isFullscreen", fullscreen);
        fragment.setArguments(args);

        if (fullscreen) {
            // Hide main content
            binding.mainContent.setVisibility(View.GONE);
            // Show fullscreen fragment container
            binding.fullscreenFragmentContainer.setVisibility(View.VISIBLE);

            fragmentTransaction.replace(binding.fullscreenFragmentContainer.getId(), fragment, fragmentTag);
        } else {
            fragmentTransaction.replace(binding.fragmentContainer.getId(), fragment, fragmentTag);
        }

        fragmentTransaction.addToBackStack(fragmentTag);
        fragmentTransaction.commit();

        updateToolbarNavigationIcon();
    }


    private final SharedPreferences.OnSharedPreferenceChangeListener prefListener = (sharedPreferences, key) -> {
        if ("debug_mode".equals(key)) {
            // Update mode
            isDebugMode = sharedPreferences.getBoolean("debug_mode", false);
            // Handle mode change
            handleModeChange();
        } else if ("fullscreen_mode".equals(key)) {
            // Update fullscreen mode
            isFullscreenMode = sharedPreferences.getBoolean("fullscreen_mode", true);
            Toast.makeText(this, "Fullscreen mode will apply the next time you open a chart.", Toast.LENGTH_SHORT).show();
        }
    };

    private void observeData() {
        sharedViewModel.getVoltageEntries().observe(this, entries -> {
            if (entries != null && !entries.isEmpty()) {
                Entry latestEntry = entries.get(entries.size() - 1);
                float voltage = latestEntry.getY();
                updateVoltageText(voltage);
            } else {
                voltageText = "Voltage: N/A";
                textVoltageCurrent.setText(voltageText + "   " + currentText);
            }
        });

        sharedViewModel.getCurrentEntries().observe(this, entries -> {
            if (entries != null && !entries.isEmpty()) {
                Entry latestEntry = entries.get(entries.size() - 1);
                float current = latestEntry.getY();
                updateCurrentText(current);
            } else {
                currentText = "Current: N/A";
                textVoltageCurrent.setText(voltageText + "   " + currentText);
            }
        });


        sharedViewModel.getTemperatureEntries().observe(this, entries -> {
            if (entries != null && !entries.isEmpty()) {
                Entry latestEntry = entries.get(entries.size() - 1);
                float temperature = latestEntry.getY();
                updateTemperatureText(temperature);
            } else {
                temperatureText = "Temperature: N/A";
                textTemperatureRpm.setText(temperatureText + "   " + rpmText);
            }
        });

        sharedViewModel.getRPMEntries().observe(this, entries -> {
            if (entries != null && !entries.isEmpty()) {
                Entry latestEntry = entries.get(entries.size() - 1);
                float rpm = latestEntry.getY();
                updateRPMText(rpm);
            } else {
                rpmText = "Temperature: N/A";
                textTemperatureRpm.setText(temperatureText + "   " + rpmText);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateVoltageText(float voltage) {
        voltageText = String.format(Locale.getDefault(), "Voltage: %.2f V", voltage);
        textVoltageCurrent.setText(voltageText + "   " + currentText);
    }

    @SuppressLint("SetTextI18n")
    private void updateCurrentText(float current) {
        currentText = String.format(Locale.getDefault(), "Current: %.2f A", current);
        textVoltageCurrent.setText(voltageText + "   " + currentText);
    }

    @SuppressLint("SetTextI18n")
    private void updateTemperatureText(float temperature) {
        temperatureText = String.format(Locale.getDefault(), "Temperature: %.2f°C", temperature);
        textTemperatureRpm.setText(temperatureText + "   " + rpmText);
    }

    @SuppressLint("SetTextI18n")
    private void updateRPMText(float rpm) {
        rpmText = String.format(Locale.getDefault(), "RPM: %.0f", rpm);
        textTemperatureRpm.setText(temperatureText + "   " + rpmText);
    }


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

        // Reset local variables
        voltageText = "Voltage: N/A";
        currentText = "Current: N/A";
        temperatureText = "Temperature: N/A";
        rpmText = "RPM: N/A";

        // Update TextViews
        textVoltageCurrent.setText(voltageText + "   " + currentText);
        textTemperatureRpm.setText(temperatureText + "   " + rpmText);
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
                    // Simulate real-time data by adding a delay
                    Thread.sleep(1000); // Adjust delay as needed
                    parseCSVData(line);
                }
            } catch (IOException | InterruptedException e) {
                Log.e("MainActivity", "Failed to read data file.", e);
                runOnUiThread(() -> Toast.makeText(this, "Failed to read data file.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void parseCSVData(String data) {
        CSVParser csvParser = new CSVParser();
        List<DataPoint> dataPoints = csvParser.parseCSV(data);

        for (DataPoint point : dataPoints) {
            Entry voltageEntry = new Entry(point.time, point.voltage);
            Entry temperatureEntry = new Entry(point.time, point.temperature);
            Entry rpmEntry = new Entry(point.time, point.rpm);
            Entry currentEntry = new Entry(point.time, point.current);

            // Update ViewModel on the main thread
            runOnUiThread(() -> {
                sharedViewModel.addVoltageEntry(voltageEntry);
                sharedViewModel.addTemperatureEntry(temperatureEntry);
                sharedViewModel.addRPMEntry(rpmEntry);
                sharedViewModel.addCurrentEntry(currentEntry);
            });
        }
    }

    private void updateToolbarNavigationIcon() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(backStackEntryCount > 0);
        }
    }

    private void handleBackNavigation() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fragmentManager = getSupportFragmentManager();

                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();

                    // Add a BackStackChangedListener
                    fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                        @Override
                        public void onBackStackChanged() {
                            // Remove the listener to prevent it from being called multiple times
                            fragmentManager.removeOnBackStackChangedListener(this);

                            // Update the toolbar navigation icon
                            updateToolbarNavigationIcon();

                            if (fragmentManager.getBackStackEntryCount() == 0) {
                                // Show main content
                                binding.mainContent.setVisibility(View.VISIBLE);
                                // Hide fullscreen fragment container
                                binding.fullscreenFragmentContainer.setVisibility(View.GONE);

                                // Reset toolbar
                                if (getSupportActionBar() != null) {
                                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                                    getSupportActionBar().setTitle("SmartClip");
                                }

                                // Remove navigation click listener
                                Toolbar toolbar = binding.toolbar;
                                toolbar.setNavigationOnClickListener(null);
                            } else {
                                // Get the last fragment in the back stack
                                FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1);
                                String tag = backStackEntry.getName();

                                if ("FULLSCREEN".equals(tag)) {
                                    // Show fullscreen container, hide main content
                                    binding.mainContent.setVisibility(View.GONE);
                                    binding.fullscreenFragmentContainer.setVisibility(View.VISIBLE);
                                } else if ("NON_FULLSCREEN".equals(tag)) {
                                    // Show main content, hide fullscreen container
                                    binding.mainContent.setVisibility(View.VISIBLE);
                                    binding.fullscreenFragmentContainer.setVisibility(View.GONE);
                                }
                            }
                        }
                    });
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
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
        new Thread(() -> parseCSVData(data)).start();
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

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean currentFullscreenMode = sharedPrefs.getBoolean("fullscreen_mode", true);

        if (currentFullscreenMode != lastKnownFullscreenMode) {
            // The fullscreen mode setting has changed
            lastKnownFullscreenMode = currentFullscreenMode;
            isFullscreenMode = currentFullscreenMode;

            // Clear the fragment back stack
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            // Reset UI
            binding.mainContent.setVisibility(View.VISIBLE);
            binding.fullscreenFragmentContainer.setVisibility(View.GONE);
        }
    }

}