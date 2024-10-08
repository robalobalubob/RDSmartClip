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
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * AccelerationChartFragment class
 */
public class AccelerationChartFragment extends Fragment {

    private LineChart accelerationChart;
    private LineDataSet accelerationDataSet;
    private LineData accelerationLineData;
    private SharedViewModel sharedViewModel;

    /**
     * Necessary empty Constructor
     */
    public AccelerationChartFragment() {
        // Required empty public constructor
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_acceleration_chart, container, false);
    }

    /**
     * Formats the chart and prepares the display
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Initialize the chart
        accelerationChart = view.findViewById(R.id.acceleration_chart);

        // Initialize the data
        accelerationDataSet = new LineDataSet(new ArrayList<>(), "Acceleration");
        accelerationLineData = new LineData(accelerationDataSet);
        accelerationChart.setData(accelerationLineData);

        // Initialize the ViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Setup chart appearance and initial Y-limits
        setupChart();

        // Observe the LiveData and update the chart data
        sharedViewModel.getAccelerationEntries().observe(getViewLifecycleOwner(), this::updateChart);

        // Observe Y-Limits and update the chart accordingly
        sharedViewModel.getAccelerationLowerYLimit().observe(getViewLifecycleOwner(),
                lowerLimit -> updateYLimits());

        sharedViewModel.getAccelerationUpperYLimit().observe(getViewLifecycleOwner(),
                upperLimit -> updateYLimits());

        // Handle toolbar settings
        handleToolbar();
    }

    /**
     * Set up chart appearance and initial Y-axis limits
     */
    private void setupChart() {
        // Customize chart appearance
        accelerationChart.getDescription().setEnabled(false);
        accelerationChart.getAxisRight().setEnabled(false); // Disable right axis

        // Apply initial Y-limits
        updateYLimits();
    }

    /**
     * Updates the Y-axis limits of the chart based on ViewModel values
     */
    private void updateYLimits() {
        Float lowerLimit = sharedViewModel.getAccelerationLowerYLimit().getValue();
        Float upperLimit = sharedViewModel.getAccelerationUpperYLimit().getValue();

        YAxis leftAxis = accelerationChart.getAxisLeft();

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

        accelerationChart.invalidate();
    }

    /**
     * Updates the chart with new data
     * @param entries Data to update the chart with
     */
    private void updateChart(List<Entry> entries) {
        accelerationDataSet.setValues(entries);
        accelerationLineData.notifyDataChanged();
        accelerationChart.notifyDataSetChanged();
        accelerationChart.invalidate();
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
            activity.getSupportActionBar().setTitle("Acceleration Chart");

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
