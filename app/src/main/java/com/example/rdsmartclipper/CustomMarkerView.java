package com.example.rdsmartclipper;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.Locale;

public class CustomMarkerView extends MarkerView {

    private final TextView tvParameterName;
    private final TextView tvValue;
    private final TextView tvTime;

    public CustomMarkerView(Context context) {
        super(context, R.layout.marker_view);

        tvParameterName = findViewById(R.id.tvParameterName);
        tvValue = findViewById(R.id.tvValue);
        tvTime = findViewById(R.id.tvTime);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        // Get the parameter name from the DataSet label
        String parameterName = e.getData() != null ? e.getData().toString() : "Value";

        // Set the parameter name
        tvParameterName.setText(parameterName);

        // Format and set the value
        float value = e.getY();
        String formattedValue = formatValue(value, parameterName);
        tvValue.setText(formattedValue);

        // Format and set the time (assuming X-axis represents time in seconds)
        float time = e.getX();
        String formattedTime = formatTime(time);
        tvTime.setText(formattedTime);

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        // Center the marker horizontally and place it above the data point
        return new MPPointF(-(getWidth() / 2f), -getHeight());
    }

    private String formatValue(float value, String parameterName) {
        // Format the value based on the parameter
        switch (parameterName) {
            case "Voltage":
                return String.format(Locale.getDefault(), "%.2f V", value);
            case "Current":
                return String.format(Locale.getDefault(), "%.2f A", value);
            case "Temperature":
                return String.format(Locale.getDefault(), "%.2f°C", value);
            case "RPM":
                return String.format(Locale.getDefault(), "%.0f RPM", value);
            case "Acceleration":
                return String.format(Locale.getDefault(), "%.2f m/s²", value);
            default:
                return String.format(Locale.getDefault(), "%.2f", value);
        }
    }

    private String formatTime(float time) {
        int totalMSeconds = (int) time;
        int totalSeconds = totalMSeconds / 1000;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "Time: %d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "Time: %02d:%02d", minutes, seconds);
        }
    }
}
