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

import java.util.ArrayList;
import java.util.List;

/**
 * VoltageChartFragment class
 */
public class VoltageChartFragment extends Fragment {
    private LineChart voltageChart;
    private LineDataSet voltageDataSet;
    private LineData voltageLineData;

    /**
     * Necessary empty Constructor
     */
    public VoltageChartFragment() {

    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_voltage_chart, container, false);
    }

    /**
     * Formats the chart and prepares the display
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Initialize the chart
        voltageChart = view.findViewById(R.id.voltage_chart);

        // Initialize the data
        voltageDataSet = new LineDataSet(new ArrayList<>(), "Voltage");
        voltageLineData = new LineData(voltageDataSet);
        voltageChart.setData(voltageLineData);

        // Initialize the ViewModel
        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        float yLimit = sharedViewModel.getVoltageYLimit();

        // Apply y-limit to the chart
        YAxis leftAxis = voltageChart.getAxisLeft();
        leftAxis.setAxisMaximum(yLimit);
        leftAxis.setAxisMinimum(0);  // Lower bound set to zero
        voltageChart.getAxisRight().setEnabled(false); // Disable right axis
        // Observe the LiveData and update the chart
        sharedViewModel.getVoltageEntries().observe(getViewLifecycleOwner(), this::updateChart);

        boolean isFullscreen = getArguments() != null && getArguments().getBoolean("isFullscreen", false);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle("Voltage Chart");
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

    /**
     * Updates the chart with new data
     * @param entries List of entries to add to the chart
     */
    private void updateChart(List<Entry> entries) {
        voltageDataSet.setValues(entries);
        voltageLineData.notifyDataChanged();
        voltageChart.notifyDataSetChanged();
        voltageChart.invalidate();
    }

    /**
     * Called when the fragment is no longer in use.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Reset toolbar
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            activity.getSupportActionBar().setTitle("SmartClip");
        }

        // Remove navigation click listener
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(null);
    }
}
