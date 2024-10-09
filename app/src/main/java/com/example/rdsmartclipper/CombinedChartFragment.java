package com.example.rdsmartclipper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * CombinedChartFragment class
 */
public class CombinedChartFragment extends Fragment {

    public CombinedChartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_combined_chart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LineChart voltageChart = view.findViewById(R.id.voltage_chart);
        LineChart currentChart = view.findViewById(R.id.current_chart);
        LineChart accelerationChart = view.findViewById(R.id.acceleration_chart);
        LineChart rpmChart = view.findViewById(R.id.rpm_chart);

        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        setupChart(voltageChart, "Voltage", sharedViewModel.getVoltageEntries(),
                sharedViewModel.getVoltageLowerYLimit(), sharedViewModel.getVoltageUpperYLimit());

        setupChart(currentChart, "Current", sharedViewModel.getCurrentEntries(),
                sharedViewModel.getCurrentLowerYLimit(), sharedViewModel.getCurrentUpperYLimit());

        setupChart(accelerationChart, "Acceleration", sharedViewModel.getAccelerationEntries(),
                sharedViewModel.getAccelerationLowerYLimit(), sharedViewModel.getAccelerationUpperYLimit());

        setupChart(rpmChart, "RPM", sharedViewModel.getRPMEntries(),
                sharedViewModel.getRPMLowerYLimit(), sharedViewModel.getRPMUpperYLimit());
    }

    private void setupChart(LineChart chart, String label, LiveData<List<Entry>> entriesLiveData,
                            LiveData<Float> lowerYLimitLiveData, LiveData<Float> upperYLimitLiveData) {

        LineDataSet dataSet = new LineDataSet(new ArrayList<>(), label);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Configure chart appearance
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);

        // Apply initial Y-limits
        updateYLimits(chart, lowerYLimitLiveData.getValue(), upperYLimitLiveData.getValue());

        // Observe data changes
        entriesLiveData.observe(getViewLifecycleOwner(), entries -> {
            dataSet.setValues(entries);
            lineData.notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.invalidate();
        });

        // Observe Y-Limit changes
        lowerYLimitLiveData.observe(getViewLifecycleOwner(),
                lowerLimit -> updateYLimits(chart, lowerLimit, upperYLimitLiveData.getValue()));

        upperYLimitLiveData.observe(getViewLifecycleOwner(),
                upperLimit -> updateYLimits(chart, lowerYLimitLiveData.getValue(), upperLimit));
    }

    private void updateYLimits(LineChart chart, Float lowerLimit, Float upperLimit) {
        YAxis yAxis = chart.getAxisLeft();

        if (lowerLimit != null) {
            yAxis.setAxisMinimum(lowerLimit);
        } else {
            yAxis.resetAxisMinimum();
        }

        if (upperLimit != null) {
            yAxis.setAxisMaximum(upperLimit);
        } else {
            yAxis.resetAxisMaximum();
        }

        chart.invalidate();
    }
}
