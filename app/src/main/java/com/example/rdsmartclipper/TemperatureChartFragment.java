package com.example.rdsmartclipper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * TemperatureChartFragment class
 */
public class TemperatureChartFragment extends Fragment {

    private LineChart temperatureChart;
    private LineDataSet temperatureDataSet;
    private LineData temperatureLineData;
    private SharedViewModel sharedViewModel;

    /**
     * Necessary empty Constructor
     */
    public TemperatureChartFragment() {
        // Required empty public constructor
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_temperature_chart, container, false);
    }

    /**
     * Formats the chart and prepares the display
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Initialize the chart
        temperatureChart = view.findViewById(R.id.temperature_chart);

        // Initialize the data
        temperatureDataSet = new LineDataSet(new ArrayList<>(), "Temperature");
        temperatureLineData = new LineData(temperatureDataSet);
        temperatureChart.setData(temperatureLineData);

        // Initialize the ViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Setup chart appearance and initial Y-limits
        setupChart();

        // Observe the LiveData and update the chart data
        sharedViewModel.getTemperatureEntries().observe(getViewLifecycleOwner(), this::updateChart);

        // Observe Y-Limits and update the chart accordingly
        sharedViewModel.getTemperatureLowerYLimit().observe(getViewLifecycleOwner(),
                lowerLimit -> updateYLimits());

        sharedViewModel.getTemperatureUpperYLimit().observe(getViewLifecycleOwner(),
                upperLimit -> updateYLimits());

        // Handle toolbar settings
        handleToolbar();
    }

    /**
     * Set up chart appearance and initial Y-axis limits
     */
    private void setupChart() {
        // Customize chart appearance
        temperatureChart.getDescription().setEnabled(false);
        temperatureChart.getAxisRight().setEnabled(false); // Disable right axis

        // Apply initial Y-limits
        updateYLimits();
    }

    /**
     * Updates the Y-axis limits of the chart based on ViewModel values
     */
    private void updateYLimits() {
        Float lowerLimit = sharedViewModel.getTemperatureLowerYLimit().getValue();
        Float upperLimit = sharedViewModel.getTemperatureUpperYLimit().getValue();

        YAxis leftAxis = temperatureChart.getAxisLeft();

        if (lowerLimit != null) {
            leftAxis.setAxisMinimum(lowerLimit);
        } else {
            leftAxis.resetAxisMinimum();
        }

        if (upperLimit != null) {
            leftAxis.setAxisMaximum(upperLimit);
        } else {
            leftAxis.resetAxisMaximum();
        }

        temperatureChart.invalidate();
    }

    /**
     * Updates the chart with new data
     */
    private void updateChart(List<Entry> entries) {
        temperatureDataSet.setValues(entries);
        temperatureLineData.notifyDataChanged();
        temperatureChart.notifyDataSetChanged();
        temperatureChart.invalidate();
    }

    /**
     * Handles the toolbar settings based on fullscreen mode
     */
    private void handleToolbar() {
        boolean isFullscreen = getArguments() != null && getArguments().getBoolean("isFullscreen", false);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle("Temperature Chart");

            if (isFullscreen) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                toolbar.setNavigationOnClickListener(v -> activity.getOnBackPressedDispatcher().onBackPressed());
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                toolbar.setNavigationOnClickListener(null);
            }
        }
    }

    /**
     * Called when the fragment is no longer in use.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Reset toolbar
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        Toolbar toolbar = activity.findViewById(R.id.toolbar);

        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.VISIBLE);

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            activity.getSupportActionBar().setTitle("SmartClip");
            toolbar.setNavigationOnClickListener(null);
        }
    }
}
