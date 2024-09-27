package com.example.rdsmartclipper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<List<Entry>> voltageEntries = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Entry>> temperatureEntries = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Entry>> rpmEntries = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Entry>> currentEntries = new MutableLiveData<>(new ArrayList<>());

    private float voltageYLimit = 10; // Default value
    private float temperatureYLimit = 100;
    private float rpmYLimit = 10000;
    private float currentYLimit = 10;

    private static final int MAX_ENTRIES = 10000; // Or any suitable number

    public void addVoltageEntries(List<Entry> entries) {
        List<Entry> currentList = voltageEntries.getValue();
        if (currentList != null) {
            currentList.addAll(entries);
            // Remove old entries if size exceeds MAX_ENTRIES
            if (currentList.size() > MAX_ENTRIES) {
                currentList = currentList.subList(currentList.size() - MAX_ENTRIES, currentList.size());
            }
            voltageEntries.setValue(currentList);
        }
    }

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


    public LiveData<List<Entry>> getVoltageEntries() {
        return voltageEntries;
    }

    public float getVoltageYLimit() {
        return voltageYLimit;
    }

    public void setVoltageYLimit(float voltageYLimit) {
        this.voltageYLimit = voltageYLimit;
    }

    public void addTemperatureEntries(List<Entry> entries) {
        List<Entry> currentList = temperatureEntries.getValue();
        if (currentList != null) {
            currentList.addAll(entries);
            if (currentList.size() > MAX_ENTRIES) {
                currentList = currentList.subList(currentList.size() - MAX_ENTRIES, currentList.size());
            }
            temperatureEntries.setValue(currentList);
        }
    }

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

    public LiveData<List<Entry>> getTemperatureEntries() {
        return temperatureEntries;
    }

    public float getTemperatureYLimit() {
        return temperatureYLimit;
    }

    public void setTemperatureYLimit(float temperatureYLimit) {
        this.temperatureYLimit = temperatureYLimit;
    }

    public void addRPMEntries(List<Entry> entries) {
        List<Entry> currentList = rpmEntries.getValue();
        if (currentList != null) {
            currentList.addAll(entries);
            if (currentList.size() > MAX_ENTRIES) {
                currentList = currentList.subList(currentList.size() - MAX_ENTRIES, currentList.size());
            }
            rpmEntries.setValue(currentList);
        }
    }

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

    public LiveData<List<Entry>> getRPMEntries() {
        return rpmEntries;
    }

    public float getRPMYLimit() {
        return rpmYLimit;
    }

    public void setRPMYLimit(float rpmYLimit) {
        this.rpmYLimit = rpmYLimit;
    }


    public void addCurrentEntries(List<Entry> entries) {
        List<Entry> currentList = currentEntries.getValue();
        if (currentList != null) {
            currentList.addAll(entries);
            if (currentList.size() > MAX_ENTRIES) {
                currentList = currentList.subList(currentList.size() - MAX_ENTRIES, currentList.size());
            }
            currentEntries.setValue(currentList);
        }
    }

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

    public LiveData<List<Entry>> getCurrentEntries() {
        return currentEntries;
    }

    public float getCurrentYLimit() {
        return currentYLimit;
    }

    public void setCurrentYLimit(float currentYLimit) {
        this.currentYLimit = currentYLimit;
    }

    public void clearAllData() {
        voltageEntries.setValue(new ArrayList<>());
        temperatureEntries.setValue(new ArrayList<>());
        rpmEntries.setValue(new ArrayList<>());
        currentEntries.setValue(new ArrayList<>());
    }
}
