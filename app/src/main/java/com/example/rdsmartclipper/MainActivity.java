package com.example.rdsmartclipper;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.rdsmartclipper.databinding.ActivityMainBinding;
import com.example.rdsmartclipper.fragments.ChartsFragment;
import com.example.rdsmartclipper.fragments.CombinedChartFragment;
import com.example.rdsmartclipper.fragments.OpenGLFragment;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

/**
 * MainActivity
 * Creates and initializes the main view of the application.
 */
public class MainActivity extends AppCompatActivity implements BluetoothService.BluetoothDataCallback {

    // Key strings
    private static final String DEBUG_MODE_KEY = "debug_mode";
    private static final String FULLSCREEN_MODE_KEY = "fullscreen_mode";
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    private ActivityMainBinding binding;
    private BluetoothService bluetoothService;
    private boolean isServiceBound = false;
    private SharedViewModel sharedViewModel;

    // TextViews for parameters
    private TextView textVoltage, textVoltageMin, textVoltageMax, textVoltageAvg;
    private TextView textCurrent, textCurrentMin, textCurrentMax, textCurrentAvg;
    private TextView textTemperature, textTemperatureMin, textTemperatureMax, textTemperatureAvg;
    private TextView textRPM, textRPMMin, textRPMMax, textRPMAvg;
    private TextView textAcceleration, textAccelerationMin, textAccelerationMax, textAccelerationAvg;

    // Mode checks
    private boolean isDebugMode = false;
    private boolean isFullscreenMode = true;
    private boolean lastKnownFullscreenMode = true;

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
     * ServiceConnection to manage the BluetoothService binding.
     */
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
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
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

        // Initialize the SharedViewModel
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        // Setup bottom navigation
        BottomNavigationView bottomNavigationView = binding.bottomNavigation;
        setupBottomNavigation(bottomNavigationView);

        // Initialize TextViews
        initializeTextViews();

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

    /**
     * Initializes the TextViews for displaying data.
     */
    private void initializeTextViews() {
        // Voltage TextViews
        textVoltage = binding.textVoltage;
        textVoltageMin = binding.textVoltageMin;
        textVoltageMax = binding.textVoltageMax;
        textVoltageAvg = binding.textVoltageAvg;

        // Current TextViews
        textCurrent = binding.textCurrent;
        textCurrentMin = binding.textCurrentMin;
        textCurrentMax = binding.textCurrentMax;
        textCurrentAvg = binding.textCurrentAvg;

        // Temperature TextViews
        textTemperature = binding.textTemperature;
        textTemperatureMin = binding.textTemperatureMin;
        textTemperatureMax = binding.textTemperatureMax;
        textTemperatureAvg = binding.textTemperatureAvg;

        // RPM TextViews
        textRPM = binding.textRpm;
        textRPMMin = binding.textRpmMin;
        textRPMMax = binding.textRpmMax;
        textRPMAvg = binding.textRpmAvg;

        // Acceleration TextViews
        textAcceleration = binding.textAcceleration;
        textAccelerationMin = binding.textAccelerationMin;
        textAccelerationMax = binding.textAccelerationMax;
        textAccelerationAvg = binding.textAccelerationAvg;
    }

    /**
     * Sets up the bottom navigation and handles item selection.
     */
    private void setupBottomNavigation(BottomNavigationView bottomNavigationView) {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                // Show main content
                binding.mainContent.setVisibility(View.VISIBLE);
                // Hide any fragments
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    getSupportActionBar().setTitle("SmartClip");
                }
                return true;
            } else if (itemId == R.id.navigation_charts) {
                // Open ChartsFragment
                openFragment(new ChartsFragment(), isFullscreenMode);
                return true;
            } else if (itemId == R.id.navigation_combined_chart) {
                // Open Combined Chart
                openFragment(new CombinedChartFragment(), isFullscreenMode);
                return true;
            } else if (itemId == R.id.navigation_3d_model) {
                // Open 3D Model
                openFragment(new OpenGLFragment(), isFullscreenMode);
                return true;
            } else if (itemId == R.id.navigation_settings) {
                // Navigate to settings
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    /**
     * Checks for Bluetooth permissions and requests them if not granted.
     */
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

    /**
     * Starts and binds the BluetoothService.
     */
    private void startAndBindBluetoothService() {
        Intent intent = new Intent(this, BluetoothService.class);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Shows a list of paired Bluetooth devices and initiates connection.
     */
    private void showDeviceListAndConnect() {
        if (isServiceBound && bluetoothService != null) {
            List<String> deviceList = bluetoothService.getPairedDevices();
            if (deviceList.isEmpty()) {
                Toast.makeText(this, "No paired Bluetooth devices found.", Toast.LENGTH_SHORT).show();
                bluetoothService.startDiscovery();
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
     *
     * @param requestCode  The request code passed
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *                     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
     * Opens a fragment in either fullscreen or non-fullscreen mode.
     *
     * @param fragment   Fragment to be opened.
     * @param fullscreen Whether or not the fragment is opened in fullscreen.
     */
    public void openFragment(Fragment fragment, Boolean fullscreen) {
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
        updateBottomNavigationSelection(fragmentTag);
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

    private void updateBottomNavigationSelection(String fragmentTag) {
        switch (fragmentTag) {
            case "HomeFragment":
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_home);
                break;
            case "ChartsFragment":
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_charts);
                break;
            case "CombinedChartFragment":
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_combined_chart);
                break;
            case "OpenGLFragment":
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_3d_model);
                break;
            case "SettingsFragment":
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_settings);
                break;
            default:
                // Deselect all if the fragment is not associated with a bottom navigation item
                binding.bottomNavigation.getMenu().setGroupCheckable(0, false, true);
                break;
        }
    }

    /**
     * Updates the text elements with the latest data from the ViewModel.
     */
    private void observeData() {
        observeParameter("Voltage", textVoltage, textVoltageMin, textVoltageMax, textVoltageAvg, "V", "%.2f");
        observeParameter("Current", textCurrent, textCurrentMin, textCurrentMax, textCurrentAvg, "A", "%.2f");
        observeParameter("Temperature", textTemperature, textTemperatureMin, textTemperatureMax, textTemperatureAvg, "°C", "%.2f");
        observeParameter("RPM", textRPM, textRPMMin, textRPMMax, textRPMAvg, "", "%.0f");
        observeParameter("Acceleration", textAcceleration, textAccelerationMin, textAccelerationMax, textAccelerationAvg, "m/s²", "%.2f");
    }

    /**
     * Observes a parameter and updates its TextViews.
     *
     * @param parameterName  Name of the parameter.
     * @param latestTextView TextView for the latest value.
     * @param minTextView    TextView for the minimum value.
     * @param maxTextView    TextView for the maximum value.
     * @param avgTextView    TextView for the average value.
     * @param unit           Unit of measurement.
     * @param valueFormat    Format string for the values.
     */
    private void observeParameter(String parameterName, TextView latestTextView, TextView minTextView, TextView maxTextView, TextView avgTextView, String unit, String valueFormat) {
        sharedViewModel.getChartData(parameterName).getEntries().observe(this, entries -> {
            if (entries != null && !entries.isEmpty()) {
                Entry latestEntry = entries.get(entries.size() - 1);
                float latestValue = latestEntry.getY();

                // Compute min, max, average
                float minValue = Float.MAX_VALUE;
                float maxValue = Float.MIN_VALUE;
                float sum = 0f;
                for (Entry entry : entries) {
                    float value = entry.getY();
                    if (value < minValue) {
                        minValue = value;
                    }
                    if (value > maxValue) {
                        maxValue = value;
                    }
                    sum += value;
                }
                float avgValue = sum / entries.size();

                // Update TextViews
                latestTextView.setText(String.format(Locale.getDefault(), valueFormat + " %s", latestValue, unit));
                minTextView.setText(String.format(Locale.getDefault(), "Min: " + valueFormat + " %s", minValue, unit));
                maxTextView.setText(String.format(Locale.getDefault(), "Max: " + valueFormat + " %s", maxValue, unit));
                avgTextView.setText(String.format(Locale.getDefault(), "Avg: " + valueFormat + " %s", avgValue, unit));
            } else {
                latestTextView.setText(R.string.n_a);
                minTextView.setText(R.string.min_n_a);
                maxTextView.setText(R.string.max_n_a);
                avgTextView.setText(R.string.avg_n_a);
            }
        });
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
     * Clears all data in the ViewModel and resets TextViews.
     */
    private void clearData() {
        // Clear data in ViewModel
        sharedViewModel.clearAllData();

        // Reset TextViews
        textVoltage.setText(R.string.n_a);
        textVoltageMin.setText(R.string.min_n_a);
        textVoltageMax.setText(R.string.max_n_a);
        textVoltageAvg.setText(R.string.avg_n_a);

        textCurrent.setText(R.string.n_a);
        textCurrentMin.setText(R.string.min_n_a);
        textCurrentMax.setText(R.string.max_n_a);
        textCurrentAvg.setText(R.string.avg_n_a);

        textTemperature.setText(R.string.n_a);
        textTemperatureMin.setText(R.string.min_n_a);
        textTemperatureMax.setText(R.string.max_n_a);
        textTemperatureAvg.setText(R.string.avg_n_a);

        textRPM.setText(R.string.n_a);
        textRPMMin.setText(R.string.min_n_a);
        textRPMMax.setText(R.string.max_n_a);
        textRPMAvg.setText(R.string.avg_n_a);

        textAcceleration.setText(R.string.n_a);
        textAccelerationMin.setText(R.string.min_n_a);
        textAccelerationMax.setText(R.string.max_n_a);
        textAccelerationAvg.setText(R.string.avg_n_a);
    }

    /**
     * Reads data from file and parses it.
     * Only used in debug mode.
     */
    private void readDataFromFile() {
        new Thread(() -> {
            try {
                InputStream inputStream = getResources().openRawResource(R.raw.data);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Simulate real-time data by adding a delay
                    Thread.sleep(1500);
                    parseCSVData(line);
                }
            } catch (IOException e) {
                Log.e("MainActivity", "Failed to read data file.", e);
                runOnUiThread(() -> Toast.makeText(this, "Failed to read data file.", Toast.LENGTH_SHORT).show());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * Parses the CSV data and adds it to the ViewModel.
     *
     * @param data Data to be added to the ViewModel.
     */
    private void parseCSVData(String data) {
        CSVParser csvParser = new CSVParser();
        List<DataPoint> dataPoints = csvParser.parseCSV(data);

        for (DataPoint point : dataPoints) {
            Entry voltageEntry = new Entry(point.time, point.voltage);
            Entry temperatureEntry = new Entry(point.time, point.temperature);
            Entry rpmEntry = new Entry(point.time, point.rpm);
            Entry currentEntry = new Entry(point.time, point.current);
            Entry accelerationEntry = new Entry(point.time, point.acceleration);

            // Update ViewModel on the main thread
            runOnUiThread(() -> {
                sharedViewModel.addEntry("Voltage", voltageEntry);
                sharedViewModel.addEntry("Temperature", temperatureEntry);
                sharedViewModel.addEntry("RPM", rpmEntry);
                sharedViewModel.addEntry("Current", currentEntry);
                sharedViewModel.addEntry("Acceleration", accelerationEntry);

                // Check if rotation data is available
                if (point.roll != 0 || point.pitch != 0 || point.yaw != 0) {
                    sharedViewModel.setRotationData(point.roll, point.pitch, point.yaw);
                }
            });
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
     *
     * @param data Data received from the BluetoothService.
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
     * Displays options menu.
     *
     * @param menu The options menu in which you place your items.
     * @return True if the menu is displayed. Always true.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu with the settings item
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    /**
     * Called when an item in the options menu is selected.
     *
     * @param item The menu item that was selected.
     * @return True if the item selection was handled.
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
