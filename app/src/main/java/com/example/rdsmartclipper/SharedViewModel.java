package com.example.rdsmartclipper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * SharedViewModel class
 * Used for the fragment charts to share data
 */
public class SharedViewModel extends ViewModel {
    // LiveData for each chart
    private final MutableLiveData<List<Entry>> voltageEntries = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Entry>> temperatureEntries = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Entry>> rpmEntries = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Entry>> currentEntries = new MutableLiveData<>(new ArrayList<>());

    // LiveData for rotation data
    private final MutableLiveData<RotationData> rotationData = new MutableLiveData<>();

    // Y-axis limits
    private float voltageYLimit = 10; // Default value
    private float temperatureYLimit = 100;
    private float rpmYLimit = 10000;
    private float currentYLimit = 10;
    // Maximum number of entries to keep
    private static final int MAX_ENTRIES = 10000; // Or any suitable number

    /**
     * Adds a single voltage entry to the LiveData
     * @param entry Entry to add
     */
    public void addVoltageEntry(Entry entry) {
        List<Entry> currentList = voltageEntries.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }
        currentList.add(entry);
        // Remove old entries if size exceeds MAX_ENTRIES
        if (currentList.size() > MAX_ENTRIES) {
            currentList = currentList.subList(currentList.size() - MAX_ENTRIES, currentList.size());
        }
        voltageEntries.setValue(currentList);
    }

    /**
     * Gets the LiveData for voltage entries
     * @return LiveData for voltage entries
     */
    public LiveData<List<Entry>> getVoltageEntries() {
        return voltageEntries;
    }

    /**
     * Gets the Y-axis limit for voltage
     * @return Y-axis limit for voltage
     */
    public float getVoltageYLimit() {
        return voltageYLimit;
    }

    /**
     * Sets the Y-axis limit for voltage
     * @param voltageYLimit Y-axis limit for voltage
     */
    public void setVoltageYLimit(float voltageYLimit) {
        this.voltageYLimit = voltageYLimit;
    }

    /**
     * Adds a single temperature entry to the LiveData
     * @param entry Entry to add
     */
    public void addTemperatureEntry(Entry entry) {
        List<Entry> currentList = temperatureEntries.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }
        currentList.add(entry);
        // Remove old entries if size exceeds MAX_ENTRIES
        if (currentList.size() > MAX_ENTRIES) {
            currentList = currentList.subList(currentList.size() - MAX_ENTRIES, currentList.size());
        }
        temperatureEntries.setValue(currentList);
    }

    /**
     * Gets the LiveData for temperature entries
     * @return LiveData for temperature entries
     */
    public LiveData<List<Entry>> getTemperatureEntries() {
        return temperatureEntries;
    }

    /**
     * Gets the Y-axis limit for temperature
     * @return Y-axis limit for temperature
     */
    public float getTemperatureYLimit() {
        return temperatureYLimit;
    }

    /**
     * Sets the Y-axis limit for temperature
     * @param temperatureYLimit Y-axis limit for temperature
     */
    public void setTemperatureYLimit(float temperatureYLimit) {
        this.temperatureYLimit = temperatureYLimit;
    }

    /**
     * Adds a single RPM entry to the LiveData
     * @param entry Entry to add
     */
    public void addRPMEntry(Entry entry) {
        List<Entry> currentList = rpmEntries.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }
        currentList.add(entry);
        // Remove old entries if size exceeds MAX_ENTRIES
        if (currentList.size() > MAX_ENTRIES) {
            currentList = currentList.subList(currentList.size() - MAX_ENTRIES, currentList.size());
        }
        rpmEntries.setValue(currentList);
    }

    /**
     * Gets the LiveData for RPM entries
     * @return LiveData for RPM entries
     */
    public LiveData<List<Entry>> getRPMEntries() {
        return rpmEntries;
    }

    /**
     * Gets the Y-axis limit for RPM
     * @return Y-axis limit for RPM
     */
    public float getRPMYLimit() {
        return rpmYLimit;
    }

    /**
     * Sets the Y-axis limit for RPM
     * @param rpmYLimit Y-axis limit for RPM
     */
    public void setRPMYLimit(float rpmYLimit) {
        this.rpmYLimit = rpmYLimit;
    }

    /**
     * Adds a single current entry to the LiveData
     * @param entry Entry to add
     */
    public void addCurrentEntry(Entry entry) {
        List<Entry> currentList = currentEntries.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }
        currentList.add(entry);
        // Remove old entries if size exceeds MAX_ENTRIES
        if (currentList.size() > MAX_ENTRIES) {
            currentList = currentList.subList(currentList.size() - MAX_ENTRIES, currentList.size());
        }
        currentEntries.setValue(currentList);
    }

    /**
     * Gets the LiveData for current entries
     * @return LiveData for current entries
     */
    public LiveData<List<Entry>> getCurrentEntries() {
        return currentEntries;
    }

    /**
     * Gets the Y-axis limit for current
     * @return Y-axis limit for current
     */
    public float getCurrentYLimit() {
        return currentYLimit;
    }

    /**
     * Sets the Y-axis limit for current
     * @param currentYLimit Y-axis limit for current
     */
    public void setCurrentYLimit(float currentYLimit) {
        this.currentYLimit = currentYLimit;
    }

    public void setRotationData(float roll, float pitch, float yaw) {
        rotationData.postValue(new RotationData(roll, pitch, yaw));
    }

    public LiveData<RotationData> getRotationData() {
        return rotationData;
    }

    /**
     * Clears all data from the LiveData
     */
    public void clearAllData() {
        voltageEntries.setValue(new ArrayList<>());
        temperatureEntries.setValue(new ArrayList<>());
        rpmEntries.setValue(new ArrayList<>());
        currentEntries.setValue(new ArrayList<>());
    }
}
