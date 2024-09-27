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

public class RPMChartFragment extends Fragment {
    private LineChart rpmChart;
    private SharedViewModel sharedViewModel;
    private LineDataSet rpmDataSet;
    private LineData rpmLineData;

    public RPMChartFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rpm_chart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rpmChart = view.findViewById(R.id.rpm_chart);

        rpmDataSet = new LineDataSet(new ArrayList<>(), "RPM");
        rpmLineData = new LineData(rpmDataSet);
        rpmChart.setData(rpmLineData);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getRPMEntries().observe(getViewLifecycleOwner(), this::updateChart);
    }

    private void updateChart(List<Entry> entries) {
        rpmDataSet.setValues(entries);
        rpmLineData.notifyDataChanged();
        rpmChart.notifyDataSetChanged();
        rpmChart.invalidate();
    }
}
