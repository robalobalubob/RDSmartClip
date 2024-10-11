//package com.example.rdsmartclipper.backup;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//
//import com.example.rdsmartclipper.RotationData;
//import com.github.mikephil.charting.data.Entry;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * SharedViewModel class
// * Used for the fragment charts to share data
// */
//public class SharedViewModelBackup extends ViewModel {
//    // Maximum number of entries to keep
//    private static final int MAX_ENTRIES = 10000; // Or any suitable number
//    // LiveData for each chart
//    private final MutableLiveData<List<Entry>> voltageEntries = new MutableLiveData<>(new ArrayList<>());
//    private final MutableLiveData<List<Entry>> temperatureEntries = new MutableLiveData<>(new ArrayList<>());
//    private final MutableLiveData<List<Entry>> rpmEntries = new MutableLiveData<>(new ArrayList<>());
//    private final MutableLiveData<List<Entry>> currentEntries = new MutableLiveData<>(new ArrayList<>());
//    private final MutableLiveData<List<Entry>> accelerationEntries = new MutableLiveData<>(new ArrayList<>());
//    // LiveData for rotation data
//    private final MutableLiveData<RotationData> rotationData = new MutableLiveData<>();
//    // Voltage Y-Limits
//    private final MutableLiveData<Float> voltageLowerYLimit = new MutableLiveData<>((float) 0);
//    private final MutableLiveData<Float> voltageUpperYLimit = new MutableLiveData<>((float) 8);
//    // Current Y-Limits
//    private final MutableLiveData<Float> currentLowerYLimit = new MutableLiveData<>((float) -5);
//    private final MutableLiveData<Float> currentUpperYLimit = new MutableLiveData<>((float) 5);
//    // Acceleration Y-Limits
//    private final MutableLiveData<Float> accelerationLowerYLimit = new MutableLiveData<>((float) 0);
//    private final MutableLiveData<Float> accelerationUpperYLimit = new MutableLiveData<>((float) 10);
//    // RPM Y-Limits
//    private final MutableLiveData<Float> rpmLowerYLimit = new MutableLiveData<>((float) 7000);
//    private final MutableLiveData<Float> rpmUpperYLimit = new MutableLiveData<>((float) 1200);
//    // Temperature Y-Limits
//    private final MutableLiveData<Float> temperatureLowerYLimit = new MutableLiveData<>((float) 0);
//    private final MutableLiveData<Float> temperatureUpperYLimit = new MutableLiveData<>((float) 90);
//
//    /**
//     * Adds a single voltage entry to the LiveData
//     *
//     * @param entry Entry to add
//     */
//    public void addVoltageEntry(Entry entry) {
//        List<Entry> currentList = voltageEntries.getValue();
//        if (currentList == null) {
//            currentList = new ArrayList<>();
//        }
//        currentList.add(entry);
//        // Remove old entries if size exceeds MAX_ENTRIES
//        if (currentList.size() > MAX_ENTRIES) {
//            currentList = currentList.subList(currentList.size() - MAX_ENTRIES, currentList.size());
//        }
//        voltageEntries.setValue(currentList);
//    }
//
//    /**
//     * Gets the LiveData for voltage entries
//     *
//     * @return LiveData for voltage entries
//     */
//    public LiveData<List<Entry>> getVoltageEntries() {
//        return voltageEntries;
//    }
//
//    public void setVoltageYLimits(Float lowerLimit, Float upperLimit) {
//        voltageLowerYLimit.setValue(lowerLimit);
//        voltageUpperYLimit.setValue(upperLimit);
//    }
//
//    public LiveData<Float> getVoltageLowerYLimit() {
//        return voltageLowerYLimit;
//    }
//
//    public LiveData<Float> getVoltageUpperYLimit() {
//        return voltageUpperYLimit;
//    }
//
//    /**
//     * Adds a single temperature entry to the LiveData
//     *
//     * @param entry Entry to add
//     */
//    public void addTemperatureEntry(Entry entry) {
//        List<Entry> currentList = temperatureEntries.getValue();
//        if (currentList == null) {
//            currentList = new ArrayList<>();
//        }
//        currentList.add(entry);
//        // Remove old entries if size exceeds MAX_ENTRIES
//        if (currentList.size() > MAX_ENTRIES) {
//            currentList = currentList.subList(currentList.size() - MAX_ENTRIES, currentList.size());
//        }
//        temperatureEntries.setValue(currentList);
//    }
//
//    /**
//     * Gets the LiveData for temperature entries
//     *
//     * @return LiveData for temperature entries
//     */
//    public LiveData<List<Entry>> getTemperatureEntries() {
//        return temperatureEntries;
//    }
//
//    public void setTemperatureYLimits(Float lowerLimit, Float upperLimit) {
//        temperatureLowerYLimit.setValue(lowerLimit);
//        temperatureUpperYLimit.setValue(upperLimit);
//    }
//
//    public LiveData<Float> getTemperatureLowerYLimit() {
//        return temperatureLowerYLimit;
//    }
//
//    public LiveData<Float> getTemperatureUpperYLimit() {
//        return temperatureUpperYLimit;
//    }
//
//    /**
//     * Adds a single RPM entry to the LiveData
//     *
//     * @param entry Entry to add
//     */
//    public void addRPMEntry(Entry entry) {
//        List<Entry> currentList = rpmEntries.getValue();
//        if (currentList == null) {
//            currentList = new ArrayList<>();
//        }
//        currentList.add(entry);
//        // Remove old entries if size exceeds MAX_ENTRIES
//        if (currentList.size() > MAX_ENTRIES) {
//            currentList = currentList.subList(currentList.size() - MAX_ENTRIES, currentList.size());
//        }
//        rpmEntries.setValue(currentList);
//    }
//
//    /**
//     * Gets the LiveData for RPM entries
//     *
//     * @return LiveData for RPM entries
//     */
//    public LiveData<List<Entry>> getRPMEntries() {
//        return rpmEntries;
//    }
//
//    public void setRPMYLimits(Float lowerLimit, Float upperLimit) {
//        rpmLowerYLimit.setValue(lowerLimit);
//        rpmUpperYLimit.setValue(upperLimit);
//    }
//
//    public LiveData<Float> getRPMLowerYLimit() {
//        return rpmLowerYLimit;
//    }
//
//    public LiveData<Float> getRPMUpperYLimit() {
//        return rpmUpperYLimit;
//    }
//
//    /**
//     * Adds a single current entry to the LiveData
//     *
//     * @param entry Entry to add
//     */
//    public void addCurrentEntry(Entry entry) {
//        List<Entry> currentList = currentEntries.getValue();
//        if (currentList == null) {
//            currentList = new ArrayList<>();
//        }
//        currentList.add(entry);
//        // Remove old entries if size exceeds MAX_ENTRIES
//        if (currentList.size() > MAX_ENTRIES) {
//            currentList = currentList.subList(currentList.size() - MAX_ENTRIES, currentList.size());
//        }
//        currentEntries.setValue(currentList);
//    }
//
//    /**
//     * Gets the LiveData for current entries
//     *
//     * @return LiveData for current entries
//     */
//    public LiveData<List<Entry>> getCurrentEntries() {
//        return currentEntries;
//    }
//
//    public void setCurrentYLimits(Float lowerLimit, Float upperLimit) {
//        currentLowerYLimit.setValue(lowerLimit);
//        currentUpperYLimit.setValue(upperLimit);
//    }
//
//    public LiveData<Float> getCurrentLowerYLimit() {
//        return currentLowerYLimit;
//    }
//
//    public LiveData<Float> getCurrentUpperYLimit() {
//        return currentUpperYLimit;
//    }
//
//    public void setRotationData(float roll, float pitch, float yaw) {
//        rotationData.postValue(new RotationData(roll, pitch, yaw));
//    }
//
//    public LiveData<RotationData> getRotationData() {
//        return rotationData;
//    }
//
//    public LiveData<List<Entry>> getAccelerationEntries() {
//        return accelerationEntries;
//    }
//
//    public void addAccelerationEntry(Entry entry) {
//        List<Entry> accelerationList = accelerationEntries.getValue();
//        if (accelerationList == null) {
//            accelerationList = new ArrayList<>();
//        }
//        accelerationList.add(entry);
//        // Remove old entries if size exceeds MAX_ENTRIES
//        if (accelerationList.size() > MAX_ENTRIES) {
//            accelerationList = accelerationList.subList(accelerationList.size() - MAX_ENTRIES, accelerationList.size());
//        }
//        accelerationEntries.setValue(accelerationList);
//    }
//
//    public void setAccelerationYLimits(Float lowerLimit, Float upperLimit) {
//        accelerationLowerYLimit.setValue(lowerLimit);
//        accelerationUpperYLimit.setValue(upperLimit);
//    }
//
//    public LiveData<Float> getAccelerationLowerYLimit() {
//        return accelerationLowerYLimit;
//    }
//
//    public LiveData<Float> getAccelerationUpperYLimit() {
//        return accelerationUpperYLimit;
//    }
//
//    /**
//     * Clears all data from the LiveData
//     */
//    public void clearAllData() {
//        voltageEntries.setValue(new ArrayList<>());
//        temperatureEntries.setValue(new ArrayList<>());
//        rpmEntries.setValue(new ArrayList<>());
//        currentEntries.setValue(new ArrayList<>());
//        accelerationEntries.setValue(new ArrayList<>());
//        rotationData.setValue(null);
//    }
//}
