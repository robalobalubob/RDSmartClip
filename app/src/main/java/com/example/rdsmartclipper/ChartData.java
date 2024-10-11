package com.example.rdsmartclipper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * ChartData encapsulates the data and Y-axis limits for a chart.
 */
public class ChartData {
    private static final int MAX_ENTRIES = 1000; // Maximum number of entries to keep

    private final MutableLiveData<List<Entry>> entries = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Float> lowerYLimit = new MutableLiveData<>();
    private final MutableLiveData<Float> upperYLimit = new MutableLiveData<>();

    /**
     * Constructor to initialize Y-axis limits.
     *
     * @param lowerYLimit Initial lower Y-axis limit.
     * @param upperYLimit Initial upper Y-axis limit.
     */
    public ChartData(Float lowerYLimit, Float upperYLimit) {
        this.lowerYLimit.setValue(lowerYLimit);
        this.upperYLimit.setValue(upperYLimit);
    }

    /**
     * Adds an entry to the chart data.
     *
     * @param entry The Entry to add.
     */
    public void addEntry(Entry entry) {
        List<Entry> currentList = entries.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        } else {
            currentList = new ArrayList<>(currentList); // Create a copy
        }
        currentList.add(entry);

        // Trim list if necessary
        if (currentList.size() > MAX_ENTRIES) {
            currentList = currentList.subList(currentList.size() - MAX_ENTRIES, currentList.size());
        }

        entries.setValue(currentList);
    }

    /**
     * Gets the LiveData for the chart entries.
     *
     * @return LiveData containing the list of entries.
     */
    public LiveData<List<Entry>> getEntries() {
        return entries;
    }

    /**
     * Sets the Y-axis limits for the chart.
     *
     * @param lowerLimit The lower Y-axis limit.
     * @param upperLimit The upper Y-axis limit.
     */
    public void setYLimits(Float lowerLimit, Float upperLimit) {
        lowerYLimit.setValue(lowerLimit);
        upperYLimit.setValue(upperLimit);
    }

    /**
     * Gets the LiveData for the lower Y-axis limit.
     *
     * @return LiveData containing the lower Y-axis limit.
     */
    public LiveData<Float> getLowerYLimit() {
        return lowerYLimit;
    }

    /**
     * Gets the LiveData for the upper Y-axis limit.
     *
     * @return LiveData containing the upper Y-axis limit.
     */
    public LiveData<Float> getUpperYLimit() {
        return upperYLimit;
    }

    /**
     * Clears all entries from the chart data.
     */
    public void clearEntries() {
        entries.setValue(new ArrayList<>());
    }
}
