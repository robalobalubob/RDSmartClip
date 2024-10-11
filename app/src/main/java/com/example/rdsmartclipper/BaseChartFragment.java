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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;

/**
 * BaseChartFragment is an abstract class that provides common functionality
 * for chart fragments. Subclasses must implement abstract methods to provide
 * specific data and configurations.
 */
public abstract class BaseChartFragment extends Fragment {

    protected LineChart chart;
    protected LineDataSet dataSet;
    protected LineData lineData;
    protected SharedViewModel sharedViewModel;

    // Abstract methods to be implemented by subclasses
    protected abstract String getChartLabel();
    protected abstract LiveData<List<Entry>> getChartEntries();
    protected abstract LiveData<Float> getLowerYLimit();
    protected abstract LiveData<Float> getUpperYLimit();
    protected abstract int getChartColor();
    protected abstract void customizeChartAppearance();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize the ViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    // Layout resource ID (can be overridden if necessary)
    protected int getLayoutResId() {
        return R.layout.fragment_chart;
    }

    // Chart ID in the layout (can be overridden if necessary)
    protected int getChartId() {
        return R.id.chart;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(getLayoutResId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        chart = view.findViewById(getChartId());

        // Initialize the data
        dataSet = new LineDataSet(null, getChartLabel());
        dataSet.setColor(getChartColor());
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Setup chart appearance
        setupChartAppearance();

        // Observe data and limits
        observeChartData();
        observeYLimits();

        // Handle toolbar settings
        handleToolbar();
    }

    private void setupChartAppearance() {
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false); // Disable right Y-axis
        chart.getLegend().setEnabled(false);    // Disable legend

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

        // Additional customization
        customizeChartAppearance();
    }

    private void observeChartData() {
        getChartEntries().observe(getViewLifecycleOwner(), entries -> {
            if (entries == null || entries.isEmpty()) {
                chart.clear();
                chart.setNoDataText("No data available");
                chart.invalidate();
                return;
            }
            dataSet.setValues(entries);
            lineData.notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.invalidate();
        });
    }

    private void observeYLimits() {
        Observer<Float> yLimitObserver = limit -> updateYLimits();
        getLowerYLimit().observe(getViewLifecycleOwner(), yLimitObserver);
        getUpperYLimit().observe(getViewLifecycleOwner(), yLimitObserver);
    }

    private void updateYLimits() {
        Float lowerLimit = getLowerYLimit().getValue();
        Float upperLimit = getUpperYLimit().getValue();

        YAxis leftAxis = chart.getAxisLeft();

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

        chart.invalidate();
    }

    private void handleToolbar() {
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        Toolbar toolbar = activity.findViewById(R.id.toolbar);

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle(getChartLabel());

            // Display the back arrow
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            // Handle back arrow click
            toolbar.setNavigationOnClickListener(v -> {
                // Navigate back to ChartsFragment
                activity.getSupportFragmentManager().popBackStack();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Reset toolbar to default state
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        Toolbar toolbar = activity.findViewById(R.id.toolbar);

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            activity.getSupportActionBar().setTitle("SmartClip");
            toolbar.setNavigationOnClickListener(null);
        }
    }
}
