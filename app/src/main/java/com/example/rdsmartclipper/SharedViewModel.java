package com.example.rdsmartclipper;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SharedViewModel manages shared data between different components of the app.
 */
public class SharedViewModel extends ViewModel {

    // Maximum number of entries to keep
    private static final int MAX_ENTRIES = 10000;

    // Map to hold ChartData for different types
    private final Map<String, ChartData> chartDataMap = new HashMap<>();

    // LiveData for rotation data
    private final MutableLiveData<RotationData> rotationData = new MutableLiveData<>();

    public SharedViewModel() {
        // Initialize chart data for different types
        chartDataMap.put("Voltage", new ChartData(0f, 8f));
        chartDataMap.put("Current", new ChartData(-5f, 5f));
        chartDataMap.put("RPM", new ChartData(7000f, 12000f));
        chartDataMap.put("Temperature", new ChartData(0f, 90f));
        chartDataMap.put("Acceleration", new ChartData(0f, 10f));
        // Add other chart types as needed
    }

    /**
     * Gets the ChartData object for the specified chart type.
     *
     * @param chartType The type of chart (e.g., "Voltage", "Current").
     * @return The ChartData object.
     */
    public ChartData getChartData(String chartType) {
        ChartData data = chartDataMap.get(chartType);
        if (data == null) {
            Log.e("SharedViewModel", "ChartData for " + chartType + " is null.");
        }
        return data;
    }

    /**
     * Adds an entry to the specified chart.
     *
     * @param chartType The type of chart.
     * @param entry     The Entry to add.
     */
    public void addEntry(String chartType, Entry entry) {
        ChartData chartData = chartDataMap.get(chartType);
        if (chartData != null) {
            chartData.addEntry(entry);
        }
    }

    /**
     * Sets the Y-axis limits for the specified chart.
     *
     * @param chartType  The type of chart.
     * @param lowerLimit The lower Y-axis limit.
     * @param upperLimit The upper Y-axis limit.
     */
    public void setYLimits(String chartType, Float lowerLimit, Float upperLimit) {
        ChartData chartData = chartDataMap.get(chartType);
        if (chartData != null) {
            chartData.setYLimits(lowerLimit, upperLimit);
        }
    }

    /**
     * Sets the rotation data.
     *
     * @param roll  The roll value.
     * @param pitch The pitch value.
     * @param yaw   The yaw value.
     */
    public void setRotationData(float roll, float pitch, float yaw) {
        rotationData.postValue(new RotationData(roll, pitch, yaw));
    }

    /**
     * Gets the LiveData for rotation data.
     *
     * @return LiveData containing RotationData.
     */
    public LiveData<RotationData> getRotationData() {
        return rotationData;
    }

    /**
     * Clears all data from the chart entries and resets rotation data.
     */
    public void clearAllData() {
        for (ChartData chartData : chartDataMap.values()) {
            chartData.clearEntries();
        }
        rotationData.setValue(null);
    }
}
