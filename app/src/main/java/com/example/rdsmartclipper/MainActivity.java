package com.example.rdsmartclipper;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.content.Context;
import android.os.IBinder;
import android.text.InputType;
import android.util.Log;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.lifecycle.ViewModelProvider;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

import com.example.rdsmartclipper.databinding.ActivityMainBinding;

/**
 * MainActivity
 * Creates and initializes the main view of the application.
 */
public class MainActivity extends AppCompatActivity implements BluetoothService.BluetoothDataCallback {

    private ActivityMainBinding binding;

    // Replace myBluetoothManager with BluetoothService
    private BluetoothService bluetoothService;
    private boolean isServiceBound = false;
    private SharedViewModel sharedViewModel;

    private TextView textVoltage;
    private TextView textCurrent;
    private TextView textTemperature;
    private TextView textRPM;

    // Default text, updated with new data
    private String voltageText = "Voltage: N/A";
    private String currentText = "Current: N/A";
    private String temperatureText = "Temperature: N/A";
    private String rpmText = "RPM: N/A";

    // Key strings
    private static final String VOLTAGE = "Voltage";
    private static final String TEMPERATURE = "Temperature";
    private static final String RPM = "RPM";
    private static final String CURRENT = "Current";
    private static final String DEBUG_MODE_KEY = "debug_mode";
    private static final String FULLSCREEN_MODE_KEY = "fullscreen_mode";

    // Mode checks
    private boolean isDebugMode = false;
    private boolean isFullscreenMode = true;
    private boolean lastKnownFullscreenMode = true;

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            bluetoothService = binder.getService();
            isServiceBound = true;

            // Register the callback to receive Bluetooth data
            bluetoothService.registerCallback(MainActivity.this);

            // Show device list and connect
            showDeviceListAndConnect();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
            bluetoothService = null;
        }
    };

    /**
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up preferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefListener);

        // Establish modes
        isDebugMode = sharedPrefs.getBoolean(DEBUG_MODE_KEY, false);
        isFullscreenMode = sharedPrefs.getBoolean(FULLSCREEN_MODE_KEY, true);
        lastKnownFullscreenMode = isFullscreenMode;

        // Set up toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        // Create text elements
        textVoltage = binding.textVoltage;
        textCurrent = binding.textCurrent;
        textTemperature = binding.textTemperature;
        textRPM = binding.textRpm;

        // Call set up buttons
        setupButtons();
        // Observe data for changes
        observeData();
        // Prep for back navigation
        handleBackNavigation();

        // Check permissions and start Bluetooth or read from file
        if (!isDebugMode) {
            Log.d("MainActivity", "App is in normal mode, checking permissions");
            checkAndRequestBluetoothPermissions();
        } else {
            Log.d("MainActivity", "App is in debug mode, reading data from file");
            readDataFromFile();
        }
    }

    private void checkAndRequestBluetoothPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN
                    },
                    REQUEST_BLUETOOTH_PERMISSIONS);
        } else {
            startAndBindBluetoothService();
        }
    }


    private void startAndBindBluetoothService() {
        Intent intent = new Intent(this, BluetoothService.class);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void showDeviceListAndConnect() {
        if (isServiceBound && bluetoothService != null) {
            List<String> deviceList = bluetoothService.getPairedDevices();
            if (deviceList.isEmpty()) {
                Toast.makeText(this, "No paired Bluetooth devices found.", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Bluetooth Device");
            builder.setItems(deviceList.toArray(new String[0]), (dialog, which) -> {
                String deviceInfo = deviceList.get(which);
                String macAddress = deviceInfo.substring(deviceInfo.length() - 17);
                bluetoothService.connectToDevice(macAddress);
            });
            builder.show();
        }
    }

    /**
     * Handles the result of permission requests.
     * @param requestCode The request code passed
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            boolean permissionsGranted = true;
            for (int result : grantResults) {
                permissionsGranted = permissionsGranted && (result == PackageManager.PERMISSION_GRANTED);
            }

            if (permissionsGranted) {
                startAndBindBluetoothService();
            } else {
                Toast.makeText(this, "Bluetooth permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Opens a fragment chart in either fullscreen or non-fullscreen mode.
     * @param fragment Fragment to be opened, one of the chart options
     * @param fullscreen Whether or not the fragment is opened in fullscreen
     */
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

    /**
     * Displays the dialog for setting the Y-limit for a chart.
     * @param chartType String that represents the type of chart
     */
    private void showYLimitDialog(String chartType) {
        // Create an AlertDialog for input
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Y-Limit for " + chartType + " Chart");

        // Set up the input field
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String userInput = input.getText().toString();
            if (!userInput.isEmpty()) {
                float yLimit = Float.parseFloat(userInput);
                applyYLimit(chartType, yLimit);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Applies the Y-limit to a chart.
     * @param chartType Type of chart to apply the limit to
     * @param yLimit The y-limit value to apply
     */
    private void applyYLimit(String chartType, float yLimit) {
        switch (chartType) {
            case VOLTAGE:
                sharedViewModel.setVoltageYLimit(yLimit);
                break;
            case TEMPERATURE:
                sharedViewModel.setTemperatureYLimit(yLimit);
                break;
            case RPM:
                sharedViewModel.setRPMYLimit(yLimit);
                break;
            case CURRENT:
                sharedViewModel.setCurrentYLimit(yLimit);
                break;
        }
    }

    /**
     * Handles when preferences change
     */
    private final SharedPreferences.OnSharedPreferenceChangeListener prefListener = (sharedPreferences, key) -> {
        if (DEBUG_MODE_KEY.equals(key)) {
            // Update mode
            isDebugMode = sharedPreferences.getBoolean(DEBUG_MODE_KEY, false);
            // Handle mode change
            handleModeChange();
        } else if (FULLSCREEN_MODE_KEY.equals(key)) {
            // Update fullscreen mode
            isFullscreenMode = sharedPreferences.getBoolean(FULLSCREEN_MODE_KEY, true);
            Toast.makeText(this, "Fullscreen mode will apply the next time you open a chart.", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * Updates the text elements with the latest data from the ViewModel
     */
    private void observeData() {
        // Observer for Voltage
        sharedViewModel.getVoltageEntries().observe(this, entries -> {
            if (entries != null && !entries.isEmpty()) {
                Entry latestEntry = entries.get(entries.size() - 1);
                float voltage = latestEntry.getY();
                updateVoltageText(voltage);
            } else {
                voltageText = "Voltage: N/A";
                textVoltage.setText(voltageText);
            }
        });

        // Observer for Current
        sharedViewModel.getCurrentEntries().observe(this, entries -> {
            if (entries != null && !entries.isEmpty()) {
                Entry latestEntry = entries.get(entries.size() - 1);
                float current = latestEntry.getY();
                updateCurrentText(current);
            } else {
                currentText = "Current: N/A";
                textCurrent.setText(currentText);
            }
        });

        // Observer for Temperature
        sharedViewModel.getTemperatureEntries().observe(this, entries -> {
            if (entries != null && !entries.isEmpty()) {
                Entry latestEntry = entries.get(entries.size() - 1);
                float temperature = latestEntry.getY();
                updateTemperatureText(temperature);
            } else {
                temperatureText = "Temperature: N/A";
                textTemperature.setText(temperatureText);
            }
        });

        // Observer for RPM
        sharedViewModel.getRPMEntries().observe(this, entries -> {
            if (entries != null && !entries.isEmpty()) {
                Entry latestEntry = entries.get(entries.size() - 1);
                float rpm = latestEntry.getY();
                updateRPMText(rpm);
            } else {
                rpmText = "RPM: N/A";
                textRPM.setText(rpmText);
            }
        });
    }

    /**
     * Updates the text element with the latest voltage data.
     * @param voltage Voltage to update text to
     */
    private void updateVoltageText(float voltage) {
        voltageText = String.format(Locale.getDefault(), "Voltage: %.2f V", voltage);
        textVoltage.setText(voltageText);
    }

    /**
     * Updates the text element with the latest current data.
     * @param current Current to update text to
     */
    private void updateCurrentText(float current) {
        currentText = String.format(Locale.getDefault(), "Current: %.2f A", current);
        textCurrent.setText(currentText);
    }

    /**
     * Updates the text element with the latest temperature data.
     * @param temperature Temperature to update text to
     */
    private void updateTemperatureText(float temperature) {
        temperatureText = String.format(Locale.getDefault(), "Temperature: %.2f°C", temperature);
        textTemperature.setText(temperatureText);
    }

    /**
     * Updates the text element with the latest RPM data.
     * @param rpm RPM to update text to
     */
    private void updateRPMText(float rpm) {
        rpmText = String.format(Locale.getDefault(), "RPM: %.0f", rpm);
        textRPM.setText(rpmText);
    }

    /**
     * Handles when Debug mode is toggled on and off.
     */
    private void handleModeChange() {
        // Clear existing data
        clearData();

        if (isDebugMode) {
            // Stop Bluetooth service if running
            if (isServiceBound) {
                unbindService(serviceConnection);
                isServiceBound = false;
            }
            stopService(new Intent(this, BluetoothService.class));

            // Read data from file
            readDataFromFile();
        } else {
            // Check permissions and start Bluetooth
            checkAndRequestBluetoothPermissions();
        }
    }

    /**
     * Clears all data in the ViewModel and resets local variables.
     */
    private void clearData() {
        // Clear data in ViewModel
        sharedViewModel.clearAllData();

        // Reset local variables
        voltageText = "Voltage: N/A";
        currentText = "Current: N/A";
        temperatureText = "Temperature: N/A";
        rpmText = "RPM: N/A";

        // Update TextViews
        textVoltage.setText(voltageText);
        textCurrent.setText(currentText);
        textTemperature.setText(temperatureText);
        textRPM.setText(rpmText);
    }

    /**
     * Reads data from file and parses it.
     * Only used in debug mode
     */
    private void readDataFromFile() {
        new Thread(() -> {
            try {
                InputStream inputStream = getResources().openRawResource(R.raw.data);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Simulate real-time data by adding a delay
                    parseCSVData(line);
                }
            } catch (IOException e) {
                Log.e("MainActivity", "Failed to read data file.", e);
                runOnUiThread(() -> Toast.makeText(this, "Failed to read data file.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /**
     * Parses the CSV data and adds it to the ViewModel.
     * @param data Data to be added to the ViewModel
     */
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

                // Check if rotation data is available
                if (point.roll != 0 || point.pitch != 0 || point.yaw != 0) {
                    sharedViewModel.setRotationData(point.roll, point.pitch, point.yaw);
                }
            });
        }
    }


    /**
     * Updates the toolbar navigation icon based on the back stack.
     */
    private void updateToolbarNavigationIcon() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(backStackEntryCount > 0);
        }
    }

    /**
     * Handles when the back button is pressed.
     */
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

    /**
     * Called when data is received from the BluetoothService.
     * @param data Data received from the BluetoothService
     */
    @Override
    public void onBluetoothDataReceived(String data) {
        new Thread(() -> parseCSVData(data)).start();
    }

    /**
     * Called when the activity is resumed.
     * Handles whether or not the fullscreen mode should be applied.
     */
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean currentFullscreenMode = sharedPrefs.getBoolean(FULLSCREEN_MODE_KEY, true);

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

    /**
     * Sets up a button to be clicked on to open a chart.
     * @param chartButton Button to be clicked on
     * @param fragment Fragment to be opened
     */
    private void setupChartButton(Button chartButton, Fragment fragment) {
        chartButton.setOnClickListener(v -> openFragment(fragment, isFullscreenMode));
    }

    /**
     * Sets up a button to be clicked on to set the Y-limit for a chart.
     * @param limitButton Button to be clicked on
     * @param chartType Type of chart to set the Y-limit for
     */
    private void setupLimitButton(Button limitButton, String chartType) {
        limitButton.setOnClickListener(v -> showYLimitDialog(chartType));
    }

    /**
     * Called to set up all buttons.
     */
    private void setupButtons() {
        setupChartButton(binding.buttonVoltageChart, new VoltageChartFragment());
        setupChartButton(binding.buttonTemperatureChart, new TemperatureChartFragment());
        setupChartButton(binding.buttonRpmChart, new RPMChartFragment());
        setupChartButton(binding.buttonCurrentChart, new CurrentChartFragment());

        setupLimitButton(binding.buttonVoltageLimit, VOLTAGE);
        setupLimitButton(binding.buttonTemperatureLimit, TEMPERATURE);
        setupLimitButton(binding.buttonRpmLimit, RPM);
        setupLimitButton(binding.buttonCurrentLimit, CURRENT);
    }

    /**
     * Displays options menu
     * @param menu The options menu in which you place your items.
     *
     * @return True if the menu is displayed. Always true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu with the settings item
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    /**
     * Called when an item in the options menu is selected.
     * @param item The menu item that was selected.
     *
     * @return True
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Navigate to the Settings activity
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Show the device selection dialog to the user.
     */
    private void showDeviceSelectionDialog() {
        if (!isServiceBound) {
            Toast.makeText(this, "Bluetooth service not bound", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check Bluetooth permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    1);
            return;
        }

        List<String> pairedDevices = bluetoothService.getPairedDevices();
        if (pairedDevices.isEmpty()) {
            Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a Bluetooth device");
        builder.setItems(pairedDevices.toArray(new String[0]), (dialog, which) -> {
            // Extract MAC address from the selected item
            String selectedDevice = pairedDevices.get(which);
            String macAddress = selectedDevice.substring(selectedDevice.length() - 17);

            // Connect to the selected device
            bluetoothService.connectToDevice(macAddress);
        });
        builder.show();
    }

    /**
     * Unbinds and stops the BluetoothService when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
        stopService(new Intent(this, BluetoothService.class));
    }
}
