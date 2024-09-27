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

public class CurrentChartFragment extends Fragment {
    private LineChart currentChart;
    private SharedViewModel sharedViewModel;
    private LineDataSet currentDataSet;
    private LineData currentLineData;

    public CurrentChartFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_chart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        currentChart = view.findViewById(R.id.current_chart);

        currentDataSet = new LineDataSet(new ArrayList<>(), "Current");
        currentLineData = new LineData(currentDataSet);
        currentChart.setData(currentLineData);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getCurrentEntries().observe(getViewLifecycleOwner(), this::updateChart);
    }

    private void updateChart(List<Entry> entries) {
        currentDataSet.setValues(entries);
        currentLineData.notifyDataChanged();
        currentChart.notifyDataSetChanged();
        currentChart.invalidate();
    }
}

