package com.example.rdsmartclipper;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class CombinedChartFragment extends Fragment {

    private LineChart voltageChart, currentChart, accelerationChart, rpmChart;
    private SharedViewModel sharedViewModel;

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
        voltageChart = view.findViewById(R.id.voltage_chart);
        currentChart = view.findViewById(R.id.current_chart);
        accelerationChart = view.findViewById(R.id.acceleration_chart);
        rpmChart = view.findViewById(R.id.rpm_chart);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        setupChart(voltageChart, "Voltage", sharedViewModel.getVoltageEntries(), sharedViewModel.getVoltageYLimit());
        setupChart(currentChart, "Current", sharedViewModel.getCurrentEntries(), sharedViewModel.getCurrentYLimit());
        setupChart(accelerationChart, "Acceleration", sharedViewModel.getAccelerationEntries(), sharedViewModel.getAccelerationYLimit());
        setupChart(rpmChart, "RPM", sharedViewModel.getRPMEntries(), sharedViewModel.getRPMYLimit());
    }

    private void setupChart(LineChart chart, String label, LiveData<List<Entry>> entriesLiveData, float yLimit) {
        LineDataSet dataSet = new LineDataSet(new ArrayList<>(), label);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Configure chart appearance
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMaximum(yLimit);
        leftAxis.setAxisMinimum(0);
        chart.getAxisRight().setEnabled(false);

        // Observe data changes
        entriesLiveData.observe(getViewLifecycleOwner(), entries -> {
            dataSet.setValues(entries);
            lineData.notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.invalidate();
        });
    }

    private void updateYLimits() {
        Float lowerLimit = sharedViewModel.getVoltageLowerYLimit().getValue();
        Float upperLimit = sharedViewModel.getVoltageUpperYLimit().getValue();

        YAxis yAxis = voltageChart.getAxisLeft();

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

        voltageChart.invalidate();
    }
}
}
