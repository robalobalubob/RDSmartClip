package com.example.rdsmartclipper.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.example.rdsmartclipper.BaseChartFragment;
import com.example.rdsmartclipper.ChartData;
import com.github.mikephil.charting.data.Entry;

import java.util.List;

/**
 * VoltageChartFragment displays voltage data using the BaseChartFragment.
 */
public class RPMChartFragment extends BaseChartFragment {

    private ChartData rpmChartData;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the ChartData
        rpmChartData = sharedViewModel.getChartData("RPM");
    }

    @Override
    protected String getChartLabel() {
        return "RPM";
    }

    @Override
    protected LiveData<List<Entry>> getChartEntries() {
        return rpmChartData.getEntries();
    }

    @Override
    protected LiveData<Float> getLowerYLimit() {
        return rpmChartData.getLowerYLimit();
    }

    @Override
    protected LiveData<Float> getUpperYLimit() {
        return rpmChartData.getUpperYLimit();
    }

    @Override
    protected int getChartColor() {
        return Color.BLUE;
    }

    @Override
    protected void customizeChartAppearance() {
        // Set grid line colors
        chart.getXAxis().setGridColor(Color.GRAY);
        chart.getAxisLeft().setGridColor(Color.GRAY);

        // Set text colors
        chart.getXAxis().setTextColor(Color.WHITE);
        chart.getAxisLeft().setTextColor(Color.WHITE);

        // Set background color
        chart.setBackgroundColor(Color.BLACK);
    }

}
