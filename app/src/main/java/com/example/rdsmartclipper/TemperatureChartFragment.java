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

public class TemperatureChartFragment extends Fragment {
    private LineChart temperatureChart;
    private SharedViewModel sharedViewModel;
    private LineDataSet temperatureDataSet;
    private LineData temperatureLineData;

    public TemperatureChartFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_temperature_chart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        temperatureChart = view.findViewById(R.id.temperature_chart);

        temperatureDataSet = new LineDataSet(new ArrayList<>(), "Temperature");
        temperatureLineData = new LineData(temperatureDataSet);
        temperatureChart.setData(temperatureLineData);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getTemperatureEntries().observe(getViewLifecycleOwner(), this::updateChart);
    }

    private void updateChart(List<Entry> entries) {
        temperatureDataSet.setValues(entries);
        temperatureLineData.notifyDataChanged();
        temperatureChart.notifyDataSetChanged();
        temperatureChart.invalidate();
    }
}
