package com.example.rdsmartclipper;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class VoltageChartFragment extends Fragment {
    private LineChart voltageChart;
    private SharedViewModel sharedViewModel;
    private LineDataSet voltageDataSet;
    private LineData voltageLineData;

    public VoltageChartFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_voltage_chart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        voltageChart = view.findViewById(R.id.voltage_chart);

        voltageDataSet = new LineDataSet(new ArrayList<>(), "Voltage");
        voltageLineData = new LineData(voltageDataSet);
        voltageChart.setData(voltageLineData);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getVoltageEntries().observe(getViewLifecycleOwner(), this::updateChart);
    }

    private void updateChart(List<Entry> entries) {
        voltageDataSet.setValues(entries);
        voltageLineData.notifyDataChanged();
        voltageChart.notifyDataSetChanged();
        voltageChart.invalidate();
    }
}
