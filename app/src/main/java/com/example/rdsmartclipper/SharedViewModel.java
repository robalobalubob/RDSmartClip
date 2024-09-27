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


    public LiveData<List<Entry>> getVoltageEntries() {
        return voltageEntries;
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

    public LiveData<List<Entry>> getTemperatureEntries() {
        return temperatureEntries;
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

    public LiveData<List<Entry>> getRPMEntries() {
        return rpmEntries;
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

    public LiveData<List<Entry>> getCurrentEntries() {
        return currentEntries;
    }

    public void clearAllData() {
        voltageEntries.setValue(new ArrayList<>());
        temperatureEntries.setValue(new ArrayList<>());
        rpmEntries.setValue(new ArrayList<>());
        currentEntries.setValue(new ArrayList<>());
    }
}
