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
 * TemperatureChartFragment class
 */
public class TemperatureChartFragment extends Fragment {
    private LineChart temperatureChart;
    private LineDataSet temperatureDataSet;
    private LineData temperatureLineData;

    /**
     * Necessary empty Constructor
     */
    public TemperatureChartFragment() {

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
        return inflater.inflate(R.layout.fragment_temperature_chart, container, false);
    }

    /**
     * Formats the chart and prepares the display
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        temperatureChart = view.findViewById(R.id.temperature_chart);

        temperatureDataSet = new LineDataSet(new ArrayList<>(), "Temperature");
        temperatureLineData = new LineData(temperatureDataSet);
        temperatureChart.setData(temperatureLineData);

        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        float yLimit = sharedViewModel.getTemperatureYLimit();

        // Apply y-limit to the chart
        YAxis leftAxis = temperatureChart.getAxisLeft();
        leftAxis.setAxisMaximum(yLimit);
        leftAxis.setAxisMinimum(0);  // Lower band set to 0
        temperatureChart.getAxisRight().setEnabled(false); // Disable right axis

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

    /**
     * Updates the chart with new data
     * @param entries List of entries to add to the chart
     */
    private void updateChart(List<Entry> entries) {
        temperatureDataSet.setValues(entries);
        temperatureLineData.notifyDataChanged();
        temperatureChart.notifyDataSetChanged();
        temperatureChart.invalidate();
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
