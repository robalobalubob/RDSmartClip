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
 * RPMChartFragment class
 */
public class RPMChartFragment extends Fragment {
    private LineChart rpmChart;
    private LineDataSet rpmDataSet;
    private LineData rpmLineData;

    /**
     * Necessary empty Constructor
     */
    public RPMChartFragment() {

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
        return inflater.inflate(R.layout.fragment_rpm_chart, container, false);
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
        rpmChart = view.findViewById(R.id.rpm_chart);

        // Initialize the data
        rpmDataSet = new LineDataSet(new ArrayList<>(), "RPM");
        rpmLineData = new LineData(rpmDataSet);
        rpmChart.setData(rpmLineData);

        // Initialize the ViewModel
        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        float yLimit = sharedViewModel.getRPMYLimit();

        // Apply y-limit to the chart
        YAxis leftAxis = rpmChart.getAxisLeft();
        leftAxis.setAxisMaximum(yLimit);
        leftAxis.setAxisMinimum(0);  // Set to 0 or any lower bound you prefer
        rpmChart.getAxisRight().setEnabled(false); // Disable right axis if not needed
        sharedViewModel.getRPMEntries().observe(getViewLifecycleOwner(), this::updateChart);

        boolean isFullscreen = getArguments() != null && getArguments().getBoolean("isFullscreen", false);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle("RPM Chart");
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
        rpmDataSet.setValues(entries);
        rpmLineData.notifyDataChanged();
        rpmChart.notifyDataSetChanged();
        rpmChart.invalidate();
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
