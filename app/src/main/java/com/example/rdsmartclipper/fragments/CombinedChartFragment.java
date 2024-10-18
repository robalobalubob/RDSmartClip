package com.example.rdsmartclipper.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Necessary imports
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.rdsmartclipper.ChartData;
import com.example.rdsmartclipper.CustomMarkerView;
import com.example.rdsmartclipper.R;
import com.example.rdsmartclipper.SharedViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;


import java.util.ArrayList;

/**
 * CombinedChartFragment displays multiple charts in a single fragment.
 */
public class CombinedChartFragment extends Fragment {

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
        LineChart voltageChart = view.findViewById(R.id.voltage_chart);
        LineChart currentChart = view.findViewById(R.id.current_chart);
        LineChart accelerationChart = view.findViewById(R.id.acceleration_chart);
        LineChart rpmChart = view.findViewById(R.id.rpm_chart);

        // Initialize SharedViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Setup charts
        setupChart(voltageChart, "Voltage", android.graphics.Color.BLUE);
        setupChart(currentChart, "Current", android.graphics.Color.GREEN);
        setupChart(accelerationChart, "Acceleration", android.graphics.Color.MAGENTA);
        setupChart(rpmChart, "RPM", android.graphics.Color.RED);

        handleToolbar();
    }

    private void setupChart(LineChart chart, String chartType, int chartColor) {
        ChartData chartData = sharedViewModel.getChartData(chartType);

        LineDataSet dataSet = new LineDataSet(new ArrayList<>(), chartType);
        dataSet.setColor(chartColor);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);

        dataSet.setLabel(chartType);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Configure chart appearance
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);

        // Enable touch interactions
        chart.setTouchEnabled(true);
        chart.setHighlightPerTapEnabled(true);
        chart.setHighlightPerDragEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);

        // Set the custom MarkerView
        CustomMarkerView markerView = new CustomMarkerView(getContext());
        markerView.setChartView(chart);
        chart.setMarker(markerView);

        // Apply initial Y-limits
        updateYLimits(chart, chartData.getLowerYLimit().getValue(), chartData.getUpperYLimit().getValue());

        // Observe data changes
        chartData.getEntries().observe(getViewLifecycleOwner(), entries -> {
            if (entries != null) {
                dataSet.setValues(entries);
                lineData.notifyDataChanged();
                chart.notifyDataSetChanged();
                chart.invalidate();
            }
        });

        // Observe Y-Limit changes
        chartData.getLowerYLimit().observe(getViewLifecycleOwner(),
                lowerLimit -> updateYLimits(chart, lowerLimit, chartData.getUpperYLimit().getValue()));

        chartData.getUpperYLimit().observe(getViewLifecycleOwner(),
                upperLimit -> updateYLimits(chart, chartData.getLowerYLimit().getValue(), upperLimit));
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

    private void handleToolbar() {
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        Toolbar toolbar = activity.findViewById(R.id.toolbar);

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle("Combined Chart");

            // Display the back arrow
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);

            // Handle back arrow click
            toolbar.setNavigationOnClickListener(v -> {
                // Navigate back to MainActivity
                activity.getSupportFragmentManager().popBackStack();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

//        // Reset toolbar to default state
//        AppCompatActivity activity = (AppCompatActivity) requireActivity();
//        Toolbar toolbar = activity.findViewById(R.id.toolbar);
//
//        if (activity.getSupportActionBar() != null) {
//            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//            activity.getSupportActionBar().setTitle("SmartClip");
//            toolbar.setNavigationOnClickListener(null);
//        }
    }
}
