//package com.example.rdsmartclipper.backup;
//
//import android.Manifest;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.OnBackPressedCallback;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.core.app.ActivityCompat;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentManager;
//import androidx.fragment.app.FragmentTransaction;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.preference.PreferenceManager;
//
//import com.example.rdsmartclipper.BluetoothService;
//import com.example.rdsmartclipper.CSVParser;
//import com.example.rdsmartclipper.fragments.ChartsFragment;
//import com.example.rdsmartclipper.fragments.CombinedChartFragment;
//import com.example.rdsmartclipper.DataPoint;
//import com.example.rdsmartclipper.fragments.OpenGLFragment;
//import com.example.rdsmartclipper.R;
//import com.example.rdsmartclipper.SettingsActivity;
//import com.example.rdsmartclipper.SharedViewModel;
//import com.example.rdsmartclipper.databinding.ActivityMainBinding;
//import com.github.mikephil.charting.data.Entry;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.List;
//import java.util.Locale;
//
///**
// * MainActivity
// * Creates and initializes the main view of the application.
// */
//public class MainActivityBackup extends AppCompatActivity implements BluetoothService.BluetoothDataCallback {
//
//    // Key strings
//    private static final String DEBUG_MODE_KEY = "debug_mode";
//    private static final String FULLSCREEN_MODE_KEY = "fullscreen_mode";
//    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
//    private ActivityMainBinding binding;
//    // Replace myBluetoothManager with BluetoothService
//    private BluetoothService bluetoothService;
//    private boolean isServiceBound = false;
//    private final ServiceConnection serviceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
//            bluetoothService = binder.getService();
//            isServiceBound = true;
//
//            // Register the callback to receive Bluetooth data
//            bluetoothService.registerCallback(MainActivityBackup.this);
//
//            // Show device list and connect
//            showDeviceListAndConnect();
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            isServiceBound = false;
//            bluetoothService = null;
//        }
//    };
//    private SharedViewModel sharedViewModel;
//    private TextView textVoltage;
//    private TextView textCurrent;
//    private TextView textTemperature;
//    private TextView textRPM;
//    private TextView textAcceleration;
//    // Default text, updated with new data
//    private String voltageText = "Voltage: N/A";
//    private String currentText = "Current: N/A";
//    private String temperatureText = "Temperature: N/A";
//    private String rpmText = "RPM: N/A";
//    private String accelerationText = "Acceleration: N/A";
//    // Mode checks
//    private boolean isDebugMode = false;
//    private boolean isFullscreenMode = true;
//    /**
//     * Handles when preferences change
//     */
//    private final SharedPreferences.OnSharedPreferenceChangeListener prefListener = (sharedPreferences, key) -> {
//        if (DEBUG_MODE_KEY.equals(key)) {
//            // Update mode
//            isDebugMode = sharedPreferences.getBoolean(DEBUG_MODE_KEY, false);
//            // Handle mode change
//            handleModeChange();
//        } else if (FULLSCREEN_MODE_KEY.equals(key)) {
//            // Update fullscreen mode
//            isFullscreenMode = sharedPreferences.getBoolean(FULLSCREEN_MODE_KEY, true);
//
//            Toast.makeText(this, "Fullscreen mode will apply the next time you open a chart.", Toast.LENGTH_SHORT).show();
//        }
//    };
//    private boolean lastKnownFullscreenMode = true;
//
//    /**
//     * @param savedInstanceState If the activity is being re-initialized after
//     *                           previously being shut down then this Bundle contains the data it most
//     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
//     */
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // Set up binding
//        binding = ActivityMainBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        // Set up preferences
//        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
//        sharedPrefs.registerOnSharedPreferenceChangeListener(prefListener);
//
//        // Establish modes
//        isDebugMode = sharedPrefs.getBoolean(DEBUG_MODE_KEY, false);
//        isFullscreenMode = sharedPrefs.getBoolean(FULLSCREEN_MODE_KEY, true);
//        lastKnownFullscreenMode = isFullscreenMode;
//
//        // Set up toolbar
//        Toolbar toolbar = binding.toolbar;
//        setSupportActionBar(toolbar);
//
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
//        bottomNavigationView.setOnItemSelectedListener(item -> {
//            int itemId = item.getItemId();
//            if (itemId == R.id.navigation_home) {
//                // Show main content
//                binding.mainContent.setVisibility(View.VISIBLE);
//                // Hide any fragments
//                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//                return true;
//            } else if (itemId == R.id.navigation_charts) {
//                // Open CombinedChartFragment
//                openFragment(new ChartsFragment(), isFullscreenMode);
//                return true;
//            } else if (itemId == R.id.navigation_combined_chart) {
//                // Open Combined Chart
//                openFragment(new CombinedChartFragment(), isFullscreenMode);
//                return true;
//            } else if (itemId == R.id.navigation_3d_model) {
//                // Open 3D Model
//                openFragment(new OpenGLFragment(), isFullscreenMode);
//                return true;
//            } else if (itemId == R.id.navigation_settings) {
//                // Navigate to settings
//                Intent intent = new Intent(MainActivityBackup.this, SettingsActivity.class);
//                startActivity(intent);
//                return true;
//            }
//            return false;
//        });
//
//        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
//
//        // Create text elements
//        textVoltage = binding.textVoltage;
//        textCurrent = binding.textCurrent;
//        textTemperature = binding.textTemperature;
//        textRPM = binding.textRpm;
//        textAcceleration = binding.textAcceleration;
//
//        // Observe data for changes
//        observeData();
//        // Prep for back navigation
//        handleBackNavigation();
//
//        // Check permissions and start Bluetooth or read from file
//        if (!isDebugMode) {
//            Log.d("MainActivity", "App is in normal mode, checking permissions");
//            checkAndRequestBluetoothPermissions();
//        } else {
//            Log.d("MainActivity", "App is in debug mode, reading data from file");
//            readDataFromFile();
//        }
//    }
//
//    private void checkAndRequestBluetoothPermissions() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
//                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(this,
//                    new String[]{
//                            Manifest.permission.BLUETOOTH_CONNECT,
//                            Manifest.permission.BLUETOOTH_SCAN
//                    },
//                    REQUEST_BLUETOOTH_PERMISSIONS);
//        } else {
//            startAndBindBluetoothService();
//        }
//    }
//
//    private void startAndBindBluetoothService() {
//        Intent intent = new Intent(this, BluetoothService.class);
//        startService(intent);
//        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
//    }
//
//    private void showDeviceListAndConnect() {
//        if (isServiceBound && bluetoothService != null) {
//            List<String> deviceList = bluetoothService.getPairedDevices();
//            if (deviceList.isEmpty()) {
//                Toast.makeText(this, "No paired Bluetooth devices found.", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle("Select Bluetooth Device");
//            builder.setItems(deviceList.toArray(new String[0]), (dialog, which) -> {
//                String deviceInfo = deviceList.get(which);
//                String macAddress = deviceInfo.substring(deviceInfo.length() - 17);
//                bluetoothService.connectToDevice(macAddress);
//            });
//            builder.show();
//        }
//    }
//
//    /**
//     * Handles the result of permission requests.
//     *
//     * @param requestCode  The request code passed
//     * @param permissions  The requested permissions. Never null.
//     * @param grantResults The grant results for the corresponding permissions
//     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
//     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
//     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
//            boolean permissionsGranted = true;
//            for (int result : grantResults) {
//                permissionsGranted = permissionsGranted && (result == PackageManager.PERMISSION_GRANTED);
//            }
//
//            if (permissionsGranted) {
//                startAndBindBluetoothService();
//            } else {
//                Toast.makeText(this, "Bluetooth permissions denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    /**
//     * Opens a fragment chart in either fullscreen or non-fullscreen mode.
//     *
//     * @param fragment   Fragment to be opened, one of the chart options
//     * @param fullscreen Whether or not the fragment is opened in fullscreen
//     */
//    public void openFragment(Fragment fragment, Boolean fullscreen) {
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//
//        String fragmentTag = fullscreen ? "FULLSCREEN" : "NON_FULLSCREEN";
//
//        Bundle args = new Bundle();
//        args.putBoolean("isFullscreen", fullscreen);
//        fragment.setArguments(args);
//
//        if (fullscreen) {
//            // Hide main content
//            binding.mainContent.setVisibility(View.GONE);
//            // Show fullscreen fragment container
//            binding.fullscreenFragmentContainer.setVisibility(View.VISIBLE);
//
//            fragmentTransaction.replace(binding.fullscreenFragmentContainer.getId(), fragment, fragmentTag);
//        } else {
//            fragmentTransaction.replace(binding.fragmentContainer.getId(), fragment, fragmentTag);
//        }
//
//        fragmentTransaction.addToBackStack(fragmentTag);
//        fragmentTransaction.commit();
//
//        updateToolbarNavigationIcon();
//    }
//
//    /**
//     * Updates the text elements with the latest data from the ViewModel
//     */
//    private void observeData() {
//        // Observer for Voltage
//        sharedViewModel.getVoltageEntries().observe(this, entries -> {
//            if (entries != null && !entries.isEmpty()) {
//                Entry latestEntry = entries.get(entries.size() - 1);
//                float voltage = latestEntry.getY();
//                updateVoltageText(voltage);
//            } else {
//                voltageText = "Voltage: N/A";
//                textVoltage.setText(voltageText);
//            }
//        });
//
//        // Observer for Current
//        sharedViewModel.getCurrentEntries().observe(this, entries -> {
//            if (entries != null && !entries.isEmpty()) {
//                Entry latestEntry = entries.get(entries.size() - 1);
//                float current = latestEntry.getY();
//                updateCurrentText(current);
//            } else {
//                currentText = "Current: N/A";
//                textCurrent.setText(currentText);
//            }
//        });
//
//        // Observer for Temperature
//        sharedViewModel.getTemperatureEntries().observe(this, entries -> {
//            if (entries != null && !entries.isEmpty()) {
//                Entry latestEntry = entries.get(entries.size() - 1);
//                float temperature = latestEntry.getY();
//                updateTemperatureText(temperature);
//            } else {
//                temperatureText = "Temperature: N/A";
//                textTemperature.setText(temperatureText);
//            }
//        });
//
//        // Observer for RPM
//        sharedViewModel.getRPMEntries().observe(this, entries -> {
//            if (entries != null && !entries.isEmpty()) {
//                Entry latestEntry = entries.get(entries.size() - 1);
//                float rpm = latestEntry.getY();
//                updateRPMText(rpm);
//            } else {
//                rpmText = "RPM: N/A";
//                textRPM.setText(rpmText);
//            }
//        });
//
//        sharedViewModel.getAccelerationEntries().observe(this, entries -> {
//            if (entries != null && !entries.isEmpty()) {
//                Entry latestEntry = entries.get(entries.size() - 1);
//                float acceleration = latestEntry.getY();
//                updateAccelerationText(acceleration);
//            } else {
//                accelerationText = "Acceleration: N/A";
//                textAcceleration.setText(accelerationText);
//            }
//        });
//    }
//
//    /**
//     * Updates the text element with the latest voltage data.
//     *
//     * @param voltage Voltage to update text to
//     */
//    private void updateVoltageText(float voltage) {
//        voltageText = String.format(Locale.getDefault(), "Voltage: %.2f V", voltage);
//        textVoltage.setText(voltageText);
//    }
//
//    /**
//     * Updates the text element with the latest current data.
//     *
//     * @param current Current to update text to
//     */
//    private void updateCurrentText(float current) {
//        currentText = String.format(Locale.getDefault(), "Current: %.2f A", current);
//        textCurrent.setText(currentText);
//    }
//
//    /**
//     * Updates the text element with the latest temperature data.
//     *
//     * @param temperature Temperature to update text to
//     */
//    private void updateTemperatureText(float temperature) {
//        temperatureText = String.format(Locale.getDefault(), "Temperature: %.2f°C", temperature);
//        textTemperature.setText(temperatureText);
//    }
//
//    /**
//     * Updates the text element with the latest RPM data.
//     *
//     * @param rpm RPM to update text to
//     */
//    private void updateRPMText(float rpm) {
//        rpmText = String.format(Locale.getDefault(), "RPM: %.0f", rpm);
//        textRPM.setText(rpmText);
//    }
//
//    private void updateAccelerationText(float acceleration) {
//        accelerationText = String.format(Locale.getDefault(), "Acceleration: %.0f", acceleration);
//        textAcceleration.setText(accelerationText);
//    }
//
//    /**
//     * Handles when Debug mode is toggled on and off.
//     */
//    private void handleModeChange() {
//        // Clear existing data
//        clearData();
//
//        if (isDebugMode) {
//            // Stop Bluetooth service if running
//            if (isServiceBound) {
//                unbindService(serviceConnection);
//                isServiceBound = false;
//            }
//            stopService(new Intent(this, BluetoothService.class));
//
//            // Read data from file
//            readDataFromFile();
//        } else {
//            // Check permissions and start Bluetooth
//            checkAndRequestBluetoothPermissions();
//        }
//    }
//
//    /**
//     * Clears all data in the ViewModel and resets local variables.
//     */
//    private void clearData() {
//        // Clear data in ViewModel
//        sharedViewModel.clearAllData();
//
//        // Reset local variables
//        voltageText = "Voltage: N/A";
//        currentText = "Current: N/A";
//        temperatureText = "Temperature: N/A";
//        rpmText = "RPM: N/A";
//        accelerationText = "Acceleration: N/A";
//
//        // Update TextViews
//        textVoltage.setText(voltageText);
//        textCurrent.setText(currentText);
//        textTemperature.setText(temperatureText);
//        textRPM.setText(rpmText);
//        textAcceleration.setText(accelerationText);
//    }
//
//    /**
//     * Reads data from file and parses it.
//     * Only used in debug mode
//     */
//    private void readDataFromFile() {
//        new Thread(() -> {
//            try {
//                InputStream inputStream = getResources().openRawResource(R.raw.data);
//                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    // Simulate real-time data by adding a delay
//                    //noinspection BusyWait
//                    Thread.sleep(1500);
//                    parseCSVData(line);
//                }
//            } catch (IOException e) {
//                Log.e("MainActivity", "Failed to read data file.", e);
//                runOnUiThread(() -> Toast.makeText(this, "Failed to read data file.", Toast.LENGTH_SHORT).show());
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }).start();
//    }
//
//    /**
//     * Parses the CSV data and adds it to the ViewModel.
//     *
//     * @param data Data to be added to the ViewModel
//     */
//    private void parseCSVData(String data) {
//        CSVParser csvParser = new CSVParser();
//        List<DataPoint> dataPoints = csvParser.parseCSV(data);
//
//        for (DataPoint point : dataPoints) {
//            Entry voltageEntry = new Entry(point.time, point.voltage);
//            Entry temperatureEntry = new Entry(point.time, point.temperature);
//            Entry rpmEntry = new Entry(point.time, point.rpm);
//            Entry currentEntry = new Entry(point.time, point.current);
//            Entry accelerationEntry = new Entry(point.time, point.acceleration);
//
//            // Update ViewModel on the main thread
//            runOnUiThread(() -> {
//                sharedViewModel.addVoltageEntry(voltageEntry);
//                sharedViewModel.addTemperatureEntry(temperatureEntry);
//                sharedViewModel.addRPMEntry(rpmEntry);
//                sharedViewModel.addCurrentEntry(currentEntry);
//                sharedViewModel.addAccelerationEntry(accelerationEntry);
//
//                // Check if rotation data is available
//                if (point.roll != 0 || point.pitch != 0 || point.yaw != 0) {
//                    sharedViewModel.setRotationData(point.roll, point.pitch, point.yaw);
//                }
//            });
//        }
//    }
//
//
//    /**
//     * Updates the toolbar navigation icon based on the back stack.
//     */
//    private void updateToolbarNavigationIcon() {
//        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(backStackEntryCount > 0);
//        }
//    }
//
//    /**
//     * Handles when the back button is pressed.
//     */
//    private void handleBackNavigation() {
//        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//                FragmentManager fragmentManager = getSupportFragmentManager();
//
//                if (fragmentManager.getBackStackEntryCount() > 0) {
//                    fragmentManager.popBackStack();
//
//                    // Add a BackStackChangedListener
//                    fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
//                        @Override
//                        public void onBackStackChanged() {
//                            // Remove the listener to prevent it from being called multiple times
//                            fragmentManager.removeOnBackStackChangedListener(this);
//
//                            // Update the toolbar navigation icon
//                            updateToolbarNavigationIcon();
//
//                            if (fragmentManager.getBackStackEntryCount() == 0) {
//                                // Show main content
//                                binding.mainContent.setVisibility(View.VISIBLE);
//                                // Hide fullscreen fragment container
//                                binding.fullscreenFragmentContainer.setVisibility(View.GONE);
//
//                                // Reset toolbar
//                                if (getSupportActionBar() != null) {
//                                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//                                    getSupportActionBar().setTitle("SmartClip");
//                                }
//
//                                // Remove navigation click listener
//                                Toolbar toolbar = binding.toolbar;
//                                toolbar.setNavigationOnClickListener(null);
//                            } else {
//                                // Get the last fragment in the back stack
//                                FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1);
//                                String tag = backStackEntry.getName();
//
//                                if ("FULLSCREEN".equals(tag)) {
//                                    // Show fullscreen container, hide main content
//                                    binding.mainContent.setVisibility(View.GONE);
//                                    binding.fullscreenFragmentContainer.setVisibility(View.VISIBLE);
//                                } else if ("NON_FULLSCREEN".equals(tag)) {
//                                    // Show main content, hide fullscreen container
//                                    binding.mainContent.setVisibility(View.VISIBLE);
//                                    binding.fullscreenFragmentContainer.setVisibility(View.GONE);
//                                }
//                            }
//                        }
//                    });
//                } else {
//                    finish();
//                }
//            }
//        };
//        getOnBackPressedDispatcher().addCallback(this, callback);
//    }
//
//    /**
//     * Called when data is received from the BluetoothService.
//     *
//     * @param data Data received from the BluetoothService
//     */
//    @Override
//    public void onBluetoothDataReceived(String data) {
//        new Thread(() -> parseCSVData(data)).start();
//    }
//
//    /**
//     * Called when the activity is resumed.
//     * Handles whether or not the fullscreen mode should be applied.
//     */
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
//        boolean currentFullscreenMode = sharedPrefs.getBoolean(FULLSCREEN_MODE_KEY, true);
//
//        if (currentFullscreenMode != lastKnownFullscreenMode) {
//            // The fullscreen mode setting has changed
//            lastKnownFullscreenMode = currentFullscreenMode;
//            isFullscreenMode = currentFullscreenMode;
//
//            // Clear the fragment back stack
//            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//
//            // Reset UI
//            binding.mainContent.setVisibility(View.VISIBLE);
//            binding.fullscreenFragmentContainer.setVisibility(View.GONE);
//        }
//    }
//
//    /**
//     * Displays options menu
//     *
//     * @param menu The options menu in which you place your items.
//     * @return True if the menu is displayed. Always true
//     */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu with the settings item
//        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
//        return true;
//    }
//
//    /**
//     * Called when an item in the options menu is selected.
//     *
//     * @param item The menu item that was selected.
//     * @return True
//     */
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        // Handle action bar item clicks
//        int id = item.getItemId();
//
//        if (id == R.id.action_settings) {
//            // Navigate to the Settings activity
//            Intent intent = new Intent(MainActivityBackup.this, SettingsActivity.class);
//            startActivity(intent);
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    /**
//     * Unbinds and stops the BluetoothService when the activity is destroyed.
//     */
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (isServiceBound) {
//            unbindService(serviceConnection);
//            isServiceBound = false;
//        }
//        stopService(new Intent(this, BluetoothService.class));
//    }
//}
