package com.example.rdsmartclipper;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

        boolean isFullscreen = getArguments() != null && getArguments().getBoolean("isFullscreen", false);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle("Temperature Chart");
            if (isFullscreen) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                // Handle toolbar navigation click
                Toolbar toolbar = activity.findViewById(R.id.toolbar);
                toolbar.setNavigationOnClickListener(v -> activity.getOnBackPressedDispatcher().onBackPressed());
            } else {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    private void updateChart(List<Entry> entries) {
        temperatureDataSet.setValues(entries);
        temperatureLineData.notifyDataChanged();
        temperatureChart.notifyDataSetChanged();
        temperatureChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Reset toolbar
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        Objects.requireNonNull(activity.getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        activity.getSupportActionBar().setTitle("SmartClip");

        // Remove navigation click listener
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(null);
    }
}
