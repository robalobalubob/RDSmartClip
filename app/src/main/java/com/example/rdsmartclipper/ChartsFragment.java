package com.example.rdsmartclipper;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class ChartsFragment extends Fragment {

    private static final String VOLTAGE = "Voltage";
    private static final String CURRENT = "Current";
    private static final String ACCELERATION = "Acceleration";
    private static final String RPM = "RPM";
    private static final String TEMPERATURE = "Temperature";

    public ChartsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_charts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Setup chart buttons
        Button voltageChartButton = view.findViewById(R.id.button_voltage_chart);
        Button currentChartButton = view.findViewById(R.id.button_current_chart);
        Button accelerationChartButton = view.findViewById(R.id.button_acceleration_chart);
        Button rpmChartButton = view.findViewById(R.id.button_rpm_chart);
        Button temperatureChartButton = view.findViewById(R.id.button_temperature_chart);

        voltageChartButton.setOnClickListener(v -> openChartFragment(new VoltageChartFragment()));
        currentChartButton.setOnClickListener(v -> openChartFragment(new CurrentChartFragment()));
        accelerationChartButton.setOnClickListener(v -> openChartFragment(new AccelerationChartFragment()));
        rpmChartButton.setOnClickListener(v -> openChartFragment(new RPMChartFragment()));
        temperatureChartButton.setOnClickListener(v -> openChartFragment(new TemperatureChartFragment()));

        // Setup Y-Limit buttons
        Button voltageLimitButton = view.findViewById(R.id.button_voltage_limit);
        Button currentLimitButton = view.findViewById(R.id.button_current_limit);
        Button accelerationLimitButton = view.findViewById(R.id.button_acceleration_limit);
        Button rpmLimitButton = view.findViewById(R.id.button_rpm_limit);
        Button temperatureLimitButton = view.findViewById(R.id.button_temperature_limit);

        voltageLimitButton.setOnClickListener(v -> showYLimitDialog(VOLTAGE));
        currentLimitButton.setOnClickListener(v -> showYLimitDialog(CURRENT));
        accelerationLimitButton.setOnClickListener(v -> showYLimitDialog(ACCELERATION));
        rpmLimitButton.setOnClickListener(v -> showYLimitDialog(RPM));
        temperatureLimitButton.setOnClickListener(v -> showYLimitDialog(TEMPERATURE));
    }

    private void openChartFragment(Fragment fragment) {
        boolean isFullscreen = getArguments() != null && getArguments().getBoolean("isFullscreen", true);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).openFragment(fragment, isFullscreen);
        }
    }

    private void showYLimitDialog(String chartType) {
        Context context = getContext();
        if (context == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Set Y-Limits for " + chartType + " Chart");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_y_limits, null);
        builder.setView(dialogView);

        EditText lowerLimitInput = dialogView.findViewById(R.id.input_lower_limit);
        EditText upperLimitInput = dialogView.findViewById(R.id.input_upper_limit);

        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String lowerLimitStr = lowerLimitInput.getText().toString();
                String upperLimitStr = upperLimitInput.getText().toString();

                Float lowerLimit = lowerLimitStr.isEmpty() ? null : Float.parseFloat(lowerLimitStr);
                Float upperLimit = upperLimitStr.isEmpty() ? null : Float.parseFloat(upperLimitStr);

                if (lowerLimit != null && upperLimit != null && lowerLimit >= upperLimit) {
                    Toast.makeText(context, "Lower limit must be less than upper limit", Toast.LENGTH_SHORT).show();
                    return;
                }

                applyYLimits(chartType, lowerLimit, upperLimit);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void applyYLimits(String chartType, Float lowerLimit, Float upperLimit) {
        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        switch (chartType) {
            case VOLTAGE:
                sharedViewModel.setVoltageYLimits(lowerLimit, upperLimit);
                break;
            case CURRENT:
                sharedViewModel.setCurrentYLimits(lowerLimit, upperLimit);
                break;
            case ACCELERATION:
                sharedViewModel.setAccelerationYLimits(lowerLimit, upperLimit);
                break;
            case RPM:
                sharedViewModel.setRPMYLimits(lowerLimit, upperLimit);
                break;
            case TEMPERATURE:
                sharedViewModel.setTemperatureYLimits(lowerLimit, upperLimit);
                break;
            default:
                break;
        }

        Toast.makeText(getContext(), "Y-Limits applied for " + chartType + " chart.", Toast.LENGTH_SHORT).show();
    }
}

