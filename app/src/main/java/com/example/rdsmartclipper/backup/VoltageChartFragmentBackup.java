//package com.example.rdsmartclipper.backup;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.example.rdsmartclipper.R;
//import com.example.rdsmartclipper.SharedViewModel;
//import com.github.mikephil.charting.charts.LineChart;
//import com.github.mikephil.charting.components.YAxis;
//import com.github.mikephil.charting.data.Entry;
//import com.github.mikephil.charting.data.LineData;
//import com.github.mikephil.charting.data.LineDataSet;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * VoltageChartFragment class
// */
//public class VoltageChartFragmentBackup extends Fragment {
//
//    private LineChart voltageChart;
//    private LineDataSet voltageDataSet;
//    private LineData voltageLineData;
//    private SharedViewModel sharedViewModel;
//
//    /**
//     * Necessary empty Constructor
//     */
//    public VoltageChartFragmentBackup() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Called to have the fragment instantiate its user interface view.
//     */
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_voltage_chart, container, false);
//    }
//
//    /**
//     * Formats the chart and prepares the display
//     */
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        // Initialize the chart
//        voltageChart = view.findViewById(R.id.voltage_chart);
//
//        // Initialize the data
//        voltageDataSet = new LineDataSet(new ArrayList<>(), "Voltage");
//        voltageLineData = new LineData(voltageDataSet);
//        voltageChart.setData(voltageLineData);
//
//        // Initialize the ViewModel
//        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
//
//        // Setup chart appearance and initial Y-limits
//        setupChart();
//
//        // Observe the LiveData and update the chart data
//        sharedViewModel.getVoltageEntries().observe(getViewLifecycleOwner(), this::updateChart);
//
//        // Observe Y-Limits and update the chart accordingly
//        sharedViewModel.getVoltageLowerYLimit().observe(getViewLifecycleOwner(),
//                lowerLimit -> updateYLimits());
//
//        sharedViewModel.getVoltageUpperYLimit().observe(getViewLifecycleOwner(),
//                upperLimit -> updateYLimits());
//
//        // Handle toolbar settings
//        handleToolbar();
//    }
//
//    /**
//     * Set up chart appearance and initial Y-axis limits
//     */
//    private void setupChart() {
//        // Customize chart appearance
//        voltageChart.getDescription().setEnabled(false);
//        voltageChart.getAxisRight().setEnabled(false); // Disable right axis
//
//        // Apply initial Y-limits
//        updateYLimits();
//    }
//
//    /**
//     * Updates the Y-axis limits of the chart based on ViewModel values
//     */
//    private void updateYLimits() {
//        Float lowerLimit = sharedViewModel.getVoltageLowerYLimit().getValue();
//        Float upperLimit = sharedViewModel.getVoltageUpperYLimit().getValue();
//
//        YAxis leftAxis = voltageChart.getAxisLeft();
//
//        if (lowerLimit != null) {
//            leftAxis.setAxisMinimum(lowerLimit);
//        } else {
//            leftAxis.resetAxisMinimum();
//        }
//
//        if (upperLimit != null) {
//            leftAxis.setAxisMaximum(upperLimit);
//        } else {
//            leftAxis.resetAxisMaximum();
//        }
//
//        voltageChart.invalidate();
//    }
//
//    /**
//     * Updates the chart with new data
//     */
//    private void updateChart(List<Entry> entries) {
//        voltageDataSet.setValues(entries);
//        voltageLineData.notifyDataChanged();
//        voltageChart.notifyDataSetChanged();
//        voltageChart.invalidate();
//    }
//
//    /**
//     * Handles the toolbar settings based on fullscreen mode
//     */
//    private void handleToolbar() {
//        boolean isFullscreen = getArguments() != null && getArguments().getBoolean("isFullscreen", false);
//
//        AppCompatActivity activity = (AppCompatActivity) requireActivity();
//        Toolbar toolbar = activity.findViewById(R.id.toolbar);
//        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);
//
//        if (activity.getSupportActionBar() != null) {
//            activity.getSupportActionBar().setTitle("Voltage Chart");
//
//            if (isFullscreen) {
//                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//                toolbar.setNavigationOnClickListener(v -> activity.getOnBackPressedDispatcher().onBackPressed());
//                bottomNavigationView.setVisibility(View.GONE);
//            } else {
//                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//                toolbar.setNavigationOnClickListener(null);
//            }
//        }
//    }
//
//    /**
//     * Called when the fragment is no longer in use.
//     */
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//
//        // Reset toolbar
//        AppCompatActivity activity = (AppCompatActivity) requireActivity();
//        Toolbar toolbar = activity.findViewById(R.id.toolbar);
//
//        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);
//        bottomNavigationView.setVisibility(View.VISIBLE);
//
//        if (activity.getSupportActionBar() != null) {
//            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//            activity.getSupportActionBar().setTitle("SmartClip");
//            toolbar.setNavigationOnClickListener(null);
//        }
//    }
//}
